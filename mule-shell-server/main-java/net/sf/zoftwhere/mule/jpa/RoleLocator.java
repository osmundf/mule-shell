package net.sf.zoftwhere.mule.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractLocator;
import net.sf.zoftwhere.mule.model.RoleModel;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.UUID;
import java.util.function.Function;

public class RoleLocator extends AbstractLocator<Role, UUID> {

	@Inject
	public RoleLocator(Provider<Session> sessionProvider) {
		super(sessionProvider);
	}

	public Role getByKey(final RoleModel role) {
		final var packageName = role.getClass().getPackage().getName();
		final var enumName = role.name();
		final var key = packageName + ":" + enumName;
		return getByKey(key);
	}

	public Role getByKey(final String key) {
		Function<Query<Role>, Role> parameter;
		// Deliberately get only any one result (allow rolling on access roles).
		parameter = query -> query.setParameter("key", key)
				.setFetchSize(1).getResultList().get(0);
		return tryFetchNamedQuery("byKey", parameter).orElse(null);
	}

	void persistCollection(ShellSession session) {
		currentSession().persist(session);
	}
}
