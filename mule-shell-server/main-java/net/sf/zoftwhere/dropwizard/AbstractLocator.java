package net.sf.zoftwhere.dropwizard;

import com.google.inject.Provider;
import io.dropwizard.hibernate.AbstractDAO;
import net.sf.zoftwhere.hibernate.TransactionalSession;
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

public abstract class AbstractLocator<E, I extends Serializable> extends AbstractDAO<E> implements TransactionalSession {

	private static final Logger logger = LoggerFactory.getLogger(AbstractLocator.class);

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

	public Optional<E> getById(I id) {
		try {
			return Optional.ofNullable(super.get(id));
		} catch (NoResultException ignore) {
			return Optional.empty();
		}
	}

	@Override
	protected Session currentSession() {
		return sessionProvider.get();
	}

	protected Optional<E> tryFetchNamedQuery(String subName, Function<Query<E>, E> function) {
		final var name = prefix + "." + subName;
		try (Session session = sessionProvider.get()) {
			final var result = function.apply(session.createNamedQuery(name, super.getEntityClass()));
			return Optional.ofNullable(result);
		} catch (NoResultException ignore) {
			return Optional.empty();
		} catch (RuntimeException e) {
			final var exceptionType = e.getClass().getName();
			logger.debug("tryFetchNamedQuery(): {} occurred running named query ({}).", exceptionType, name);
			throw e;
		}
	}

	protected <T> Optional<T> tryFetchSingleResult(String subName, Function<Query<T>, Optional<T>> routine, Class<T> resultType) {
		final var name = prefix + "." + subName;
		try (Session session = sessionProvider.get()) {
			return routine.apply(session.createNamedQuery(name, resultType));
		} catch (NoResultException ignore) {
			return Optional.empty();
		} catch (RuntimeException e) {
			final var exceptionType = e.getClass().getName();
			logger.debug("tryFetchSingleResult(): {} occurred running named query ({}).", exceptionType, name);
			throw e;
		}
	}

	protected <T> Optional<List<T>> tryFetchResult(String subName, Function<Query<T>, Query<T>> routine, Class<T> resultType) {
		final var name = prefix + "." + subName;
		try (Session session = sessionProvider.get()) {
			List<T> result = routine.apply(session.createNamedQuery(name, resultType)).getResultList();
			return Optional.ofNullable(result);
		} catch (NoResultException ignore) {
			return Optional.empty();
		} catch (RuntimeException e) {
			final var exceptionType = e.getClass().getName();
			logger.debug("tryFetchResult(): {} occurred running named query ({}).", exceptionType, name);
			throw e;
		}
	}

	public static <I, E> Optional<E> tryFetchEntity(I id, final Function<I, Optional<E>> fetcher) {
		try {
			return fetcher.apply(id);
		} catch (NoResultException ignore) {
			return Optional.empty();
		}
	}

	public static <V, I, E> Optional<E> tryFetchEntity(V value, final Function<V, Optional<I>> parser, final Function<I, Optional<E>> fetcher) {
		try {
			final Optional<I> id = parser.apply(value);
			return id.isPresent() ? fetcher.apply(id.get()) : Optional.empty();
		} catch (NoResultException ignore) {
			return Optional.empty();
		}
	}
}
