package net.sf.zoftwhere.mule.security;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.cache.Cache;
import io.dropwizard.auth.Authenticator;

import java.util.Optional;
import java.util.UUID;

public class AccountAuthenticator implements Authenticator<String, AccountPrincipal> {

	private final Cache<UUID, AccountPrincipal> cache;

	private final JWTVerifier verifier;

	public AccountAuthenticator(Cache<UUID, AccountPrincipal> cache, JWTVerifier verifier) {
		this.cache = cache;
		this.verifier = verifier;
	}

	@Override
	public Optional<AccountPrincipal> authenticate(String credentials) {
		try {
			final DecodedJWT decoded = verifier.verify(credentials);
			final String tokenId = decoded.getId();
			final UUID tokenUUID = UUID.fromString(tokenId);
			return Optional.ofNullable(cache.getIfPresent(tokenUUID));
		} catch (Exception e) {
			return Optional.empty();
		}
	}
}
