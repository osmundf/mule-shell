package net.sf.zoftwhere.dropwizard;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MuleInfo {

	private final Map<String, String> map;

	public MuleInfo() {
		this(new HashMap<>(0));
	}

	public MuleInfo(Map<String, String> map) {
		this.map = map;
	}

	public Optional<String> get(String propertyName) {
		// bootstrapCSS
		String value = map.get(propertyName);
		return Optional.ofNullable(value);
	}
}
