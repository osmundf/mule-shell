package net.sf.zoftwhere.mule.resource;

import com.auth0.jwt.JWTVerifier;
import com.google.common.cache.Cache;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import net.sf.zoftwhere.mule.jpa.Account;
import net.sf.zoftwhere.mule.jpa.AccountLocator;
import net.sf.zoftwhere.mule.security.AccountPrincipal;
import net.sf.zoftwhere.mule.security.AccountSigner;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.ImmutableMap.Builder;
import static com.google.common.collect.ImmutableMap.Entry;
import static net.sf.zoftwhere.dropwizard.AbstractLocator.tryFetchEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AccountResourceTest extends TestResource<AccountResource> {

	private static final Logger logger = LoggerFactory.getLogger(AccountResourceTest.class);

	private static final Key<Cache<UUID, AccountPrincipal>> loginCacheKey = new Key<>() {};

	private Map<String, String> accountSecretMap = new Builder<String, String>()
			.put("test-bob", "test-bob-public-secret")
			.put("test-cat", "test-cat-public-secret")
			.put("test-dot", "test-dot-public-secret")
			.put("test-egg", "test-egg-public-secret")
			.build();

	private final AccountResource resource;

	private final Injector guiceInjector;

	private final AccountLocator accountLocator;

	AccountResourceTest() {
		super(AccountResource::new);
		guiceInjector = super.getGuiceInjector();
		resource = super.getResource();

		// accountLocator = guiceInjector.getInstance(AccountLocator.class);
		final var sp = guiceInjector.getProvider(Session.class);
		accountLocator = new AccountLocator(sp);
	}

	@BeforeEach
	void prepare() {
	}

	@AfterEach
	void tearDown() {
		// Close the session factory when we are done.
		try {
			super.close();
		} catch (Exception e) {
			logger.warn("There was an exception while closing.", e);
		}
	}

	@Test
	void testLoginSuccess() {
		final var cacheProvider = guiceInjector.getProvider(loginCacheKey);
		final var jwtVerifier = guiceInjector.getInstance(JWTVerifier.class);
		final var cache = cacheProvider.get();

		registerUser("test-bob", "bob@test.test", "test-bob-public-secret");
		registerUser("test-cat", "cat@test.test", "test-cat-public-secret");
		registerUser("test-dot", "dot@test.test", "test-dot-public-secret");
		registerUser("test-egg", "egg@test.test", "test-egg-public-secret");

		for (Entry<String, String> secretEntry : accountSecretMap.entrySet()) {
			final var username = secretEntry.getKey();
			final var password = secretEntry.getValue();

			final var scheme = "basic";
			final var credentials = (username + ":" + password).getBytes(StandardCharsets.UTF_8);
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
			assertEquals(username, cached.getUsername().orElse(null), "Username");
			assertEquals("CLIENT", cached.getRole().orElse(null), "Role");
		}
	}

	@Test
	void testLoginFail() {
		registerUser("test-bob", "bob@test.test", "test-bob-public-secret");
		registerUser("test-cat", "cat@test.test", "test-cat-public-secret");
		registerUser("test-dot", "dot@test.test", "test-dot-public-secret");
		registerUser("test-egg", "egg@test.test", "test-egg-public-secret");

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
	void testLogoutFailure() {
		registerUser("test-bob", "bob@test.test", "test-bob-public-secret");
		registerUser("test-cat", "cat@test.test", "test-cat-public-secret");
		registerUser("test-dot", "dot@test.test", "test-dot-public-secret");
		registerUser("test-egg", "egg@test.test", "test-egg-public-secret");
	}

	@Test
	void testLogoutSuccess() {
		registerUser("test-bob", "bob@test.test", "test-bob-public-secret");
		registerUser("test-cat", "cat@test.test", "test-cat-public-secret");
		registerUser("test-dot", "dot@test.test", "test-dot-public-secret");
		registerUser("test-egg", "egg@test.test", "test-egg-public-secret");
	}

	private void registerUser(String username, String emailAddress, String password) {
		Provider<AccountSigner> accountSignerProvider = guiceInjector.getProvider(AccountSigner.class);

		final var digest = accountSignerProvider.get();
		final var salt = digest.generateSalt(512);
		final var data = password.getBytes(StandardCharsets.UTF_8);
		final var hash = digest.getHash(salt, data);

		final var first = tryFetchEntity(username, Optional::of, accountLocator::getByUsername).orElse(null);
		assertNull(first);

		wrapTransaction(session -> {
			Account account = new Account();
			account.setUsername(username);
			account.setEmailAddress(emailAddress);
			account.setSalt(salt);
			account.setHash(hash);
			session.persist(account);
		});

		final var account = tryFetchEntity(username, Optional::of, accountLocator::getByUsername).orElse(null);
		assertNotNull(account);
	}
}
