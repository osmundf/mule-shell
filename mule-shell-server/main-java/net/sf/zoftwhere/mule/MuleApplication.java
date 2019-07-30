package net.sf.zoftwhere.mule;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import net.sf.zoftwhere.hibernate.MacroCaseNamingStrategy;
import net.sf.zoftwhere.hibernate.SnakeCaseNamingStrategy;
import net.sf.zoftwhere.mule.jpa.Account;
import net.sf.zoftwhere.mule.jpa.AccountRole;
import net.sf.zoftwhere.mule.jpa.Role;
import net.sf.zoftwhere.mule.jpa.ShellSession;
import net.sf.zoftwhere.mule.jpa.Token;
import net.sf.zoftwhere.mule.security.AccountAuthenticator;
import net.sf.zoftwhere.mule.security.AccountAuthorizer;
import net.sf.zoftwhere.mule.security.AccountPrincipal;
import net.sf.zoftwhere.mule.security.AccountSigner;
import net.sf.zoftwhere.mule.security.JWTSigner;
import net.sf.zoftwhere.mule.security.SecureModule;
import net.sf.zoftwhere.mule.shell.JShellManager;
import net.sf.zoftwhere.mule.shell.UUIDBuffer;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.GuiceBundle;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;

public class MuleApplication extends Application<MuleConfiguration> {

	private static final Logger logger = LoggerFactory.getLogger(MuleApplication.class);

	public static void main(String[] args) throws Exception {
		long time = -System.nanoTime();
		new MuleApplication().run(args);
		time += System.nanoTime();
		logger.info("Started: " + ((time / 1_000) / 1e3) + " ms");
	}

	private final HibernateBundle<MuleConfiguration> hibernateBundle = getHibernateBundle();

	private final Cache<UUID, AccountPrincipal> cache = getLoginAccountCache();

	private JWTVerifier verifier;

	@Override
	public String getName() {
		return "mule-shell-server";
	}

	@Override
	public void run(MuleConfiguration configuration, Environment environment) {
		// Handle dates correctly in the json serializer/deserializer.
		environment.getObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		environment.getObjectMapper().registerModule(new JavaTimeModule());

		// Add AuthFilters and Roles.
		addSecurity(environment, hibernateBundle.getSessionFactory());
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

		// TODO: Retrieve from database.
		final var issuer = "mule-shell";
		final var algorithm = Algorithm.HMAC256("secret");
		final var signer = new JWTSigner(issuer, algorithm);

		this.verifier = JWT.require(algorithm)
				.withIssuer(issuer)
				.acceptIssuedAt(0)
				.acceptNotBefore(0)
				.acceptExpiresAt(0)
				.build();

		GuiceBundle<MuleConfiguration> guiceBundle = this.<MuleConfiguration>setupGuice()
				.modules(new SecureModule(verifier, signer))
				.modules(serverModule())
				.modules(muleModule())
				.build();

		bootstrap.addBundle(hibernateBundle);

		bootstrap.addBundle(guiceBundle);
	}

	public <T extends Configuration> GuiceBundle.Builder<T> setupGuice() {
		return GuiceBundle.<T>builder().enableAutoConfig(getClass().getPackage().getName());
	}

	protected Cache<UUID, AccountPrincipal> getLoginAccountCache() {
		return CacheBuilder.newBuilder()
				.maximumSize(10000)
				.expireAfterWrite(Duration.ofMinutes(30))
				.build();
	}

	protected void addSecurity(Environment environment, SessionFactory sessionFactory) {
		environment.jersey().register(new AuthDynamicFeature(
				new AuthorizationAuthFilter.Builder<AccountPrincipal>()
						.setAuthorizer(new AccountAuthorizer())
						.setAuthenticator(new AccountAuthenticator(cache, verifier, sessionFactory::openSession))
						.setPrefix("bearer")
						.setRealm("mule-shell-public")
						.buildAuthFilter()));
		environment.jersey().register(RolesAllowedDynamicFeature.class);
	}

	protected AbstractModule serverModule() {
		return new AbstractModule() {

			@Override
			protected void configure() {
				final var sessionFactory = hibernateBundle.getSessionFactory();
				bind(SessionFactory.class).toProvider(() -> sessionFactory).in(Singleton.class);
				bind(Session.class).toProvider(sessionFactory::openSession);
			}

			@Provides
			public ObjectMapper getObjectMapper(Environment environment) {
				return environment.getObjectMapper();
			}

			@Provides
			public AccountSigner getAccountSigner() {
				try {
					return new AccountSigner(MessageDigest.getInstance("SHA-256"), 6);
				} catch (NoSuchAlgorithmException e) {
					return null;
				}
			}

			@Provides
			@Singleton
			public Cache<UUID, AccountPrincipal> getLoginCache() {
				return cache;
			}
		};
	}

	protected AbstractModule muleModule() {
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
				final String namingStrategy = configuration.getProperty("hibernate.physical_naming_strategy");

				if (SnakeCaseNamingStrategy.class.getName().equals(namingStrategy)) {
					configuration.setPhysicalNamingStrategy(new SnakeCaseNamingStrategy());
				} else if (MacroCaseNamingStrategy.class.getName().equals(namingStrategy)) {
					configuration.setPhysicalNamingStrategy(new MacroCaseNamingStrategy());
				} else {
					logger.warn("The following naming strategy may not have been loaded: {}", new Object[]{namingStrategy});
				}
			}
		};
	}

	public static Class<?>[] persistenceEntities() {
		return new Class<?>[]{
				Role.class,
				Token.class,
				Account.class,
				AccountRole.class,
				ShellSession.class,
		};
	}
}