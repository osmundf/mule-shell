package net.sf.zoftwhere.mule.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 */
class TryParseTest {

	@Test
	void testToInteger() {
		assertEquals(12, (int) TryParse.toInteger("12").orElseThrow());
		try {
			TryParse.toInteger("not.an.integer").orElseThrow();
			fail();
		}
		catch (Exception ignore) {
		}
	}

	@Test
	void testToLong() {
		assertEquals(12L, (long) TryParse.toLong("12").orElseThrow());
		try {
			TryParse.toInteger("not.a.long").orElseThrow();
			fail();
		}
		catch (Exception ignore) {
		}
	}

}