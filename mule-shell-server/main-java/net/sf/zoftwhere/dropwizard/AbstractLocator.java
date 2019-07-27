package net.sf.zoftwhere.dropwizard;

import com.google.inject.Provider;
import io.dropwizard.hibernate.AbstractDAO;
import net.sf.zoftwhere.mule.proxy.EmptyInterface;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class AbstractLocator<E, I extends Serializable> extends AbstractDAO<E> implements TransactionalSession {

	private static final Logger logger = LoggerFactory.getLogger(AbstractResource.class);

	private final Provider<Session> sessionProvider;

	private final String prefix;

	public AbstractLocator(final Provider<Session> sessionProvider) {
		super(EmptyInterface.create(
				AbstractDAO.class.getClassLoader(),
				new Class<?>[]{SessionFactory.class},
				new NullPointerException("Do not use session factory here.")
		));

		this.prefix = getEntityClass().getSimpleName();
		this.sessionProvider = sessionProvider;
	}

	public E getById(I id) {
		return super.get(id);
	}

	@Override
	protected Session currentSession() {
		return sessionProvider.get();
	}

	public Optional<E> tryFetchNamedQuery(String subName, Function<Query<E>, E> parameter) {
		try (Session session = sessionProvider.get()) {
			final var name = prefix + "." + subName;
			final var result = parameter.apply(session.createNamedQuery(name, super.getEntityClass()));
			return Optional.of(result);
		} catch (RuntimeException e) {
			logger.warn("Error running named query ({}.{})", prefix, subName);
			return Optional.empty();
		}
	}

	public <T> Optional<T> tryFetchSingleResult(String subName, Function<Query<T>, Optional<T>> routine, Class<T> resultType) {
		try (Session session = sessionProvider.get()) {
			final var name = prefix + "." + subName;
			return routine.apply(session.createNamedQuery(name, resultType));
		} catch (RuntimeException e) {
			logger.warn("Error running named query ({}.{})", prefix, subName);
			return Optional.empty();
		}
	}

	public <T> Optional<List<T>> tryFetchResult(String subName, Function<Query<T>, Query<T>> routine, Class<T> resultType) {
		try (Session session = sessionProvider.get()) {
			final var name = prefix + "." + subName;
			List<T> result = routine.apply(session.createNamedQuery(name, resultType)).getResultList();
			return Optional.ofNullable(result);
		} catch (RuntimeException e) {
			logger.warn("Error running named query ({}.{})", prefix, subName);
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
