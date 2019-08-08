package net.sf.zoftwhere.dropwizard;

import net.sf.zoftwhere.mule.model.RoleModel;
import org.junit.jupiter.api.Test;

import static net.sf.zoftwhere.dropwizard.AbstractResource.ADMIN_ROLE;
import static net.sf.zoftwhere.dropwizard.AbstractResource.CLIENT_ROLE;
import static net.sf.zoftwhere.dropwizard.AbstractResource.GUEST_ROLE;
import static net.sf.zoftwhere.dropwizard.AbstractResource.REGISTER_ROLE;
import static net.sf.zoftwhere.dropwizard.AbstractResource.SYSTEM_ROLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractResourceTest {

	@Test
	void ensureRoleNames() {
		assertEquals(ADMIN_ROLE, RoleModel.ADMIN.name());
		assertEquals(CLIENT_ROLE, RoleModel.CLIENT.name());
		assertEquals(GUEST_ROLE, RoleModel.GUEST.name());
		assertEquals(REGISTER_ROLE, RoleModel.REGISTER.name());
		assertEquals(SYSTEM_ROLE, RoleModel.SYSTEM.name());
	}
}