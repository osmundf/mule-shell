package net.sf.zoftwhere.mule.security;

import java.security.Principal;
import java.util.Optional;

public class AccountPrincipal implements Principal {

	private final String userName;

	private final String role;

	public AccountPrincipal(String userName, String role) {
		this.userName = userName;
		this.role = role;
	}

	@Override
	public String getName() {
		throw new NullPointerException();
	}

	public Optional<String> getUserName() {
		return Optional.ofNullable(userName);
	}

	public Optional<String> getRole() {
		return Optional.ofNullable(role);
	}
}
