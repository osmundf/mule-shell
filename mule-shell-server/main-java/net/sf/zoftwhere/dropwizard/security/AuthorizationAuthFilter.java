package net.sf.zoftwhere.dropwizard.security;

import java.security.Principal;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;

import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.Authenticator;

@Priority(Priorities.AUTHENTICATION)
public class AuthorizationAuthFilter<P extends Principal> extends AuthFilter<String, P> {

	private AuthorizationAuthFilter() {
	}

	@Override
	public void filter(ContainerRequestContext requestContext) {
		final var authorizationHeader = requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		final var credentials = getCredentials(authorizationHeader).orElse(null);

		if (!authenticate(requestContext, credentials, prefix)) {
			throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
		}
	}

	/**
	 * Parses a value of the `Authorization` header in the form of `&lt;scheme&gt; a892bf3e284da9bb40648ab10`.
	 *
	 * @param header the value of the `Authorization` header
	 * @return a token
	 */
	@Nonnull
	private Optional<String> getCredentials(String header) {
		if (header == null) {
			return Optional.empty();
		}

		final int space = header.indexOf(' ');
		if (space <= 0) {
			return Optional.empty();
		}

		final String prefix = header.substring(0, space);
		if (!this.prefix.equalsIgnoreCase(prefix)) {
			return Optional.empty();
		}

		return Optional.of(header.substring(space + 1));
	}

	/**
	 * Builder for {@link AuthorizationAuthFilter}.
	 * <p>An {@link Authenticator} must be provided during the building process.</p>
	 *
	 * @param <P> the type of the principal
	 */
	public static class Builder<P extends Principal>
		extends AuthFilterBuilder<String, P, AuthorizationAuthFilter<P>>
	{

		@Override
		protected AuthorizationAuthFilter<P> newInstance() {
			return new AuthorizationAuthFilter<>();
		}
	}
}
