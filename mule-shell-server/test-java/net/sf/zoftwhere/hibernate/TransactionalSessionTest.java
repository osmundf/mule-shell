package net.sf.zoftwhere.hibernate;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.inject.Provider;
import net.sf.zoftwhere.mule.MuleApplication;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionalSessionTest implements TransactionalSession {

	private static final Logger logger = LoggerFactory.getLogger(TransactionalSessionTest.class);

	private static final List<Class<?>> entityList = List.of(MuleApplication.persistenceEntities());

	private final SessionFactory sessionFactory;

	private final Provider<Session> sessionProvider;

	TransactionalSessionTest() {
		final var configuration = HibernateLoader.getH2DatabaseConfiguration(entityList);
		configuration.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "managed");
		this.sessionFactory = configuration.buildSessionFactory();
		this.sessionProvider = sessionFactory::openSession;
	}

	@AfterEach
	void tearDown() {
		try {
			sessionFactory.close();
		}
		catch (RuntimeException e) {
			logger.warn("Exception while closing session factory.", e);
		}
	}

	@Test
	void testConsumer() {
		wrapConsumer(session -> {
			session.beginTransaction();
			session.getTransaction().commit();
		});
	}

	@Test
	void testFunction() {
		final Optional<String> result = wrapFunction(session -> {
			session.beginTransaction();
			session.flush();
			session.getTransaction().commit();
			return null;
		});

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	private void wrapConsumer(Consumer<Session> consumer) {
		TransactionalSession.wrapSession(sessionProvider, consumer);
	}

	private <E> Optional<E> wrapFunction(Function<Session, E> function) {
		return TransactionalSession.wrapSession(sessionProvider, function);
	}
}
