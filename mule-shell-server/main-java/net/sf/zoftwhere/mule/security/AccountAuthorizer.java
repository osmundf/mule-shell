package net.sf.zoftwhere.mule.security;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import io.dropwizard.auth.Authorizer;

public class AccountAuthorizer implements Authorizer<AccountPrincipal> {

	@Override
	public boolean authorize(AccountPrincipal user, String role) {
		if (user == null) {
			return false;
		}

		if (Strings.isNullOrEmpty(role)) {
			return false;
		}

		if (user.getUsername().isEmpty()) {
			return false;
		}

		if (user.getRole().isEmpty()) {
			return false;
		}

		final var userRole = user.getRole().orElseThrow();

		//noinspection RedundantIfStatement
		if (!Objects.equal(userRole, role)) {
			return false;
		}

		return true;
	}
}
