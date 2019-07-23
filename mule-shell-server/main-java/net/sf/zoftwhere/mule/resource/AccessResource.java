package net.sf.zoftwhere.mule.resource;

import com.auth0.jwt.JWT;
import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractResource;
import net.sf.zoftwhere.mule.api.SecureApi;
import net.sf.zoftwhere.mule.jpa.AccessToken;
import net.sf.zoftwhere.mule.jpa.Account;
import net.sf.zoftwhere.mule.jpa.AccountLocator;
import net.sf.zoftwhere.mule.model.BasicUserModel;
import net.sf.zoftwhere.mule.security.AccountPrincipal;
import net.sf.zoftwhere.mule.security.JWTSigner;
import org.hibernate.Session;

import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AccessResource extends AbstractResource implements SecureApi {

	private final Provider<Session> sessionProvider;

	private final AccountLocator accountLocator;

	@Inject
	private Cache<UUID, AccountPrincipal> cache;

	@Inject
	private JWTSigner signer;

	@Inject
	public AccessResource(Provider<Session> sessionProvider) {
		this.sessionProvider = sessionProvider;
		this.accountLocator = new AccountLocator(sessionProvider);
	}

	@Override
	public Response login(List<String> authorization) {
		final int size = authorization.size();
		if (size != 1) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		final String header = authorization.get(0);

		if (header.length() > 256) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		final Optional<String[]> split = splitHeader(header);

		if (split.isEmpty()) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		final String scheme = split.get()[0];
		final String detail = split.get()[1];

		if (!scheme.equalsIgnoreCase("basic")) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		final byte[] decoded = Base64.getDecoder().decode(detail);
		final Optional<String> username = getUserName(decoded);

		if (username.isEmpty()) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		final var session = sessionProvider.get();
		session.beginTransaction();
		final var userAccount = new Account()
				.setUserName(username.get())
				.setEmailAddress("")
				.setSalt(new byte[]{}) // TODO: Fix salt.
				.setHash(new byte[]{}); // TODO: Fix hash.
		session.persist(userAccount);
		session.getTransaction().commit();

		session.beginTransaction();
		final var accessToken = new AccessToken().setAccount(userAccount);
		session.save(accessToken);
		session.getTransaction().commit();

		final var tokenBuilder = JWT.create()
				.withJWTId(accessToken.getId().toString())
				.withIssuer("mule-shell")
				.withExpiresAt(new Date(Instant.now().plus(Duration.ofMinutes(10)).toEpochMilli()));

		final var jwtToken = signer.sign(tokenBuilder);

		final var principal = new AccountPrincipal(username.get(), "CLIENT");
		cache.put(accessToken.getId(), principal);

		return Response.ok(jwtToken).build();
	}

	@Override
	public Response logout(BasicUserModel body) {
		return Response.ok().build();
	}

	@Override
	protected Session session() {
		return sessionProvider.get();
	}

	private Optional<String[]> splitHeader(final String header) {
		if (header == null) {
			return Optional.empty();
		}

		final int index = header.indexOf(' ');
		if (index < 0 || index == header.length()) {
			return Optional.empty();
		}

		final String[] split = new String[]{header.substring(0, index), header.substring(index + 1)};
		return Optional.of(split);
	}

	private Optional<String> getUserName(final byte[] input) {
		final int size = input != null ? input.length : 0;

		for (int i = 0; i < size; i++) {
			if (input[i] == ':') {
				return Optional.of(new String(input, 0, i + 1, StandardCharsets.UTF_8));
			}
		}

		return Optional.empty();
	}
}
