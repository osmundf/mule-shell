package net.sf.zoftwhere.mule.security;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import io.dropwizard.auth.Authorizer;

import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;

public class AccountAuthorizer implements Authorizer<AccountPrincipal> {

	@Override
	@SuppressWarnings({"deprecation", "RedundantSuppression"})
	public boolean authorize(AccountPrincipal principal, String role) {
		return authorize(principal, role, null);
	}

	@Override
	public boolean authorize(AccountPrincipal user, String role, @Nullable ContainerRequestContext requestContext) {
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

		final var userRole = user.getRole().get();

		return Objects.equal(userRole, role);
	}
}
