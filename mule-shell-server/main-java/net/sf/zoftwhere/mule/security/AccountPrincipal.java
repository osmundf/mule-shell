package net.sf.zoftwhere.mule.security;

import java.security.Principal;
import java.util.Optional;

import net.sf.zoftwhere.mule.jpa.Role;
import net.sf.zoftwhere.mule.model.RoleModel;

public class AccountPrincipal implements Principal {

	private final String username;

	private final String role;

	private AccountPrincipal(String username, String role) {
		this.username = username;
		this.role = role;
	}

	public AccountPrincipal() {
		this(null, (String) null);
	}

	public AccountPrincipal(String username, RoleModel role) {
		this.username = username;
		this.role = role.name();
	}

	public AccountPrincipal(String username, Role role) {
		this.username = username;
		this.role = role.getName();
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
