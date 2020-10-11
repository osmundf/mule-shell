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
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import net.sf.zoftwhere.dropwizard.AbstractEntity;
import net.sf.zoftwhere.dropwizard.ContextPath;
import net.sf.zoftwhere.dropwizard.DatabaseConfiguration;
import net.sf.zoftwhere.dropwizard.MuleInfo;
import net.sf.zoftwhere.dropwizard.ViewAssetPath;
import net.sf.zoftwhere.dropwizard.security.AuthorizationAuthFilter;
import net.sf.zoftwhere.hibernate.MacroCaseNamingStrategy;
import net.sf.zoftwhere.hibernate.SnakeCaseNamingStrategy;
import net.sf.zoftwhere.mule.data.TryParse;
import net.sf.zoftwhere.mule.data.Variable;
import net.sf.zoftwhere.mule.function.PlaceHolder;
import net.sf.zoftwhere.mule.jpa.Account;
import net.sf.zoftwhere.mule.jpa.AccountRole;
import net.sf.zoftwhere.mule.jpa.Role;
import net.sf.zoftwhere.mule.jpa.Setting;
import net.sf.zoftwhere.mule.jpa.SettingLocator;
import net.sf.zoftwhere.mule.jpa.ShellSession;
import net.sf.zoftwhere.mule.jpa.Token;
import net.sf.zoftwhere.mule.security.AccountAuthenticator;
import net.sf.zoftwhere.mule.security.AccountAuthorizer;
import net.sf.zoftwhere.mule.security.AccountPrincipal;
import net.sf.zoftwhere.mule.security.AccountSigner;
import net.sf.zoftwhere.mule.security.AuthenticationScheme;
import net.sf.zoftwhere.mule.security.JWTSigner;
import net.sf.zoftwhere.mule.security.SecureModule;
import net.sf.zoftwhere.mule.shell.MuleShell;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.GuiceBundle;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.getProperty;
import static net.sf.zoftwhere.mule.MuleApplicationBuilder.create;

public class MuleApplication extends Application<MuleConfiguration> {

	private static final Logger logger = LoggerFactory.getLogger(MuleApplication.class);

	public static final String USER_CACHE_SIZE_PROPERTY = "MuleShellUserCacheSize";

	public static final String SHELL_CACHE_SIZE_PROPERTY = "MuleShellShellCacheSize";

	public static void main(String[] args) throws Exception {
		long time = -System.nanoTime();
		create(MuleApplication::new)
				.realm("mule-shell-public")
				.userCacheSize(TryParse.toInteger(getProperty(USER_CACHE_SIZE_PROPERTY)).orElse(10))
				.shellCacheSize(TryParse.toInteger(getProperty(SHELL_CACHE_SIZE_PROPERTY)).orElse(10))
				.run(args);
		time += System.nanoTime();
		logger.info("Started: " + ((time / 1_000) / 1e3) + " ms");
	}

	private final ExecutorService executor;

	private final HibernateBundle<MuleConfiguration> hibernateBundle;

	private final String realm;

	private final PlaceHolder<String> contextPath = new Variable<>();

	private final PlaceHolder<ViewAssetPath> viewAssetPath = new Variable<>();

	private final Cache<UUID, AccountPrincipal> principalCache;

	private final Cache<UUID, MuleShell> shellCache;

	private final PlaceHolder<JWTSigner> jwtSigner = new Variable<>();

	private final PlaceHolder<JWTVerifier> jwtVerifier = new Variable<>();

	public <T extends MuleApplication> MuleApplication(MuleApplicationBuilder<T> builder) {
		this.realm = builder.realm();
		this.executor = getExecutor();
		this.hibernateBundle = newHibernateBundle();
		this.principalCache = newLoginAccountCache(builder.userCacheSize());
		this.shellCache = newMuleShellCache(builder.shellCacheSize());
	}

	@Override
	public String getName() {
		return "mule-shell-server";
	}

	@Override
	public void initialize(Bootstrap<MuleConfiguration> bootstrap) {
		// Enable variable substitution with environment variables.
		bootstrap.setConfigurationSourceProvider(
				new SubstitutingSourceProvider(
						bootstrap.getConfigurationSourceProvider(),
						new EnvironmentVariableSubstitutor(false)
				)
		);

		// Make static assets available if they're present.
		bootstrap.addBundle(new AssetsBundle("/assets", "/assets", ""));

		// Drop-Wizard views.
		bootstrap.addBundle(new ViewBundle<>());

		bootstrap.addBundle(hibernateBundle);

		GuiceBundle guiceBundle = newGuiceBuilder()
				.modules(new SecureModule(jwtSigner, jwtVerifier))
				.modules(serverModule())
				.build();

		bootstrap.addBundle(guiceBundle);
	}

	@Override
	public void run(MuleConfiguration configuration, Environment environment) {
		// Handle dates correctly in the json serializer/deserializer.
		environment.getObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		environment.getObjectMapper().registerModule(new JavaTimeModule());

		String base = environment.getApplicationContext().getContextPath();
		base = base == null || "/".equals(base) ? "" : base;
		contextPath.set(base);

		final var map = new HashMap<String, String>();
		for (var o : configuration.getViewAssetPath().entrySet()) {
			final var key = o.getKey();
			final var value = String.format(o.getValue(), base);
			map.put(key, value);
		}
		viewAssetPath.set(new ViewAssetPath(map));

		setupDatabaseData(hibernateBundle.getSessionFactory());
		applySecurityFacet(configuration, environment);
	}

	protected ExecutorService getExecutor() {
		return Executors.newSingleThreadExecutor();
	}

	private GuiceBundle.Builder newGuiceBuilder() {
		return MuleApplication.newGuiceBuilder(getClass().getPackage());
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
			public ExecutorService getExecutorService() {
				return executor;
			}

			@Provides
			public ObjectMapper getObjectMapper(Environment environment) {
				return environment.getObjectMapper();
			}

			@Provides
			@Singleton
			public MuleInfo getMuleProperties(MuleConfiguration configuration) {
				return configuration.getInfo();
			}

			@Provides
			@Singleton
			public ContextPath getContextPath(Environment environment) {
				return new ContextPath(environment.getApplicationContext().getContextPath());
			}

			@Provides
			//TODO
			// @Singleton
			public ViewAssetPath getViewAssetPath() {
				return viewAssetPath.get();
			}

			@Provides
			public AccountSigner getAccountSigner() {
				try {
					return newAccountSigner();
				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException(e);
				}
			}

			@Provides
			@Singleton
			public Cache<UUID, AccountPrincipal> getLoginCache() {
				return principalCache;
			}

			@Provides
			@Singleton
			public Cache<UUID, MuleShell> getJShellCache() {
				return shellCache;
			}
		};
	}

	public <T extends DatabaseConfiguration> HibernateBundle<T> newHibernateBundle() {
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

	@SuppressWarnings("unused")
	protected void setupDatabaseData(SessionFactory sessionFactory) {
	}

	protected void applySecurityFacet(MuleConfiguration configuration, Environment environment) {
		final var sessionFactory = hibernateBundle.getSessionFactory();
		final var settingLocator = new SettingLocator(sessionFactory::openSession);

		final var jwtIssuerSetting = settingLocator.getByKey("mule-shell-jwt-issuer")
				.orElseThrow(RuntimeException::new);

		final var jwtHashSecret = settingLocator.getByKey("mule-shell-jwt-hash-key")
				.orElseThrow(RuntimeException::new);

		final var issuer = jwtIssuerSetting.getValue();
		final var algorithm = Algorithm.HMAC256(jwtHashSecret.getValue());
		final var signer = new JWTSigner(issuer, algorithm);

		JWTVerifier verifier = JWT.require(algorithm)
				.withIssuer(issuer)
				.acceptIssuedAt(0)
				.acceptNotBefore(0)
				.acceptExpiresAt(0)
				.build();

		// Update signer and verifier placeholders.
		jwtSigner.set(signer);
		jwtVerifier.set(verifier);

		// Create filter
		final var filter = new AuthorizationAuthFilter.Builder<AccountPrincipal>()
				.setAuthorizer(new AccountAuthorizer())
				.setAuthenticator(new AccountAuthenticator(principalCache, jwtVerifier.get(), sessionFactory::openSession))
				.setPrefix(AuthenticationScheme.BEARER)
				.setRealm(realm)
				.buildAuthFilter();

		// Add AuthFilters and Roles.
		environment.jersey().register(new AuthDynamicFeature(filter));
		environment.jersey().register(RolesAllowedDynamicFeature.class);
	}

	protected static AccountSigner newAccountSigner() throws NoSuchAlgorithmException {
		return new AccountSigner(MessageDigest.getInstance("SHA-256"), 6);
	}

	public static GuiceBundle.Builder newGuiceBuilder(Package basePackage) {
		return GuiceBundle.builder().enableAutoConfig(basePackage.getName());
	}

	public static Cache<UUID, AccountPrincipal> newLoginAccountCache(int maximumSize) {
		return CacheBuilder.newBuilder()
				.maximumSize(maximumSize)
				.expireAfterWrite(Duration.ofMinutes(30))
				.build();
	}

	public static Cache<UUID, MuleShell> newMuleShellCache(int maximumSize) {
		return CacheBuilder.newBuilder()
				.maximumSize(maximumSize)
				.expireAfterWrite(Duration.ofMinutes(30))
				.build();
	}

	public static Class<?>[] persistenceEntities() {
		return new Class<?>[]{
				Account.class,
				AccountRole.class,
				Role.class,
				Setting.class,
				ShellSession.class,
				Token.class,
		};
	}
}
