package net.sf.zoftwhere.mule.data;

import java.util.Optional;

public class TryParse {

	public static Optional<Integer> toInteger(String input) {
		try {
			return Optional.of(Integer.parseInt(input));
		} catch (NumberFormatException ignore) {
			return Optional.empty();
		}
	}

	public static Optional<Long> toLong(String input) {
		try {
			return Optional.of(Long.parseLong(input));
		} catch (NumberFormatException ignore) {
			return Optional.empty();
		}
	}
}
