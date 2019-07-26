package net.sf.zoftwhere.dropwizard;

import net.sf.zoftwhere.mule.model.AccessRoleModel;
import org.junit.jupiter.api.Test;

import static net.sf.zoftwhere.dropwizard.AbstractResource.ADMIN_ROLE;
import static net.sf.zoftwhere.dropwizard.AbstractResource.CLIENT_ROLE;
import static net.sf.zoftwhere.dropwizard.AbstractResource.REGISTER_ROLE;
import static net.sf.zoftwhere.dropwizard.AbstractResource.SYSTEM_ROLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractResourceTest {

	@Test
	void ensureRoleNames() {
		assertEquals(ADMIN_ROLE, AccessRoleModel.ADMIN.name());
		assertEquals(CLIENT_ROLE, AccessRoleModel.CLIENT.name());
		assertEquals(REGISTER_ROLE, AccessRoleModel.REGISTER.name());
		assertEquals(SYSTEM_ROLE, AccessRoleModel.SYSTEM.name());
	}
}