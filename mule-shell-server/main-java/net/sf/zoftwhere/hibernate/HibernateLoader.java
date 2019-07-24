package net.sf.zoftwhere.hibernate;

import org.hibernate.cfg.Configuration;

import java.util.List;
import java.util.Map;

public class HibernateLoader {

	public static Configuration getConfiguration(final Map<String, String> propertyMap, List<Class<?>> entityList) {
		Configuration configuration = new Configuration();
		propertyMap.forEach(configuration::setProperty);
		entityList.forEach(configuration::addAnnotatedClass);
		return configuration;
	}
}
