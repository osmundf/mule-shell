package net.sf.zoftwhere.mule.model;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoleModelTest {

	private static Logger logger = LoggerFactory.getLogger(RoleModelTest.class);

	@Test
	void ensureBasicRolesAreAvailable() {
		ensureKey(RoleModel.ADMIN);
		ensureKey(RoleModel.CLIENT);
		ensureKey(RoleModel.SYSTEM);
		ensureKey(RoleModel.REGISTER);
	}

	private void ensureKey(RoleModel model) {
		assertTrue(model != null, "Must not be null.");
		assertEquals("net.sf.zoftwhere.mule.model", model.getClass().getPackage().getName(), "Constant package");
		assertNotNull(model.name());

		logger.debug("Ensured access key model enum " + model.name());
	}
}