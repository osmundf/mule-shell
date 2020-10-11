package net.sf.zoftwhere.mule.resource;

import java.util.Map;
import java.util.UUID;

import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.google.inject.Key;
import net.sf.zoftwhere.mule.jpa.AccountLocator;
import net.sf.zoftwhere.mule.security.AccountPrincipal;
import net.sf.zoftwhere.mule.shell.MuleShell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SessionResourceTest extends TestResource<SessionResource> {

	private static final Logger logger = LoggerFactory.getLogger(SessionResourceTest.class);

	private static final Key<Cache<UUID, AccountPrincipal>> principalCacheKey = new Key<>() { };

	private static final Key<Cache<UUID, MuleShell>> shellCacheKey = new Key<>() { };

	private Map<String, String> accountSecretMap = new ImmutableMap.Builder<String, String>()
		.put("test-bob", "test-bob-public-secret")
		.put("test-cat", "test-cat-public-secret")
		.put("test-dot", "test-dot-public-secret")
		.put("test-egg", "test-egg-public-secret")
		.build();

	private final SessionResource resource;

	private final Injector guiceInjector;

	private final AccountLocator accountLocator;

	SessionResourceTest() {
		super(SessionResource::new);
		guiceInjector = super.getGuiceInjector();
		resource = super.getResource();
		accountLocator = guiceInjector.getInstance(AccountLocator.class);
	}

	@BeforeEach
	void prepare() {
	}

	@AfterEach
	void tearDown() {
		// Close the session factory when we are done.
		try {
			super.close();
		}
		catch (Exception e) {
			logger.warn("There was an exception while closing.", e);
		}
	}
}
