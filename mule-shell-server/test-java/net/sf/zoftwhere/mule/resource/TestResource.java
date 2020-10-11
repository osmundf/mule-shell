package net.sf.zoftwhere.mule.resource;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import com.google.inject.Injector;
import com.google.inject.Provider;
import lombok.Getter;
import net.sf.zoftwhere.dropwizard.AbstractResource;
import net.sf.zoftwhere.dropwizard.TestInjection;
import net.sf.zoftwhere.mule.MuleApplication;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.sf.zoftwhere.hibernate.HibernateLoader.getH2DatabaseConfiguration;

public abstract class TestResource<TestClass extends AbstractResource> extends AbstractResource
	implements AutoCloseable, Closeable
{

	private static final Logger logger = LoggerFactory.getLogger(AbstractResource.class);

	private static final List<Class<?>> entityList = List.of(MuleApplication.persistenceEntities());

	private final SessionFactory sessionFactory;

	@Getter
	private final Injector guiceInjector;

	@Getter
	private final TestClass resource;

	TestResource(Function<Provider<Session>, TestClass> constructor) {
		this(constructor, new TestConfiguration(getH2DatabaseConfiguration(entityList)));
	}

	private TestResource(Function<Provider<Session>, TestClass> constructor, TestConfiguration configuration) {
		super(configuration.sessionProvider);
		this.sessionFactory = configuration.sessionFactory;
		this.guiceInjector = configuration.guiceInjector;
		this.resource = constructor.apply(configuration.sessionProvider);
		guiceInjector.injectMembers(resource);
	}

	@Override
	public void close() throws IOException {
		try {
			sessionFactory.close();
		}
		catch (HibernateException e) {
			logger.warn("Exception occurred.", e);
			throw new IOException(e);
		}
	}

	public static class TestConfiguration {
		private final SessionFactory sessionFactory;
		private final Provider<Session> sessionProvider;
		private final Injector guiceInjector;

		@SuppressWarnings("WeakerAccess")
		public TestConfiguration(Configuration configuration) {
			this.sessionFactory = configuration.buildSessionFactory();
			this.sessionProvider = sessionFactory::openSession;
			this.guiceInjector = TestInjection.newTestGuiceInjector(sessionFactory);
		}
	}
}
