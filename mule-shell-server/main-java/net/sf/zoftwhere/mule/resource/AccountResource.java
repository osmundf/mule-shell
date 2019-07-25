package net.sf.zoftwhere.mule.resource;

import com.auth0.jwt.JWT;
import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractResource;
import net.sf.zoftwhere.mule.api.AccountApi;
import net.sf.zoftwhere.mule.jpa.AccessToken;
import net.sf.zoftwhere.mule.jpa.AccountLocator;
import net.sf.zoftwhere.mule.model.BasicUserModel;
import net.sf.zoftwhere.mule.security.AccountPrincipal;
import net.sf.zoftwhere.mule.security.AccountSigner;
import net.sf.zoftwhere.mule.security.JWTSigner;
import org.hibernate.Session;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AccountResource extends AbstractResource implements AccountApi {

	@Inject
	private Cache<UUID, AccountPrincipal> cache;

	@Inject
	private JWTSigner signer;

	@Inject
	private AccountSigner accountSigner;

	private final AccountLocator accountLocator;

	@Inject
	public AccountResource(Provider<Session> sessionProvider) {
		super(sessionProvider);
		this.accountLocator = new AccountLocator(sessionProvider);
	}

	/**
	 * System only: register user.
	 */
	@RolesAllowed({"SYSTEM"})
	@POST
	@Path("/register")
	public Response register(@QueryParam("user") String username, @QueryParam("email") String emailAddress) {
		BasicUserModel model = new BasicUserModel();
		return Response.ok(model).build();
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

		final var basicByteArray = Base64.getDecoder().decode(detail);
		final var usernameOption = getUsername(basicByteArray);

		if (usernameOption.isEmpty()) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		final var username = usernameOption.get();
		final var accountOption = tryFetchEntity(username, Optional::of, accountLocator::getByUsername);

		if (accountOption.isEmpty()) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		final var account = accountOption.get();
		final var data = getPassword(basicByteArray).orElse(null);

		if (data == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		if (codePointCount(data) < 6) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		if (!accountSigner.validate(data, account.getSalt(), account.getHash())) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		wrapTransaction(session -> {
			final var salt = accountSigner.generateSalt(512);
			final var hash = accountSigner.getHash(salt, data);

			account.setSalt(salt);
			account.setHash(hash);
			session.update(account);
		});

		final var accessToken = new AccessToken().setAccount(account);

		wrapTransaction(session -> session.save(accessToken));

		final var tokenBuilder = JWT.create()
				.withJWTId(accessToken.getId().toString())
				.withExpiresAt(new Date(Instant.now().plus(Duration.ofMinutes(10)).toEpochMilli()));

		final var jwtToken = signer.sign(tokenBuilder);

		final var principal = new AccountPrincipal(usernameOption.get(), "CLIENT");

		// Place in active cache.
		cache.put(accessToken.getId(), principal);

		return Response.ok(jwtToken).build();
	}

	@Override
	public Response logout(BasicUserModel body) {
		return Response.ok().build();
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

	private Optional<String> getUsername(final byte[] input) {
		final int size = input != null ? input.length : 0;

		for (int i = 0; i < size; i++) {
			if (input[i] == ':') {
				return Optional.of(new String(input, 0, i, StandardCharsets.UTF_8));
			}
		}

		return Optional.empty();
	}

	private Optional<byte[]> getPassword(final byte[] input) {
		final int size = input != null ? input.length : 0;

		for (int i = 0; i < size; i++) {
			if (input[i] == ':') {
				int dataSize = size - i - 1;
				byte[] data = new byte[dataSize];
				System.arraycopy(input, i + 1, data, 0, dataSize);
				return Optional.of(data);
			}
		}

		return Optional.empty();
	}

}
