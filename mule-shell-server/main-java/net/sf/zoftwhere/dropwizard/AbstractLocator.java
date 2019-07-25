package net.sf.zoftwhere.dropwizard;

import com.google.inject.Provider;
import io.dropwizard.hibernate.AbstractDAO;
import net.sf.zoftwhere.mule.proxy.EmptyInterface;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import javax.persistence.NoResultException;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class AbstractLocator<E, I extends Serializable> extends AbstractDAO<E> implements TransactionalSession {

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

	protected Query<E> namedQuery(String subQueryName) throws HibernateException {
		return namedQuery(sessionProvider.get(), subQueryName);
	}

	private Query<E> namedQuery(Session session, String subQueryName) throws HibernateException {
		final String namedQuery = prefix + "." + subQueryName;
		return session.createNamedQuery(namedQuery, super.getEntityClass());
	}

	protected void wrapTransaction(Consumer<Session> action) {
		TransactionalSession.wrapTransaction(sessionProvider, action);
	}

	protected Optional<E> wrapQuery(Function<Session, E> query) {
		return TransactionalSession.wrapQuery(sessionProvider, query);
	}

	@Override
	protected Session currentSession() {
		new Exception("Provider taken but possibly not returned?").printStackTrace();
		return sessionProvider.get();
	}

	public <V> Optional<E> tryFetchEntity(V value, final Function<V, Optional<I>> parser) {
		try {
			return parser.apply(value).map(this::getById);
		} catch (NoResultException ignore) {
			return Optional.empty();
		}
	}

	public static <V, E, I> Optional<E> tryFetchEntity(V value, final Function<V, Optional<I>> parser, final Function<I, E> fetcher) {
		try {
			return parser.apply(value).map(fetcher);
		} catch (NoResultException ignore) {
			return Optional.empty();
		}
	}

}
