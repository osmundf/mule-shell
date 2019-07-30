package net.sf.zoftwhere.mule.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractLocator;
import net.sf.zoftwhere.mule.model.RoleModel;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class AccountRoleLocator extends AbstractLocator<AccountRole, UUID> {

	@Inject
	public AccountRoleLocator(Provider<Session> sessionProvider) {
		super(sessionProvider);
	}

	public AccountRole getByIdId(Account account, Role role) {
		Function<Query<AccountRole>, AccountRole> parameter;
		parameter = query -> query
				.setParameter("accountId", account.getId())
				.setParameter("roleId", role.getId())
				.getSingleResult();
		return tryFetchNamedQuery("byIdId", parameter).orElse(null);
	}

	public List<AccountRole> getForAccount(Account account) {
		Function<Query<AccountRole>, Query<AccountRole>> parameter;
		parameter = query -> query.setParameter("accountId", account.getId());
		return tryFetchResult("byAccountId", parameter, AccountRole.class).orElse(null);

	}

	public AccountRole getByRoleName(final Account account, final RoleModel role) {
		final var roleName = role.name();
		Function<Query<AccountRole>, AccountRole> parameter;
		parameter = query -> query
				.setParameter("accountId", account.getId())
				.setParameter("roleName", roleName)
				.getSingleResult();
		return tryFetchNamedQuery("byAccountAndRoleName", parameter).orElse(null);
	}

	public AccountRole getByKey(final Account account, final RoleModel role) {
		final var packageName = role.getClass().getPackage().getName();
		final var enumName = role.name();
		final var key = packageName + ":" + enumName;
		return getByKey(account, key);
	}

	public AccountRole getByKey(final Account account, final String key) {
		Function<Query<AccountRole>, AccountRole> parameter;
		parameter = query -> query
				.setParameter("accountId", account.getId())
				.setParameter("key", key)
				.getSingleResult();
		return tryFetchNamedQuery("byAccountAndKey", parameter).orElse(null);
	}

	void persistCollection(ShellSession session) {
		currentSession().persist(session);
	}
}
