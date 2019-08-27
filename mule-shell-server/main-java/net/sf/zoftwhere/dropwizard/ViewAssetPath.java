package net.sf.zoftwhere.dropwizard;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ViewAssetPath {

	private final Map<String, String> map;

	public ViewAssetPath() {
		this(new HashMap<>(0));
	}

	public ViewAssetPath(Map<String, String> map) {
		this.map = map;
	}

	public Optional<String> get(String assetName) {
		String value = map.get(assetName);
		return Optional.ofNullable(value);
	}
}
