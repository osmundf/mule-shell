package net.sf.zoftwhere.mule;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sf.zoftwhere.dropwizard.AbstractEntity;
import net.sf.zoftwhere.dropwizard.DatabaseConfiguration;
import net.sf.zoftwhere.dropwizard.security.AuthorizationAuthFilter;
import net.sf.zoftwhere.mule.jpa.AccessToken;
import net.sf.zoftwhere.mule.jpa.Account;
import net.sf.zoftwhere.mule.jpa.ShellSession;
import net.sf.zoftwhere.mule.resource.AccessResource;
import net.sf.zoftwhere.mule.resource.AssetResource;
import net.sf.zoftwhere.mule.resource.ExpressionResource;
import net.sf.zoftwhere.mule.resource.SessionResource;
import net.sf.zoftwhere.mule.security.AccountAuthenticator;
import net.sf.zoftwhere.mule.security.AccountAuthorizer;
import net.sf.zoftwhere.mule.security.AccountPrincipal;
import net.sf.zoftwhere.mule.security.JWTSigner;
import net.sf.zoftwhere.mule.shell.JShellManager;
import net.sf.zoftwhere.mule.shell.UUIDBuffer;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ru.vyarus.dropwizard.guice.GuiceBundle;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class MuleApplication extends Application<MuleConfiguration> {

	public static void main(String[] args) throws Exception {
		new MuleApplication().run(args);
	}

	private final HibernateBundle<MuleConfiguration> hibernateBundle = getHibernateBundle();

	@Override
	public String getName() {
		return "mule-shell-server";
	}

	@Override
	public void run(MuleConfiguration configuration, Environment environment) {
		environment.jersey().register(new AuthDynamicFeature(
				new AuthorizationAuthFilter.Builder<AccountPrincipal>()
						.setAuthenticator(new AccountAuthenticator())
						.setAuthorizer(new AccountAuthorizer())
						.setRealm("mule-shell-public")
						.buildAuthFilter()));

		updateObjectMapper(environment.getObjectMapper());

		environment.jersey().register(RolesAllowedDynamicFeature.class);
		environment.jersey().register(AccessResource.class);
		environment.jersey().register(AssetResource.class);
		environment.jersey().register(ExpressionResource.class);
		environment.jersey().register(SessionResource.class);
	}

	@Override
	public void initialize(Bootstrap<MuleConfiguration> bootstrap) {
		// Enable variable substitution with environment variables
		bootstrap.setConfigurationSourceProvider(
				new SubstitutingSourceProvider(
						bootstrap.getConfigurationSourceProvider(),
						new EnvironmentVariableSubstitutor(false)
				)
		);

		bootstrap.addBundle(hibernateBundle);

		GuiceBundle<MuleConfiguration> guiceBundle = this.<MuleConfiguration>setupGuice()
				.modules(secureModule(Algorithm.HMAC256("secret"), "mule-shel"))
				.modules(serverModule())
				.modules(muleModule())
				.build();

		bootstrap.addBundle(guiceBundle);
	}

	public ObjectMapper updateObjectMapper(ObjectMapper objectMapper) {

		final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ");

		SimpleModule module = new SimpleModule();
		module.addSerializer(OffsetDateTime.class, new JsonSerializer<>() {
			@Override
			public void serialize(OffsetDateTime dateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
				jsonGenerator.writeString(dateTimeFormatter.format(dateTime));
			}
		});

		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.registerModule(module);

		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		return objectMapper;
	}

	public <T extends Configuration> GuiceBundle.Builder<T> setupGuice() {
		return GuiceBundle.<T>builder().enableAutoConfig(getClass().getPackage().getName());
	}

	private AbstractModule secureModule(final Algorithm algorithm, final String issuer) {
		return new AbstractModule() {
			@Override
			protected void configure() {
				super.configure();
			}

			@Provides
			@Singleton
			public JWTVerifier getJWTVerifier() {
				return JWT.require(algorithm)
						.withIssuer(issuer)
						.acceptIssuedAt(0)
						.acceptExpiresAt(0)
						.acceptNotBefore(0)
						.build();
			}

			@Provides
			@Singleton
			public JWTSigner getJWTSigner() {
				return new JWTSigner(algorithm);
			}
		};
	}

	private AbstractModule serverModule() {
		return new AbstractModule() {

			@Override
			protected void configure() {
				bind(SessionFactory.class).toProvider(hibernateBundle::getSessionFactory).in(Singleton.class);
				bind(Session.class).toProvider(() -> hibernateBundle.getSessionFactory().openSession());
			}

			@Provides
			public ObjectMapper getObjectMapper(Environment environment) {
				return environment.getObjectMapper();
			}
		};
	}

	private AbstractModule muleModule() {
		return new AbstractModule() {
			@Provides
			@Singleton
			public JShellManager getJShell() {
				return new JShellManager();
			}

			@Provides
			@Singleton
			public UUIDBuffer getUUIDBuffer() {
				return new UUIDBuffer(new Random());
			}
		};
	}


	public static <T extends DatabaseConfiguration> HibernateBundle<T> getHibernateBundle() {
		return new HibernateBundle<>(AbstractEntity.class, persistenceEntities()) {
			@Override
			public DataSourceFactory getDataSourceFactory(T configuration) {
				return configuration.getDataSourceFactory();
			}

			@Override
			protected void configure(org.hibernate.cfg.Configuration configuration) {
				for (Class<?> clazz : persistenceEntities()) {
					configuration.addAnnotatedClass(clazz);
				}

				configuration.setProperty("hibernate.hbm2ddl.auto", "update");
			}
		};
	}

	public static Class<?>[] persistenceEntities() {
		return new Class<?>[]{
				Account.class,
				AccessToken.class,
				ShellSession.class,
		};
	}
}