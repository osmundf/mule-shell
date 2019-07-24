package net.sf.zoftwhere.dropwizard;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hibernate.Session;

import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
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

	protected abstract Session session();

	public static int codePointCount(final byte[] input) {
		int count = 0;
		final int size = input != null ? input.length : 0;
		for (int i = 0; i < size; i++) {
			final int b = input[i] & 0b1111_1111;
			if ((b & 0b1000_0000) == 0x0) {
				count += 1;
			} else if ((b & 0b1110_0000) == 0b1100_0000) {
				count += 1;
			} else if ((b & 0b1111_0000) == 0b1110_0000) {
				count += 1;
			} else if ((b & 0b1111_1000) == 0b1111_0000) {
				count += 1;
			} else {
				count += 0;
			}
		}
		return count;
	}

	public static String getCodePointString(byte[] array, int index) {
		int b = array[index] & 0xff;
		byte[] codepoint;
		if ((b & 0b1000_0000) == 0x0) {
			codepoint = new byte[1];
			codepoint[0] = array[index];
		} else if ((b & 0b1110_0000) == 0b1100_0000) {
			codepoint = new byte[2];
			codepoint[0] = array[index];
			codepoint[1] = array[index + 1];
		} else if ((b & 0b1111_0000) == 0b1110_0000) {
			codepoint = new byte[3];
			codepoint[0] = array[index];
			codepoint[1] = array[index + 1];
			codepoint[2] = array[index + 2];
		} else if ((b & 0b1111_1000) == 0b1111_0000) {
			codepoint = new byte[4];
			codepoint[0] = array[index];
			codepoint[1] = array[index + 1];
			codepoint[2] = array[index + 2];
			codepoint[3] = array[index + 3];
		} else {
			return null;
		}

		return new String(codepoint);
	}
}
