package net.sf.zoftwhere.mule.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractLocator;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class ShellSessionLocator extends AbstractLocator<ShellSession, UUID> {

	@Inject
	public ShellSessionLocator(Provider<Session> sessionProvider) {
		super(sessionProvider);
	}

	public Optional<ShellSession> getForIdAndAccount(UUID id, Account account) {
		Function<Query<ShellSession>, ShellSession> parameter;
		parameter = query -> {
			query.setParameter("id", id);
			query.setParameter("accountId", account.getId());
			return query.getSingleResult();
		};

		return tryFetchNamedQuery("byIdAndAccountId", parameter);
	}

	public List<ShellSession> getForAccount(Account account) {
		Function<Query<ShellSession>, Query<ShellSession>> parameter;
		parameter = query -> query.setParameter("accountId", account.getId());
		return tryFetchResult("byAccountId", parameter, ShellSession.class).orElse(new ArrayList<>());
	}
}
