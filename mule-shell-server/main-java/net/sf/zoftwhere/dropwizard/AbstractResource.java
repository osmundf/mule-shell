package net.sf.zoftwhere.dropwizard;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hibernate.Session;

import javax.persistence.EntityNotFoundException;
import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public abstract class AbstractResource {

	protected final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

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

	public Optional<ZoneOffset> tryAsZoneOffset(String tz) {
		if (Strings.isNullOrEmpty(tz)) {
			return Optional.empty();
		}

		return Optional.of(ZoneOffset.of(tz));
	}

	protected <V, E, M> M getFromMap(V value, Function<V, E> toEntity, Function<E, M> toModel) {
		return Optional.ofNullable(toEntity.apply(value)).map(toModel).orElse(null);
	}

	protected <E, I> E getFromMap(String name, String value, final Function<String, Optional<I>> parser, final Function<I, E> fetcher) {
		return parser.apply(value).map(fetcher).orElseThrow(() -> entityNotFound(name, value));
	}

	public EntityNotFoundException entityNotFound(String name, String id) {
		return new EntityNotFoundException(String.format("Could not find %s entity with id (%s).", name, id));
	}

	protected abstract Session session();
}
