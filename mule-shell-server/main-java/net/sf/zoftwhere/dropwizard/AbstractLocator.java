package net.sf.zoftwhere.dropwizard;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.dropwizard.hibernate.AbstractDAO;
import net.sf.zoftwhere.mule.proxy.EmptyInterface;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.Serializable;
import java.util.Optional;

public class AbstractLocator<E, I extends Serializable> extends AbstractDAO<E> {

	private final Provider<Session> sessionProvider;

	@Inject
	public AbstractLocator(Provider<Session> sessionProvider) {
		super(EmptyInterface.create(
				AbstractDAO.class.getClassLoader(),
				new Class<?>[]{SessionFactory.class},
				new NullPointerException("Do not use session factory here.")
		));
		this.sessionProvider = sessionProvider;
	}

	public Optional<E> getById(I id) {
		return Optional.of(super.get(id));
	}

	protected Session session() {
		return sessionProvider.get();
	}
	
	@Override
	protected Session currentSession() {
		return sessionProvider.get();
	}
}
