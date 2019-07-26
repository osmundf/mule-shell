package net.sf.zoftwhere.mule.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractLocator;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.UUID;
import java.util.function.Function;

public class AccountLocator extends AbstractLocator<Account, UUID> {

	@Inject
	public AccountLocator(Provider<Session> sessionProvider) {
		super("Account", sessionProvider);
	}

	public Account getByUsername(final String username) {
		Function<Query<Account>, Query<Account>> parameter;
		parameter = query -> query.setParameter("username", username);
		return tryFetchNamedQuery("byUsername", parameter).orElse(null);
	}

	void persistCollection(ShellSession session) {
		currentSession().persist(session);
	}
}
