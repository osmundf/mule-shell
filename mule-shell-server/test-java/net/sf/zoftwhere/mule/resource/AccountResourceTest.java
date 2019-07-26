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
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountResourceTest extends TestResource<AccountResource> {

	private static final Logger logger = LoggerFactory.getLogger(AccountResourceTest.class);

	private static final Key<Cache<UUID, AccountPrincipal>> loginCacheKey = new Key<>() {};

	private static final Map<String, String> accountSecretMap = new Builder<String, String>()
			.put("test-bob", "test-bob-public-secret")
			.put("test-cat", "test-cat-public-secret")
			.put("test-dot", "test-dot-public-secret")
			.put("test-egg", "test-egg-public-secret")
			.build();

	private static final Map<String, String> accountEmailMap = new Builder<String, String>()
			.put("test-bob", "bob@test.test")
			.put("test-cat", "cat@test.test")
			.put("test-dot", "dot@test.test")
			.put("test-egg", "egg@test.test")
			.build();

	private final AccountResource resource;

	private final Injector guiceInjector;

	AccountResourceTest() {
		super(AccountResource::new);
		resource = super.getResource();
		guiceInjector = super.getGuiceInjector();
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
		final var accountLocator = guiceInjector.getInstance(AccountLocator.class);
		final var cacheProvider = guiceInjector.getProvider(loginCacheKey);
		final var jwtVerifier = guiceInjector.getInstance(JWTVerifier.class);
		final var cache = cacheProvider.get();

		registerTestAccountGroup();

		for (Entry<String, String> secretEntry : accountSecretMap.entrySet()) {
			final var username = secretEntry.getKey();
			final var password = secretEntry.getValue();

			final var accountPhase1 = accountLocator.getByUsername(username);

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

			final var accountPhase2 = accountLocator.getByUsername(username);

			assertNotNull(login);
			assertNotNull(cache);
			assertNotNull(cached);

			assertNotNull(accountPhase1.getSalt());
			assertNotNull(accountPhase1.getHash());
			assertNotNull(accountPhase2.getSalt());
			assertNotNull(accountPhase2.getHash());

			assertTrue(!arraysEqual(accountPhase1.getSalt(), accountPhase2.getSalt()));
			assertTrue(!arraysEqual(accountPhase1.getHash(), accountPhase2.getHash()));

			assertEquals(username, cached.getUsername().orElse(null), "Username");
			assertEquals("CLIENT", cached.getRole().orElse(null), "Role");
		}
	}

	@Test
	void testLoginFail() {
		final var scheme = "basic";
		final var openJoin = new String[]{
				"fake:thisPasswordIsLongEnough",
				"test:2tiny",
				"test:wrongPassword",
				"spammer:This Password Is Long Enough To Cause The Encoder To Take Longer Than Needed To Decode And Check, " +
						"so the limit is set to a feasible limit to stop spamming of long authorization headers.",
		};

		registerTestAccountGroup();

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
		registerTestAccountGroup();
	}

	@Test
	void testLogoutSuccess() {
		registerTestAccountGroup();
	}

	private void registerTestAccountGroup() {
		for (var e : accountSecretMap.entrySet()) {
			final var username = e.getKey();
			final var password = e.getValue();
			final var email = accountEmailMap.get(username);

			assertNotNull(username);
			assertNotNull(password);
			assertNotNull(email);

			registerAccount(username, email, password);
		}
	}

	private void registerAccount(String username, String emailAddress, String password) {
		final var accountSignerProvider = guiceInjector.getProvider(AccountSigner.class);
		final var accountLocator = guiceInjector.getInstance(AccountLocator.class);

		final var digest = accountSignerProvider.get();
		final var salt = digest.generateSalt(512);
		final var data = password.getBytes(StandardCharsets.UTF_8);
		final var hash = digest.getHash(salt, data);

		final var first = tryFetchEntity(username, Optional::of, accountLocator::getByUsername).orElse(null);
		assertNull(first);

		wrapSession(session -> {
			session.beginTransaction();
			Account account = new Account();
			account.setUsername(username);
			account.setEmailAddress(emailAddress);
			account.setSalt(salt);
			account.setHash(hash);
			session.persist(account);
			session.getTransaction().commit();
		});

		final var account = tryFetchEntity(username, Optional::of, accountLocator::getByUsername).orElse(null);
		assertNotNull(account);
	}

	/**
	 * Checks to arrays for equality.
	 *
	 * @param a1
	 * @param a2
	 * @return Returns true if both the arrays contain the same data, false otherwise.
	 */
	private boolean arraysEqual(byte[] a1, byte[] a2) {
		if (a1 == a2) {
			return true;
		}

		if (a1 == null || a2 == null || a1.length != a2.length) {
			return false;
		}

		for (int i = 0, size = a1.length; i < size; i++) {
			if (a1[i] != a2[i]) {
				return false;
			}
		}

		return true;
	}
}
