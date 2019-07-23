package net.sf.zoftwhere.dropwizard;

import com.google.inject.Provider;
import io.dropwizard.hibernate.AbstractDAO;
import net.sf.zoftwhere.mule.proxy.EmptyInterface;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.io.Serializable;

public class AbstractLocator<E, I extends Serializable> extends AbstractDAO<E> {

	private final Provider<Session> sessionProvider;

	private final String prefix;

	public AbstractLocator(final String prefix, final Provider<Session> sessionProvider) {
		super(EmptyInterface.create(
				AbstractDAO.class.getClassLoader(),
				new Class<?>[]{SessionFactory.class},
				new NullPointerException("Do not use session factory here.")
		));

		this.prefix = prefix;
		this.sessionProvider = sessionProvider;
	}

	public E getById(I id) {
		return super.get(id);
	}

	protected Session session() {
		return sessionProvider.get();
	}

	@Override
	protected Query<E> namedQuery(String subQueryName) throws HibernateException {
		final String namedQuery = prefix + "." + subQueryName;
		return session().createNamedQuery(namedQuery, super.getEntityClass());
	}

	@Override
	protected Session currentSession() {
		return session();
	}
}
