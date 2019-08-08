package net.sf.zoftwhere.mule.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractLocator;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class SettingLocator extends AbstractLocator<Setting, UUID> {

	@Inject
	public SettingLocator(Provider<Session> sessionProvider) {
		super(sessionProvider);
	}

	public Optional<Setting> getByKey(final String key) {
		Function<Query<Setting>, Setting> parameter;
		parameter = query -> query.setParameter("key", key).getSingleResult();
		return tryFetchNamedQuery("byKey", parameter);
	}
}
