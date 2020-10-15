package net.sf.zoftwhere.time;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class Instants {
	public static OffsetDateTime withZoneOffset(Instant instant, ZoneOffset zoneOffset) {
		return instant != null
			? zoneOffset != null ? instant.atOffset(zoneOffset) : instant.atOffset(ZoneOffset.UTC)
			: null;
	}
}
