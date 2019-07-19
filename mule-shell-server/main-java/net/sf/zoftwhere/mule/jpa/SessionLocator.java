package net.sf.zoftwhere.mule.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractLocator;

public class SessionLocator extends AbstractLocator<ShellSession, Integer> {

	@Inject
	public SessionLocator(Provider<org.hibernate.Session> sessionProvider) {
		super(sessionProvider);
	}

	void persistCollection(ShellSession session) {
		currentSession().persist(session);
	}
}
