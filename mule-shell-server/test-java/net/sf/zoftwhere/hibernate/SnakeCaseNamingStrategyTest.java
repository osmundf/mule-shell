package net.sf.zoftwhere.hibernate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SnakeCaseNamingStrategyTest extends SnakeCaseNamingStrategy {

	@Test
	void testSeparators() {
		checkResults("ThisHelloWorld", "this_hello_world");
		checkResults("this_hello_world", "this_hello_world");
		checkResults("MyHTTPServer", "my_http_server");
		checkResults("my_http_server", "my_http_server");
	}

	private void checkResults(String input, String expected) {
		assertEquals(expected, snakeCase(input), input);
	}
}
