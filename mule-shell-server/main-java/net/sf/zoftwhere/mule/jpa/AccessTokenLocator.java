package net.sf.zoftwhere.mule.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractLocator;
import org.hibernate.Session;

import java.util.UUID;

public class AccessTokenLocator extends AbstractLocator<AccessToken, UUID> {

	@Inject
	public AccessTokenLocator(Provider<Session> sessionProvider) {
		super(sessionProvider);
	}

	void persistCollection(ShellSession session) {
		currentSession().persist(session);
	}
}
