package net.sf.zoftwhere.dropwizard;

import com.google.inject.Provider;
import org.hibernate.Session;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface TransactionalSession {

	static <E> Optional<E> wrapQuery(Provider<Session> sessionProvider, Function<Session, E> action) {
		final var session = sessionProvider.get();
		E result;
		session.beginTransaction();
		try {
			result = action.apply(session);
			session.getTransaction().commit();
			return Optional.ofNullable(result);
		} catch (RuntimeException e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			session.close();
		}
	}

	static void wrapTransaction(Provider<Session> sessionProvider, Consumer<Session> action) {
		final var session = sessionProvider.get();
		session.beginTransaction();
		try {
			action.accept(session);
			session.getTransaction().commit();
		} catch (RuntimeException e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			session.close();
		}
	}
}
