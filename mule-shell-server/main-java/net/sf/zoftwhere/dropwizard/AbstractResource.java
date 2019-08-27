package net.sf.zoftwhere.dropwizard;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.inject.Provider;
import net.sf.zoftwhere.hibernate.TransactionalSession;
import net.sf.zoftwhere.mule.jpa.AccountLocator;
import net.sf.zoftwhere.mule.jpa.ShellSessionLocator;
import net.sf.zoftwhere.mule.shell.MuleShell;
import net.sf.zoftwhere.mule.shell.MuleShellManager;
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
	public static final String GUEST_ROLE = "GUEST";
	public static final String REGISTER_ROLE = "REGISTER";
	public static final String SYSTEM_ROLE = "SYSTEM";

	private final Provider<Session> sessionProvider;

	public AbstractResource(Provider<Session> sessionProvider) {
		this.sessionProvider = sessionProvider;
	}

	protected void wrapSession(Consumer<Session> consumer) {
		TransactionalSession.wrapSession(sessionProvider, consumer);
	}

	protected <E> Optional<E> wrapFunction(Function<Session, E> function) {
		return TransactionalSession.wrapSession(sessionProvider, function);
	}

	protected <T> T wrapMuleShell(Cache<UUID, MuleShell> shellCache, String username, String sessionId, Function<MuleShell, T> function) {
		final var accountLocator = new AccountLocator(sessionProvider);
		final var shellSessionLocator = new ShellSessionLocator(sessionProvider);

		final var account = accountLocator.getByUsername(username).orElseThrow();
		final var manager = new MuleShellManager(shellCache, shellSessionLocator);
		final var shell = manager.getMuleShell(tryAsUUID(sessionId).orElse(null), account).orElse(null);

		return function.apply(shell);
	}

	protected <T extends AbstractEntity<?>> void persistCollection(T entity) {
		wrapSession(session -> {
			session.beginTransaction();
			session.persist(entity);
			session.getTransaction().commit();
		});
	}

	protected <T extends AbstractEntity<?>> void saveEntity(T entity) {
		wrapSession(session -> {
			session.beginTransaction();
			session.save(entity);
			session.getTransaction().commit();
		});
	}

	protected <T extends AbstractEntity<?>> void updateEntity(T entity) {
		wrapSession(session -> {
			session.beginTransaction();
			session.update(entity);
			session.getTransaction().commit();
		});
	}

	protected Optional<Integer> tryAsInteger(String value) {
		if (Strings.isNullOrEmpty(value)) {
			return Optional.empty();
		}

		try {
			return Optional.of(Integer.valueOf(value));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	protected Optional<UUID> tryAsUUID(String value) {
		if (Strings.isNullOrEmpty(value)) {
			return Optional.empty();
		}

		try {
			return Optional.of(UUID.fromString(value));
		} catch (RuntimeException e) {
			return Optional.empty();
		}
	}

	protected Optional<ZoneOffset> getZoneOffset(String tz) throws DateTimeException {
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

	protected <V, E, I> Optional<E> tryFetchEntity(V value, final Function<V, Optional<I>> parser, final Function<I, Optional<E>> fetcher) {
		try {
			return parser.apply(value).map(fetcher).get();
		} catch (NoResultException ignore) {
			return Optional.empty();
		}
	}

	public static EntityNotFoundException entityNotFound(String name, String id) {
		return new EntityNotFoundException(String.format("Could not find %s entity with id (%s).", name, id));
	}
}
