package net.sf.zoftwhere.dropwizard;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

public abstract class AbstractResource {

	public final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	
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

	public EntityNotFoundException entityNotFound(String name, String id) {
		return new EntityNotFoundException(String.format("Could not find %s entity with id (%s).", name, id));
	}
}
