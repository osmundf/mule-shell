package net.sf.zoftwhere.dropwizard.scope;

import java.util.ArrayList;

import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractResource;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AbstractResourceTest {

	@Test
	void testRoleScope() {
		assertNotNull(AbstractResource.ADMIN_ROLE);
		assertNotNull(AbstractResource.CLIENT_ROLE);
		assertNotNull(AbstractResource.GUEST_ROLE);
		assertNotNull(AbstractResource.REGISTER_ROLE);
		assertNotNull(AbstractResource.CLIENT_ROLE);
		assertNotNull(AbstractResource.SYSTEM_ROLE);
	}

	@Test
	void testWrapperScope() {
		var list = new ArrayList<Class<?>>();
		var configuration = HibernateLoader.getH2DatabaseConfiguration(list);
		var sessionFactory = configuration.buildSessionFactory();
		var provider = (Provider<Session>) sessionFactory::openSession;
		var instance = new AbstractResourceScopeTest(provider);
		instance.checkMethods();
	}

	private static class AbstractResourceScopeTest extends AbstractResource {

		AbstractResourceScopeTest(Provider<Session> sessionProvider) {
			super(sessionProvider);
		}

		void checkMethods() {
			super.wrapSession(session -> {});
			var e = entityNotFound("TestEntity", "0");
			var message = e.getMessage();
			assertEquals(message, "Could not find TestEntity entity with id (0).");
		}
	}
}
