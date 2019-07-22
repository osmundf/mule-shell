package net.sf.zoftwhere.mule.security;

import io.dropwizard.auth.Authorizer;

public class AccountAuthorizer implements Authorizer<AccountPrincipal> {

	@Override
	public boolean authorize(AccountPrincipal user, String role) {
		return user != null
				&& user.getUserName().isPresent()
				&& user.getRole().orElseThrow().equalsIgnoreCase(role);
	}
}
