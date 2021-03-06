package net.sf.zoftwhere.hibernate;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.inject.Provider;
import org.hibernate.Session;

public interface TransactionalSession {

	static void wrapSession(Provider<Session> sessionProvider, Consumer<Session> consumer) {
		try (var session = sessionProvider.get()) {
			consumer.accept(session);
		}
	}

	static <E> Optional<E> wrapSession(Provider<Session> sessionProvider, Function<Session, E> function) {
		try (var session = sessionProvider.get()) {
			return Optional.ofNullable(function.apply(session));
		}
	}
}
