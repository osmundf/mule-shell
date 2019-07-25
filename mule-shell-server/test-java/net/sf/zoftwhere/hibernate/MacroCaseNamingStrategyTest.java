package net.sf.zoftwhere.hibernate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MacroCaseNamingStrategyTest extends MacroCaseNamingStrategy {

	@Test
	void testSeparators() {
		checkResults("ThisHelloWorld", "THIS_HELLO_WORLD");
		checkResults("THIS_HELLO_WORLD", "THIS_HELLO_WORLD");
		checkResults("MyHTTPServer", "MY_HTTP_SERVER");
		checkResults("MY_HTTP_SERVER", "MY_HTTP_SERVER");
	}

	private void checkResults(String input, String expected) {
		assertEquals(expected, macroCase(input), input);
	}
}
