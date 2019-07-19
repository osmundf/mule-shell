package net.sf.zoftwhere.mule.shell;

import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

public class UUIDBuffer implements Supplier<UUID> {

	private final Random random;

	public UUIDBuffer(Random random) {
		this.random = random;
	}

	@Override
	public UUID get() {
		return getUUID();
	}

	synchronized private UUID getUUID() {
		final var upper = random.nextLong();
		final var lower = random.nextLong();
		return new UUID(upper, lower);
	}
}
