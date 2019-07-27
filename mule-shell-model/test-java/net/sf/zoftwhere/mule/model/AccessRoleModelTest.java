package net.sf.zoftwhere.mule.model;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccessRoleModelTest {

	private static Logger logger = LoggerFactory.getLogger(AccessRoleModelTest.class);

	@Test
	void ensureBasicRolesAreAvailable() {
		ensureKey(AccessRoleModel.ADMIN);
		ensureKey(AccessRoleModel.CLIENT);
		ensureKey(AccessRoleModel.SYSTEM);
		ensureKey(AccessRoleModel.REGISTER);
	}

	private void ensureKey(AccessRoleModel model) {
		assertTrue(model != null, "Must not be null.");
		assertEquals("net.sf.zoftwhere.mule.model", model.getClass().getPackage().getName(), "Constant package");
		assertNotNull(model.name());

		logger.debug("Ensured access key model enum " + model.name());
	}
}