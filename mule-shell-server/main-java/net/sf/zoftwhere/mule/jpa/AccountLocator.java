package net.sf.zoftwhere.mule.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractLocator;
import org.hibernate.Session;

import java.util.UUID;

public class AccountLocator extends AbstractLocator<Account, UUID> {

	@Inject
	public AccountLocator(Provider<Session> sessionProvider) {
		super("Account", sessionProvider);
	}

	public Account newAccount(final String userName, final String emailAddress) {
		final var user = new Account();
		user.setUserName(userName);
		user.setEmailAddress(emailAddress);
		session().persist(user);
		return user;
	}

	void persistCollection(ShellSession session) {
		currentSession().persist(session);
	}
}
