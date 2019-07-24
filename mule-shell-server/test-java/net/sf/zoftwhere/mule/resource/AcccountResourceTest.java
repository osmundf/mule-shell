package net.sf.zoftwhere.mule.resource;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.sf.zoftwhere.hibernate.HibernateLoader;
import net.sf.zoftwhere.hibernate.SnakeCaseNamingStrategy;
import net.sf.zoftwhere.mule.MuleApplication;
import net.sf.zoftwhere.mule.jpa.Account;
import net.sf.zoftwhere.mule.jpa.AccountLocator;
import net.sf.zoftwhere.mule.security.AccountPrincipal;
import net.sf.zoftwhere.mule.security.AccountSigner;
import net.sf.zoftwhere.mule.security.JWTSigner;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountResourceTest extends AccountResource {

	private final static Logger logger = LoggerFactory.getLogger(AccountResourceTest.class);

	private static final List<Class<?>> entityList = List.of(MuleApplication.persistenceEntities());

	private final Provider<Session> sessionProvider;

	private final AccountResource resource;

	private final Injector guiceInjector;

	AccountResourceTest() {
		super(null);
		final var sessionFactory = newSessionFactory();
		sessionProvider = newSessionProvider(sessionFactory);
		resource = new AccountResource(newSessionProvider(sessionFactory));
		final var algorithm = Algorithm.HMAC256("test-public-secret");
		final var issuer = "mule-server-test";
		final var signer = new JWTSigner(issuer, algorithm);
		final var verifier = JWT.require(algorithm)
				.withIssuer(issuer)
				.acceptIssuedAt(0)
				.acceptNotBefore(0)
				.acceptExpiresAt(0)
				.build();

		guiceInjector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				super.configure();
				bind(SessionFactory.class).toProvider(() -> sessionFactory).in(Singleton.class);
				bind(Session.class).toProvider(sessionFactory::openSession);
				bind(JWTVerifier.class).toInstance(verifier);
				bind(JWTSigner.class).toInstance(signer);

				final var cacheKey = new Key<Cache<UUID, AccountPrincipal>>() {};
				bind(cacheKey).toProvider(this::getLoginCache).in(Singleton.class);
				bind(AccountSigner.class).toProvider(this::getAccountSigner);
			}

			private Cache<UUID, AccountPrincipal> getLoginCache() {
				return CacheBuilder.newBuilder()
						.maximumSize(10000)
						.expireAfterWrite(Duration.ofMinutes(30))
						.build();
			}

			private AccountSigner getAccountSigner() {
				try {
					return new AccountSigner(MessageDigest.getInstance("SHA-256"));
				} catch (NoSuchAlgorithmException e) {
					return null;
				}
			}
		});
	}

	@Test
	public void testLoginSuccess() {
		guiceInjector.injectMembers(resource);
		final var key = new Key<Cache<UUID, AccountPrincipal>>() {};
		Provider<Cache<UUID, AccountPrincipal>> cacheProvider = guiceInjector.getProvider(key);
		Provider<AccountSigner> accountSignerProvider = guiceInjector.getProvider(AccountSigner.class);

		final var jwtVerifier = guiceInjector.getInstance(JWTVerifier.class);

		final var cache = cacheProvider.get();
		final var digest = accountSignerProvider.get();
		final var salt = digest.generateSalt(512);
		final var password = "test-public-secret";
		final var data = password.getBytes(StandardCharsets.UTF_8);
		final var hash = digest.getHash(salt, data);

		wrapTransaction(sessionProvider, session -> {
			Account account = new Account();
			account.setUsername("test");
			account.setEmailAddress("test@test.test");
			account.setSalt(salt);
			account.setHash(hash);
			session.persist(account);
		});

//		try (var session = sessionProvider.get()) {
//			Account account = new Account();
//			account.setUsername("test");
//			account.setEmailAddress("test@test.test");
//			account.setSalt(salt);
//			account.setHash(hash);
//
//			session.beginTransaction();
//			session.persist(account);
//			session.getTransaction().commit();
//			session.close();
//		}

		final var accountLocator = new AccountLocator(sessionProvider);
		final var account = tryFetchEntity("test", Optional::of, accountLocator::getByUsername).orElse(null);
		assertNotNull(account);

		final var scheme = "basic";
		final var credentials = ("test" + ":" + password).getBytes(StandardCharsets.UTF_8);
		final var header = scheme + " " + Base64.getEncoder().encodeToString(credentials);
		final var login = resource.login(Collections.singletonList(header));
		assertEquals(Status.OK.getStatusCode(), login.getStatus(), "OK: 200");

		final var token = login.getEntity().toString();
		final var jwtDecoded = jwtVerifier.verify(token);
		final var uuid = UUID.fromString(jwtDecoded.getId());

		final Provider<Optional<AccountPrincipal>> principalProvider = () -> {
			try {
				return Optional.of(cache.get(uuid, () -> new AccountPrincipal("", "")));
			} catch (ExecutionException e) {
				return Optional.empty();
			}
		};
		final AccountPrincipal cached = principalProvider.get().orElse(null);

		assertNotNull(login);
		assertNotNull(cache);
		assertNotNull(cached);
		assertEquals("test", cached.getUsername().orElse(null), "Username");
		assertEquals("CLIENT", cached.getRole().orElse(null), "Role");
	}

	@Test
	public void testLoginFail() {
		guiceInjector.injectMembers(resource);
		Provider<AccountSigner> accountSignerProvider = guiceInjector.getProvider(AccountSigner.class);
		final var digest = accountSignerProvider.get();
		final var salt = digest.generateSalt(512);
		final var data = "test-public-secret".getBytes(StandardCharsets.UTF_8);
		final var hash = digest.getHash(salt, data);

		// toProvider(new TypeLiteral<Cache<UUID, AccountPrincipal>>() { });

		wrapTransaction(sessionProvider, session -> {
			Account account = new Account();
			account.setUsername("test");
			account.setEmailAddress("test@test.test");
			account.setSalt(salt);
			account.setHash(hash);
			session.persist(account);
		});

//		try (var session = sessionProvider.get()) {
//			Account account = new Account();
//			account.setUsername("test");
//			account.setEmailAddress("test@test.test");
//			account.setSalt(salt);
//			account.setHash(hash);
//			session.beginTransaction();
//			session.persist(account);
//			session.getTransaction().commit();
//			session.close();
//		}

		final var accountLocator = new AccountLocator(sessionProvider);
		final var account = tryFetchEntity("test", Optional::of, accountLocator::getByUsername).orElse(null);
		assertNotNull(account);

		final var scheme = "basic";
		final var openJoin = new String[]{
				"fake:thisPasswordIsLongEnough",
				"test:2tiny",
				"test:wrongPassword",
				"spammer:This Password Is Long Enough To Cause The Encoder To Take Longer Than Needed To Decode And Check, " +
						"so the limit is set to a feasible limit to stop spamming of long authorization headers.",
		};

		for (String string : openJoin) {
			final var credentials = string.getBytes(StandardCharsets.UTF_8);
			final var header = scheme + " " + Base64.getEncoder().encodeToString(credentials);
			final var login = resource.login(Collections.singletonList(header));

			assertNotNull(login);
			assertEquals(Status.UNAUTHORIZED.getStatusCode(), login.getStatus(), "Unauthorized: 401 (" + string + ")");
		}
	}

	@Test
	public void testLogoutFailure() {
		guiceInjector.injectMembers(resource);

	}

	private static void wrapTransaction(Provider<Session> sessionProvider, Consumer<Session> action) {
		final var session = sessionProvider.get();
		session.beginTransaction();
		try {
			action.accept(session);
			session.getTransaction().commit();
		} catch (RuntimeException e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			session.close();
		}
	}

	private static Provider<Session> newSessionProvider(final SessionFactory sessionFactory) {
		return sessionFactory::openSession;
	}

	private static SessionFactory newSessionFactory() {
		final Configuration configuration = HibernateLoader.getConfiguration(new HashMap<>(), entityList);

//		configuration.setProperty("hibernate.connection.url", "jdbc:h2:mem:test");
		configuration.setProperty("hibernate.connection.url", "jdbc:h2:mem:test;Mode=PostgreSQL;database_to_upper=false");
//		configuration.setProperty("hibernate.connection.url", "jdbc:h2:~/mule.h2.db");
//		configuration.setProperty("hibernate.connection.url", "jdbc:h2:~/mule.h2.db;Mode=PostgreSQL;database_to_upper=false");

		configuration.getProperties().setProperty("hibernate.connection.username", "admin");
		configuration.getProperties().setProperty("hibernate.connection.password", "");

//		configuration.setProperty("hibernate.globally_quoted_identifiers", "true");
		configuration.setProperty("hibernate.connection.driver_class", org.h2.Driver.class.getName());
		configuration.setProperty("hibernate.dialect", org.hibernate.dialect.PostgreSQL10Dialect.class.getName());
		configuration.setProperty("hibernate.hbm2ddl.auto", "create");

		configuration.setPhysicalNamingStrategy(new SnakeCaseNamingStrategy());
//		configuration.setPhysicalNamingStrategy(new H2TestNamingStrategy());

		return configuration.buildSessionFactory();
	}

}