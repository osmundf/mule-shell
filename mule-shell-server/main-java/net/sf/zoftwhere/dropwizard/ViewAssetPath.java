package net.sf.zoftwhere.dropwizard;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ViewAssetPath {

	private final Map<String, String> map;

	public ViewAssetPath() {
		this(new HashMap<>(0));
	}

	public ViewAssetPath(Map<String, String> map) {
		this.map = map;
	}

	public Set<Map.Entry<String, String>> entrySet() {
		return map.entrySet();
	}

	public Optional<String> get(String assetName) {
		String value = map.get(assetName);
		return Optional.ofNullable(value);
	}
}
