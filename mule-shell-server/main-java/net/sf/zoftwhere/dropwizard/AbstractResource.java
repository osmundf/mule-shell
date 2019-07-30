package net.sf.zoftwhere.dropwizard;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provider;
import org.hibernate.Session;

import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractResource implements TransactionalSession {

	public static final String ADMIN_ROLE = "ADMIN";
	public static final String CLIENT_ROLE = "CLIENT";
	public static final String REGISTER_ROLE = "REGISTER";
	public static final String SYSTEM_ROLE = "SYSTEM";

	protected final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

	private final Provider<Session> sessionProvider;

	public AbstractResource(Provider<Session> sessionProvider) {
		this.sessionProvider = sessionProvider;
	}

	protected void wrapSession(Consumer<Session> consumer) {
		TransactionalSession.wrapSession(sessionProvider, consumer);
	}

	public Optional<Integer> tryAsInteger(String value) {
		if (Strings.isNullOrEmpty(value)) {
			return Optional.empty();
		}

		try {
			return Optional.of(Integer.valueOf(value));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	public Optional<UUID> tryAsUUID(String value) {
		if (Strings.isNullOrEmpty(value)) {
			return Optional.empty();
		}

		try {
			return Optional.of(UUID.fromString(value));
		} catch (RuntimeException e) {
			return Optional.empty();
		}
	}

	public Optional<ZoneOffset> getZoneOffset(String tz) throws DateTimeException {
		if (Strings.isNullOrEmpty(tz)) {
			return Optional.empty();
		}

		return Optional.of(ZoneOffset.of(tz));
	}

	public Optional<ZoneOffset> tryAsZoneOffset(String tz) {
		if (Strings.isNullOrEmpty(tz)) {
			return Optional.empty();
		}

		try {
			return Optional.of(ZoneOffset.of(tz));
		} catch (DateTimeException e) {
			return Optional.empty();
		}
	}

	protected <V, E, M> Optional<M> tryGetModel(V value, Function<V, E> toEntity, Function<E, M> toModel) {
		return Optional.ofNullable(toEntity.apply(value)).map(toModel);
	}

	protected <E, I> E fetchEntity(String value, final Function<String, Optional<I>> parser, final Function<I, E> fetcher) {
		final var name = fetcher.getClass().getGenericSuperclass().getTypeName();
		return parser.apply(value).map(fetcher).orElseThrow(() -> entityNotFound(name, value));
	}

	protected <V, E, I> Optional<E> tryFetchEntity(V value, final Function<V, Optional<I>> parser, final Function<I, E> fetcher) {
		try {
			return parser.apply(value).map(fetcher);
		} catch (NoResultException ignore) {
			return Optional.empty();
		}
	}

	public EntityNotFoundException entityNotFound(String name, String id) {
		return new EntityNotFoundException(String.format("Could not find %s entity with id (%s).", name, id));
	}
}
