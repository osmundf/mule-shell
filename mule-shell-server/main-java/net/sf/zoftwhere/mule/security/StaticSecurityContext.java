package net.sf.zoftwhere.mule.security;

import java.security.Principal;
import java.util.Objects;
import javax.ws.rs.core.SecurityContext;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public class StaticSecurityContext implements SecurityContext {

	public static Builder withBuilder() {
		return new Builder();
	}

	@Getter
	private final boolean secure;

	@Getter
	private final String authenticationScheme;

	@Getter
	private final Principal userPrincipal;

	private final String role;

	public StaticSecurityContext(boolean secure, String authenticationScheme, Principal userPrincipal, String role) {
		this.secure = secure;
		this.authenticationScheme = authenticationScheme;
		this.userPrincipal = userPrincipal;
		this.role = role;
	}

	@Override
	public boolean isUserInRole(String desiredRole) {
		return Objects.equals(role, desiredRole);
	}

	@Getter
	@Setter
	@Accessors(fluent = true)
	public static class Builder {

		private boolean secure;
		private String authenticationScheme;
		private Principal userPrincipal;
		private String role;

		protected Builder() {
		}

		public SecurityContext build() {
			return new StaticSecurityContext(secure, authenticationScheme, userPrincipal, role);
		}
	}
}
