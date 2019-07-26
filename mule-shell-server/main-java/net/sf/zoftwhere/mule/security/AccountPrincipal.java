package net.sf.zoftwhere.mule.security;

import java.security.Principal;
import java.util.Optional;

public class AccountPrincipal implements Principal {

	private final String username;

	private final String role;

	public AccountPrincipal(String username, String role) {
		this.username = username;
		this.role = role;
	}

	@Override
	public String getName() {
		return username;
	}

	public Optional<String> getUsername() {
		return Optional.ofNullable(username);
	}

	public Optional<String> getRole() {
		return Optional.ofNullable(role);
	}
}
