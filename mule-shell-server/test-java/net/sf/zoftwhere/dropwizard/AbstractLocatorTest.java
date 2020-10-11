package net.sf.zoftwhere.dropwizard;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.inject.Injector;
import net.sf.zoftwhere.mule.MuleApplication;
import net.sf.zoftwhere.mule.jpa.Account;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.sf.zoftwhere.hibernate.HibernateLoader.getH2DatabaseConfiguration;
import static org.junit.jupiter.api.Assertions.fail;

class AbstractLocatorTest {

	private static final Logger logger = LoggerFactory.getLogger(AbstractLocatorTest.class);

	private static final List<Class<?>> entityList = List.of(MuleApplication.persistenceEntities());

	private final SessionFactory sessionFactory;

	private final Injector guiceInjector;

	AbstractLocatorTest() {
		final var configuration = getH2DatabaseConfiguration(entityList);
		this.sessionFactory = configuration.buildSessionFactory();
		this.guiceInjector = TestInjection.newTestGuiceInjector(sessionFactory);
	}

	@AfterEach
	void tearDown() {
		// Close the session factory when we are done.
		try {
			sessionFactory.close();
		}
		catch (HibernateException e) {
			logger.warn("Exception occurred.", e);
		}
	}

	@Test
	void testNamedQuery() {
		final var provider = guiceInjector.getProvider(Session.class);
		final var locator = new AbstractLocator<Object, UUID>(provider) { };
		final var subName = "doesNotExist";

		try {
			final var e = locator.tryFetchNamedQuery(subName, accountQuery -> null);
			fail("Try Fetch should throw an exception for all but NoResultException");
		}
		catch (Exception ignored) {
		}

		try {
			final var e = locator.tryFetchSingleResult(subName, accountQuery -> Optional.empty(), Account.class);
			fail("Try Fetch should throw an exception for all but NoResultException");
		}
		catch (Exception ignored) {
		}

		try {
			final var e = locator.tryFetchResult(subName, accountQuery -> accountQuery, Account.class);
			fail("Try Fetch should throw an exception for all but NoResultException");
		}
		catch (Exception ignored) {
		}
	}
}
