package net.sf.zoftwhere.mule.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccessRoleModelTest {

	public static void main(String[] args) {
		final var valueArray = AccessRoleModel.values();
		final var size = valueArray.length;
		final var idArray = newIdArray(valueArray.length);

		for (int i = 0; i < size; i++) {
			final var e = valueArray[i];
			final var id = idArray[i];
			final var key = getKey(e);
			System.out.printf("%s %s %s%n", id.toString(), e.name(), key);
		}
	}

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
	}

	private static UUID[] newIdArray(int length) {
		final var array = new UUID[length];
		for (int i = 0; i < length; i++) {
			array[i] = UUID.randomUUID();
		}
		Arrays.sort(array, Comparator.comparing(UUID::toString));
		return array;
	}

	private static String getKey(AccessRoleModel model) {
		final var packageName = model.getClass().getPackage().getName();
		final var enumName = model.name();
		return packageName + ":" + enumName;
	}
}