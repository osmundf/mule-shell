package net.sf.zoftwhere.mule.resource;

import com.auth0.jwt.JWTVerifier;
import com.google.common.cache.Cache;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import net.sf.zoftwhere.mule.data.Variable;
import net.sf.zoftwhere.mule.jpa.Account;
import net.sf.zoftwhere.mule.jpa.AccountLocator;
import net.sf.zoftwhere.mule.jpa.AccountRole;
import net.sf.zoftwhere.mule.jpa.AccountRoleLocator;
import net.sf.zoftwhere.mule.jpa.Role;
import net.sf.zoftwhere.mule.jpa.RoleLocator;
import net.sf.zoftwhere.mule.model.RoleModel;
import net.sf.zoftwhere.mule.security.AccountPrincipal;
import net.sf.zoftwhere.mule.security.AccountSigner;
import net.sf.zoftwhere.mule.security.AuthenticationScheme;
import net.sf.zoftwhere.mule.security.StaticSecurityContext;
import net.sf.zoftwhere.text.UTF_8;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountResourceTest extends TestResource<AccountResource> {

	private static final Logger logger = LoggerFactory.getLogger(AccountResourceTest.class);

	private static final Key<Cache<UUID, AccountPrincipal>> loginCacheKey = new Key<>() {};
	private static final Key<Variable<SecurityContext>> securityKey = new Key<>() {};

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
		populateRoles();
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

			final var accountPhase1 = accountLocator.getByUsername(username).orElseThrow();

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

			final var accountPhase2 = accountLocator.getByUsername(username).orElseThrow();

			assertNotNull(login);
			assertNotNull(cache);
			assertNotNull(cached);

			assertNotNull(accountPhase1.getSalt());
			assertNotNull(accountPhase1.getHash());
			assertNotNull(accountPhase2.getSalt());
			assertNotNull(accountPhase2.getHash());

			assertFalse(arraysEqual(accountPhase1.getSalt(), accountPhase2.getSalt()));
			assertFalse(arraysEqual(accountPhase1.getHash(), accountPhase2.getHash()));

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
		final var interchange = guiceInjector.getInstance(securityKey);
		interchange.accept(StaticSecurityContext.withBuilder().secure(true).build());

		createAccount("osmundf", "osmund.francis@gmail.com", "123456", RoleModel.ADMIN);
	}

	@Test
	void testLogoutSuccess() {
		registerTestAccountGroup();
	}

	@Test
	void testNewAccountRegistration() {
		final var accountLocator = guiceInjector.getInstance(AccountLocator.class);
		final var accountRoleLocator = guiceInjector.getInstance(AccountRoleLocator.class);
		final var securityVariable = guiceInjector.getInstance(securityKey);

		for (var e : accountSecretMap.entrySet()) {
			final var username = e.getKey();
			final var password = e.getValue();
			final var email = accountEmailMap.get(username);

			assertNotNull(username);
			assertNotNull(password);
			assertNotNull(email);

			createAccount(username, email, password, RoleModel.REGISTER);

			final var account = accountLocator.getByUsername(username).orElseThrow();
			final var accountRoleList = accountRoleLocator.getForAccount(account);

			assertNotNull(account);
			assertNotNull(accountRoleList);
			assertEquals((1), accountRoleList.size());

			final var accountRole = accountRoleList.get(0);

			assertEquals(RoleModel.REGISTER.name(), accountRole.getRole().getName());
		}

		for (var e : accountSecretMap.entrySet()) {

			final var username = e.getKey();
			final var password = e.getValue();
			final var role = RoleModel.REGISTER.name();

			final var principal = new AccountPrincipal(username, role);
			final var securityContext = StaticSecurityContext.withBuilder()
					.secure(true)
					.authenticationScheme(AuthenticationScheme.BEARER)
					.role(role)
					.userPrincipal(principal).build();

			securityVariable.accept(securityContext);

			final var reset = resource.reset(username, password);
			final var status = reset.getStatus();

			assertNotNull(reset);
			assertEquals(Status.OK.getStatusCode(), status);

			final var account = accountLocator.getByUsername(username).orElseThrow();
			final var accountRoleList = accountRoleLocator.getForAccount(account);

			assertNotNull(account);
			assertNotNull(accountRoleList);
			assertEquals((1), accountRoleList.size());

			final var accountRole = accountRoleList.get(0);

			assertEquals(RoleModel.CLIENT.name(), accountRole.getRole().getName());
		}
	}

	@Test
	void registerAccountRoles() {
		final var key = RoleModel.CLIENT.getClass().getName();
	}

	private void populateRoles() {
		final Map<String, Integer> priority = new Builder<String, Integer>()
				.put(RoleModel.SYSTEM.name(), 100)
				.put(RoleModel.ADMIN.name(), 80)
				.put(RoleModel.CLIENT.name(), 60)
				.put(RoleModel.REGISTER.name(), 40)
				.build();
		final var roleModelArray = RoleModel.values();
		for (var roleModel : roleModelArray) {
			wrapConsumer(session -> {
				final var key = Role.getKey(roleModel);
				final var name = roleModel.name();
				final var value = name.toLowerCase();
				final var priorityValue = priority.get(name);
				final var role = new Role(key, name, value, priorityValue);

				session.beginTransaction();
				session.persist(role);
				session.getTransaction().commit();
			});
		}

		final var count = wrapFunction(session -> {
			final var select = "select count(o) from Role o where o.deletedAt is null";
			final var query = session.createQuery(select, Long.class);
			return query.getSingleResult();
		}).orElse(0L);

		assertNotNull(count);
		assertEquals((long) roleModelArray.length, (long) count, "");
	}

	private void registerTestAccountGroup() {
		for (var e : accountSecretMap.entrySet()) {
			final var username = e.getKey();
			final var password = e.getValue();
			final var email = accountEmailMap.get(username);

			assertNotNull(username);
			assertNotNull(password);
			assertNotNull(email);

			createAccount(username, email, password, RoleModel.CLIENT);
		}
	}

	private void createAccount(String username, String emailAddress, String password, RoleModel roleModel) {
		final var accountSignerProvider = guiceInjector.getProvider(AccountSigner.class);
		final var accountLocator = guiceInjector.getInstance(AccountLocator.class);
		final var accountRoleLocator = guiceInjector.getInstance(AccountRoleLocator.class);
		final var roleLocator = guiceInjector.getInstance(RoleLocator.class);

		final var digest = accountSignerProvider.get();
		final var data = password.getBytes(StandardCharsets.UTF_8);

		assertTrue(UTF_8.codePointCount(data) >= digest.getMinimumPasswordLength());

		// Ensure no other account with the same username.
		final var first = tryFetchEntity(username, Optional::of, accountLocator::getByUsername).orElse(null);
		assertNull(first);

		// Save new account.
		wrapConsumer(session -> {
			final var account = new Account(username, emailAddress).updateHash(digest, data);

			session.beginTransaction();
			session.save(account);
			session.getTransaction().commit();
		});

		// Ensure account was saved.
		final var account = tryFetchEntity(username, Optional::of, accountLocator::getByUsername).orElse(null);
		assertNotNull(account);

		// Add with role.
		final var role = roleLocator.getByKey(Role.getKey(roleModel)).orElseThrow();
		final var accountRole = new AccountRole(account, role, role.getValue());

		wrapConsumer(session -> {
			session.beginTransaction();
			session.save(accountRole);
			session.getTransaction().commit();
		});

		final var accountRoleList = accountRoleLocator.getForAccount(account);

		assertEquals((1), accountRoleList.size());

		final var currentAccountRole = accountRoleList.get(0);

		assertEquals(roleModel.name(), currentAccountRole.getRole().getName());
	}

	/**
	 * Checks to arrays for equality.
	 *
	 * @param a1 Array left.
	 * @param a2 Array right.
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
