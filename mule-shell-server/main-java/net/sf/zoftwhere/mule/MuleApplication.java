package net.sf.zoftwhere.mule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sf.zoftwhere.dropwizard.DatabaseConfiguration;
import net.sf.zoftwhere.mule.jpa.Letter;
import net.sf.zoftwhere.mule.resource.*;
import net.sf.zoftwhere.mule.shell.JShellManager;
import org.dom4j.tree.AbstractEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ru.vyarus.dropwizard.guice.GuiceBundle;

public class MuleApplication extends Application<MuleConfiguration> {

	private final HibernateBundle<MuleConfiguration> hibernateBundle = getHibernateBundle();

	public static void main(String[] args) throws Exception {
		new MuleApplication().run(args);
	}

	public static <T extends DatabaseConfiguration> HibernateBundle<T> getHibernateBundle() {
		return new HibernateBundle<>(AbstractEntity.class, persistenceEntities()) {
			@Override
			public DataSourceFactory getDataSourceFactory(T configuration) {
				return configuration.getDataSourceFactory();
			}

			@Override
			protected void configure(org.hibernate.cfg.Configuration configuration) {
				for (Class<?> clazz : persistenceEntities()) {
					configuration.addAnnotatedClass(clazz);
				}

				configuration.setProperty("hibernate.hbm2ddl.auto", "update");
			}
		};
	}

	public static Class<?>[] persistenceEntities() {
		return new Class<?>[]{
				Letter.class
		};
	}

	@Override
	public String getName() {
		return "mule-shell-server";
	}

	@Override
	public void initialize(Bootstrap<MuleConfiguration> bootstrap) {
		// Enable variable substitution with environment variables
		bootstrap.setConfigurationSourceProvider(
				new SubstitutingSourceProvider(
						bootstrap.getConfigurationSourceProvider(),
						new EnvironmentVariableSubstitutor(false)
				)
		);

		bootstrap.addBundle(hibernateBundle);

		GuiceBundle<MuleConfiguration> guiceBundle = this.<MuleConfiguration>setupGuice()
				.modules(muleModule())
				.build();

		bootstrap.addBundle(guiceBundle);
	}

	public <T extends Configuration> GuiceBundle.Builder<T> setupGuice() {
		return GuiceBundle.<T>builder().enableAutoConfig(getClass().getPackage().getName());
	}

	private AbstractModule muleModule() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(SessionFactory.class).toProvider(hibernateBundle::getSessionFactory).in(Singleton.class);
				bind(Session.class).toProvider(() -> hibernateBundle.getSessionFactory().openSession());
			}

			@Provides
			public ObjectMapper getObjectMapper(Environment environment) {
				return environment.getObjectMapper();
			}

			@Provides
			@Singleton
			public JShellManager getJShell() {
				return new JShellManager();
			}
		};
	}

	@Override
	public void run(MuleConfiguration configuration, Environment environment) {
		environment.jersey().register(AssetResource.class);
		environment.jersey().register(ExpressionResource.class);
		environment.jersey().register(HelloWorldResource.class);
		environment.jersey().register(LetterResource.class);
	}
}