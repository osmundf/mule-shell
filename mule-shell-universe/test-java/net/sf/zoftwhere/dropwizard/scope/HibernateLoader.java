package net.sf.zoftwhere.dropwizard.scope;

import java.util.List;
import java.util.Map;

import net.sf.zoftwhere.hibernate.SnakeCaseNamingStrategy;
import org.hibernate.cfg.Configuration;

public class HibernateLoader {

	public static Configuration getConfiguration(final Map<String, String> propertyMap, List<Class<?>> entityList) {
		final Configuration configuration = new Configuration();
		propertyMap.forEach(configuration::setProperty);
		entityList.forEach(configuration::addAnnotatedClass);
		return configuration;
	}

	static Configuration getH2DatabaseConfiguration(List<Class<?>> entityList) {
		final Configuration configuration = new Configuration();
		entityList.forEach(configuration::addAnnotatedClass);
		configuration.setProperty("hibernate.connection.url",
			"jdbc:h2:mem:test;mode=PostgreSQL;database_to_lower=true");
		configuration.getProperties().setProperty("hibernate.connection.username", "admin");
		configuration.getProperties().setProperty("hibernate.connection.password", "");
		configuration.setProperty("hibernate.connection.driver_class", org.h2.Driver.class.getName());
		configuration.setProperty("hibernate.dialect", org.hibernate.dialect.PostgreSQL10Dialect.class.getName());
		configuration.setProperty("hibernate.hbm2ddl.auto", "update");
		configuration.setPhysicalNamingStrategy(new SnakeCaseNamingStrategy());
		return configuration;
	}
}
