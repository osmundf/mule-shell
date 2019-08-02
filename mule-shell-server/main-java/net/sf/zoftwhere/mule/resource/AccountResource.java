package net.sf.zoftwhere.mule.resource;

import com.auth0.jwt.JWT;
import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractResource;
import net.sf.zoftwhere.mule.api.AccountApi;
import net.sf.zoftwhere.mule.jpa.Account;
import net.sf.zoftwhere.mule.jpa.AccountLocator;
import net.sf.zoftwhere.mule.jpa.AccountRole;
import net.sf.zoftwhere.mule.jpa.AccountRoleLocator;
import net.sf.zoftwhere.mule.jpa.Role;
import net.sf.zoftwhere.mule.jpa.RoleLocator;
import net.sf.zoftwhere.mule.jpa.Token;
import net.sf.zoftwhere.mule.model.BasicAccountModel;
import net.sf.zoftwhere.mule.model.RoleModel;
import net.sf.zoftwhere.mule.security.AccountPrincipal;
import net.sf.zoftwhere.mule.security.AccountSigner;
import net.sf.zoftwhere.mule.security.JWTSigner;
import net.sf.zoftwhere.text.UTF_8;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class AccountResource extends AbstractResource implements AccountApi {

	private static final Logger logger = LoggerFactory.getLogger(AccountResource.class);

	private static final String BASIC_AUTHENTICATION_SCHEME = "basic";

	@Inject
	private Provider<SecurityContext> securityContextProvider;

	@Inject
	private Cache<UUID, AccountPrincipal> cache;

	@Inject
	private Provider<JWTSigner> signerProvider;

	@Inject
	private Provider<AccountSigner> accountSignerProvider;

	private final AccountLocator accountLocator;

	private final RoleLocator roleLocator;

	private final AccountRoleLocator accountRoleLocator;

	@Inject
	public AccountResource(Provider<Session> sessionProvider) {
		super(sessionProvider);
		this.accountLocator = new AccountLocator(sessionProvider);
		this.roleLocator = new RoleLocator(sessionProvider);
		this.accountRoleLocator = new AccountRoleLocator(sessionProvider);
	}

	/**
	 * System only: register user.
	 */
	@RolesAllowed({SYSTEM_ROLE})
	@POST
	@Path("/register")
	public Response register(@QueryParam("user") String username, @QueryParam("email") String emailAddress) {
		final var security = securityContextProvider.get();
		final var signer = signerProvider.get();

		// TODO: !security.isSecure()
		if (security == null || security.getUserPrincipal() == null
				|| !Objects.equals(username, security.getUserPrincipal().getName()))
		{
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		final var first = tryFetchEntity(username, Optional::of, accountLocator::getByUsername).orElse(null);

		if (first != null) {
			return Response.ok(Response.Status.CONFLICT).build();
		}

		final var role = tryFetchEntity(RoleModel.REGISTER, Optional::of, roleLocator::getByKey).orElse(null);

		if (role == null) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		wrapSession(session -> {
			Account account = new Account(username, emailAddress);

			session.beginTransaction();
			session.save(account);
			session.getTransaction().commit();
		});

		final var account = tryFetchEntity(username, Optional::of, accountLocator::getByUsername).orElse(null);

		if (account == null) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		final var accountRole = new AccountRole(account, role, role.getValue());

		wrapSession(session -> {
			session.beginTransaction();
			session.save(accountRole);
			session.getTransaction().commit();
		});

		final var accessToken = new Token(accountRole);

		wrapSession(session -> {
			session.beginTransaction();
			session.save(accessToken);
			session.getTransaction().commit();
		});

		final var tokenBuilder = JWT.create()
				.withJWTId(accessToken.getId().toString())
				.withExpiresAt(new Date(Instant.now().plus(Duration.ofMinutes(120)).toEpochMilli()));

		final var jwtToken = signer.sign(tokenBuilder);

		final var principal = new AccountPrincipal(username, role.getName());

		// Place in active cache.
		cache.put(accessToken.getId(), principal);

		return Response.ok(jwtToken).build();
	}

	/**
	 * System only: register user.
	 */
	@RolesAllowed(value = {CLIENT_ROLE, REGISTER_ROLE})
	@POST
	@Path("/reset")
	public Response reset(@QueryParam("user") String username, @QueryParam("password") String password) {
		final var security = securityContextProvider.get();

		// TODO: !security.isSecure()
		if (security == null || security.getUserPrincipal() == null
				|| !Objects.equals(username, security.getUserPrincipal().getName()) || password == null)
		{
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		final var name = security.getUserPrincipal().getName();
		// TODO: Fix this with an appropriate message.
		final var account = accountLocator.getByUsername(name).orElseThrow();
		final var digest = accountSignerProvider.get();
		final var data = password.getBytes(StandardCharsets.UTF_8);

		if (UTF_8.codePointCount(data) < digest.getMinimumPasswordLength()) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		if (!digest.validate(data, account.getSalt(), account.getHash())) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		if (security.isUserInRole(REGISTER_ROLE)) {
			try {
				setupRegisteredAccount(account, RoleModel.CLIENT);
			} catch (Exception e) {
				logger.error("Error setting up registered account.", e);
			}
		}

		updateAccountSaltHash(account, data);

		return Response.ok().build();
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

		if (!scheme.equalsIgnoreCase(BASIC_AUTHENTICATION_SCHEME)) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		final var basicByteArray = Base64.getDecoder().decode(detail);
		final var usernameOption = getUsername(basicByteArray);

		if (usernameOption.isEmpty()) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		final var username = usernameOption.get();
		final var account = tryFetchEntity(username, Optional::of, accountLocator::getByUsername).orElse(null);

		if (account == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		final var digest = accountSignerProvider.get();
		final var data = getPassword(basicByteArray).orElse(null);

		if (data == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		if (UTF_8.codePointCount(data) < digest.getMinimumPasswordLength()) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		if (!digest.validate(data, account.getSalt(), account.getHash())) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		final var accountRoleList = accountRoleLocator.getForAccount(account);

		if (accountRoleList.size() == 0) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		final var accountRole = accountRoleList.get(0);

		final var accessToken = new Token(accountRole);

		wrapSession(session -> {
			session.beginTransaction();
			session.save(accessToken);
			session.getTransaction().commit();
		});

		updateAccountSaltHash(account, data);

		final var tokenBuilder = JWT.create()
				.withJWTId(accessToken.getId().toString())
				.withExpiresAt(new Date(Instant.now().plus(Duration.ofMinutes(10)).toEpochMilli()));

		final var signer = signerProvider.get();
		final var jwtToken = signer.sign(tokenBuilder);

		final var principal = new AccountPrincipal(username, accountRole.getRole().getName());

		// Place in active cache.
		cache.put(accessToken.getId(), principal);

		return Response.ok(jwtToken).build();
	}

	@PermitAll
	@Override
	public Response logout(BasicAccountModel body) {
		final var context = securityContextProvider.get();

		// TODO: !security.isSecure()
		if (context == null || context.getAuthenticationScheme() == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		return Response.ok().build();
	}

	public void updateAccountSaltHash(final Account account, final byte[] data) {
		final var digest = accountSignerProvider.get();
		wrapSession(session -> {
			account.updateHash(digest, data);

			session.beginTransaction();
			session.update(account);
			session.getTransaction().commit();
		});
	}

	private void setupRegisteredAccount(Account account, RoleModel roleModel) {
		// Check if account has this as an active role.
		final var currentAccountRole = accountRoleLocator.getByKey(account, roleModel);

		if (currentAccountRole == null) {
			// Add with role.
			wrapSession(session -> {
				// TODO: Fix this with the needed checks.
				final var role = roleLocator.getByKey(Role.getKey(roleModel)).orElseThrow();
				final var accountRole = new AccountRole(account, role, role.getValue());

				session.beginTransaction();
				session.save(accountRole);
				session.getTransaction().commit();
			});
		}

		// Check if account has active register role.
		final var registerRole = accountRoleLocator.getByRoleName(account, RoleModel.REGISTER);

		if (registerRole != null) {
			// Delete register account role.
			wrapSession(session -> {
				registerRole.delete();

				session.beginTransaction();
				session.update(registerRole);
				session.getTransaction().commit();
			});
		}
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
