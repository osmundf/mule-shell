package net.sf.zoftwhere.mule.resource;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AccessResourceTest {

	@Test
	public void testAccess() {
		final var resource = new AccessResource(null);
		final var login = resource.login(Collections.singletonList("basic 348298347289342374392847394723948239724387"));

		assertNotNull(resource);
		assertNotNull(login);
	}

}