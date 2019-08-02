package net.sf.zoftwhere.mule.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractLocator;
import net.sf.zoftwhere.mule.model.RoleModel;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class RoleLocator extends AbstractLocator<Role, UUID> {

	@Inject
	public RoleLocator(Provider<Session> sessionProvider) {
		super(sessionProvider);
	}

	public Optional<Role> getByKey(final RoleModel role) {
		final var packageName = role.getClass().getPackage().getName();
		final var enumName = role.name();
		final var key = packageName + ":" + enumName;
		return getByKey(key);
	}

	public Optional<Role> getByKey(final String key) {
		Function<Query<Role>, Role> parameter;

		// Deliberately get only any one result (allow rolling on access roles).
		parameter = query -> {
			List<Role> list = query.setParameter("key", key).setFetchSize(1).getResultList();
			return list != null && list.size() > 0 ? list.get(0) : null;
		};

		return tryFetchNamedQuery("byKey", parameter);
	}

	void persistCollection(ShellSession session) {
		currentSession().persist(session);
	}
}
