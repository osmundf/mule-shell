package net.sf.zoftwhere.dropwizard;

import com.google.inject.Provider;
import io.dropwizard.hibernate.AbstractDAO;
import net.sf.zoftwhere.mule.proxy.EmptyInterface;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import javax.persistence.NoResultException;
import java.io.Serializable;
import java.util.Optional;
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

	@Override
	protected Session currentSession() {
		return sessionProvider.get();
	}

	public Optional<E> tryFetchNamedQuery(String subName, Function<Query<E>, Query<E>> parameter) {
		try (Session session = sessionProvider.get()) {
			final var name = prefix + "." + subName;
			Query<E> query = session.createNamedQuery(name, super.getEntityClass());
			query = parameter.apply(query);
			return Optional.of(query.getSingleResult());
		} catch (RuntimeException e) {
			return Optional.empty();
		}
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
