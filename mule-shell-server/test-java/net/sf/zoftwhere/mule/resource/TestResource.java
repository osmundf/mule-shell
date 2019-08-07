package net.sf.zoftwhere.mule.resource;

import com.google.inject.Injector;
import com.google.inject.Provider;
import lombok.Getter;
import net.sf.zoftwhere.dropwizard.AbstractEntity;
import net.sf.zoftwhere.dropwizard.AbstractResource;
import net.sf.zoftwhere.dropwizard.TestInjection;
import net.sf.zoftwhere.hibernate.TransactionalSession;
import net.sf.zoftwhere.mule.MuleApplication;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.sf.zoftwhere.hibernate.HibernateLoader.getH2DatabaseConfiguration;

public abstract class TestResource<TestClass extends AbstractResource> implements AutoCloseable, Closeable {

	private static final Logger logger = LoggerFactory.getLogger(AbstractResource.class);

	private static final List<Class<?>> entityList = List.of(MuleApplication.persistenceEntities());

	private final SessionFactory sessionFactory;

	private final Provider<Session> sessionProvider;

	@Getter
	private final Injector guiceInjector;

	@Getter
	private final TestClass resource;

	TestResource(Function<Provider<Session>, TestClass> constructor) {
		final var configuration = getH2DatabaseConfiguration(entityList);
		this.sessionFactory = configuration.buildSessionFactory();
		this.sessionProvider = sessionFactory::openSession;
		this.guiceInjector = TestInjection.newTestGuiceInjector(sessionFactory);
		this.resource = constructor.apply(sessionProvider);
		guiceInjector.injectMembers(resource);
	}

	@Override
	public void close() throws IOException {
		try {
			sessionFactory.close();
		} catch (HibernateException e) {
			logger.warn("Exception occurred.", e);
			throw new IOException(e);
		}
	}

	<E> Optional<E> wrapFunction(Function<Session, E> function) {
		return TransactionalSession.wrapSession(sessionProvider, function);
	}

	<T extends AbstractEntity<?>> void persistCollection(T entity) {
		TransactionalSession.wrapSession(sessionProvider, session -> {
			session.beginTransaction();
			session.persist(entity);
			session.getTransaction().commit();
		});
	}

	<T extends AbstractEntity<?>> void saveEntity(T entity) {
		TransactionalSession.wrapSession(sessionProvider, session -> {
			session.beginTransaction();
			session.save(entity);
			session.getTransaction().commit();
		});
	}

	<T extends AbstractEntity<?>> void updateEntity(T entity) {
		TransactionalSession.wrapSession(sessionProvider, session -> {
			session.beginTransaction();
			session.update(entity);
			session.getTransaction().commit();
		});
	}
}
