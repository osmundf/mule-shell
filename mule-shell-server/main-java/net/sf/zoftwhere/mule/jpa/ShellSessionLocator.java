package net.sf.zoftwhere.mule.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractLocator;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import java.util.UUID;

public class ShellSessionLocator extends AbstractLocator<ShellSession, UUID> {

	@Inject
	public ShellSessionLocator(Provider<Session> sessionProvider) {
		super("Shell", sessionProvider);
	}

	public ShellSession newShellSession() {
		return new ShellSession().setName("empty");
	}

	protected Session session() {
		return super.session();
	}

	@Override
	protected ShellSession persist(ShellSession entity) throws HibernateException {
		return super.persist(entity);
	}

	void persistCollection(ShellSession session) {
		currentSession().persist(session);
	}
}
