package net.sf.zoftwhere.mule.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.inject.Provider;
import io.dropwizard.auth.Authenticator;
import net.sf.zoftwhere.hibernate.TransactionalSession;
import net.sf.zoftwhere.mule.jpa.TokenLocator;
import org.hibernate.Session;

import java.util.Optional;
import java.util.UUID;

public class AccountAuthenticator implements Authenticator<String, AccountPrincipal>, TransactionalSession {

	private final Cache<UUID, AccountPrincipal> cache;

	private final JWTVerifier verifier;

	private final Provider<Session> sessionProvider;

	public AccountAuthenticator(Cache<UUID, AccountPrincipal> cache, JWTVerifier verifier, Provider<Session> sessionProvider) {
		this.cache = cache;
		this.verifier = verifier;
		this.sessionProvider = sessionProvider;
	}

	@Override
	public Optional<AccountPrincipal> authenticate(String credentials) {
		try {
			final DecodedJWT decoded = verifier.verify(credentials);
			final String tokenId = decoded.getId();
			final UUID tokenUUID = UUID.fromString(tokenId);
			final AccountPrincipal accountPrincipal = cache.getIfPresent(tokenUUID);

			if (accountPrincipal != null) {
				return Optional.of(accountPrincipal);
			}

			final var tokenLocator = new TokenLocator(sessionProvider);
			final var accessToken = tokenLocator.getById(tokenUUID).orElseThrow();
			final var accountRole = accessToken.getAccountRole();
			final var account = accountRole.getAccount();
			final var role = accountRole.getRole();

			// First check if the access token needs to be deleted.
			if (accountRole.getDeletedAt() == null) {
				// Check if the access token needs to be deleted.
				if (account.getDeletedAt() != null || role.getDeletedAt() != null
						|| Strings.isNullOrEmpty(account.getUsername()))
				{
					// Delete the access token.
					accountRole.delete();
					try (var session = sessionProvider.get()) {
						session.beginTransaction();
						session.update(accountRole);
						session.getTransaction().commit();
					}
					return Optional.empty();
				}
			}

			// Then check if the token is deleted.
			if (accountRole.getDeletedAt() != null) {
				return Optional.empty();
			}

			// Reinstate the cache entry for this access token.
			final var entry = new AccountPrincipal(account.getUsername(), role);
			cache.put(accessToken.getId(), entry);
			return Optional.of(entry);
		} catch (Exception e) {
			return Optional.empty();
		}
	}
}
