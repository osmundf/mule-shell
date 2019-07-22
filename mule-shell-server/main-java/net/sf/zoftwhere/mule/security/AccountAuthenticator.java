package net.sf.zoftwhere.mule.security;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Inject;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import net.sf.zoftwhere.mule.cache.LoginAccountCache;

import java.util.Optional;
import java.util.UUID;

public class AccountAuthenticator implements Authenticator<String, AccountPrincipal> {

	@Inject
	private LoginAccountCache cache;

	@Inject
	private JWTVerifier verifier;

	public AccountAuthenticator() {
	}

	@Override
	public Optional<AccountPrincipal> authenticate(String credentials) throws AuthenticationException {
		final DecodedJWT decoded = verifier.verify(credentials);
		final String tokenId = decoded.getId();
		final UUID tokenUUID = UUID.fromString(tokenId);
		return Optional.ofNullable(cache.getIfPresent(tokenUUID));
	}
}
