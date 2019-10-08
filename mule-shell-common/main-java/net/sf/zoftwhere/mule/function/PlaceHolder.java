package net.sf.zoftwhere.mule.function;

import java.util.Optional;
import java.util.function.Supplier;

public interface PlaceHolder<I> extends Receiver<I>, Supplier<I> {

	Optional<I> optional();

	boolean isPresent();

	default boolean isEmpty() {
		return !isPresent();
	}

	default I orElse(I other) {
		return optional().orElse(other);
	}
}
