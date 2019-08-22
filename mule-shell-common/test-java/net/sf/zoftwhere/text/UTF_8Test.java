package net.sf.zoftwhere.text;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static net.sf.zoftwhere.text.UTF_8.codePointCount;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UTF_8Test {

	@Test
	void testCodePointCount() {
		assertEquals(5, codePointCount("møøse".getBytes(StandardCharsets.UTF_8)), "møøse");
		assertEquals(7, codePointCount("𝔘𝔫𝔦𝔠𝔬𝔡𝔢".getBytes(StandardCharsets.UTF_8)), "𝔘𝔫𝔦𝔠𝔬𝔡𝔢");
		assertEquals(8, codePointCount("J̲o̲s̲é̲".getBytes(StandardCharsets.UTF_8)), "J̲o̲s̲é̲");
	}
}
