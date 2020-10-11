package net.sf.zoftwhere.mule.resource;

import com.auth0.jwt.JWT;
import com.google.common.base.Strings;
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
import net.sf.zoftwhere.mule.jpa.ShellSession;
import net.sf.zoftwhere.mule.jpa.Token;
import net.sf.zoftwhere.mule.model.BasicAccountModel;
import net.sf.zoftwhere.mule.model.JsonWebTokenModel;
import net.sf.zoftwhere.mule.model.RoleModel;
import net.sf.zoftwhere.mule.security.AccountPrincipal;
import net.sf.zoftwhere.mule.security.AccountSigner;
import net.sf.zoftwhere.mule.security.AuthenticationScheme;
import net.sf.zoftwhere.mule.security.JWTSigner;
import net.sf.zoftwhere.text.UTF_8;
import net.sf.zoftwhere.time.Instants;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
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
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class AccountResource extends AbstractResource implements AccountApi {

	private static final Logger logger = LoggerFactory.getLogger(AccountResource.class);

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
	 *
	 * @param username     username
	 * @param emailAddress email address
	 * @return JsonWebTokenModel model.
	 */
	@RolesAllowed({SYSTEM_ROLE})
	@POST
	@Path("/register")
	public Response register(@QueryParam("user") String username, @QueryParam("email") String emailAddress) {
		final var security = securityContextProvider.get();

		// TODO: !security.isSecure()
		if (security == null || security.getUserPrincipal() == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		if (!Objects.equals(username, security.getUserPrincipal().getName())) {
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

		final var account = new Account(username, emailAddress);
		final var accountRole = new AccountRole(account, role, role.getValue());
		final var accessToken = new Token(accountRole);

		saveEntity(account);
		saveEntity(accountRole);
		saveEntity(accessToken);

		return activeLogin(account, accessToken, Duration.ofMinutes(120));
	}

	/**
	 * System only: reset user password.
	 *
	 * @param username username
	 * @param password password
	 * @return 200 OK response.
	 */
	@RolesAllowed(value = {CLIENT_ROLE, REGISTER_ROLE})
	@POST
	@Path("/reset")
	public Response reset(@QueryParam("user") String username, @QueryParam("password") String password) {
		final var security = securityContextProvider.get();

		// TODO: !security.isSecure()
		if (security == null || security.getUserPrincipal() == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		if (!Objects.equals(username, security.getUserPrincipal().getName()) || password == null) {
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
	public Response login(@Nonnull List<String> authorization, String role) {
		final int size = authorization.size();
		if (size != 1) {
			if (Strings.isNullOrEmpty(role) || !RoleModel.GUEST.name().equalsIgnoreCase(role)) {
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}

			return loginGuest();
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

		if (!scheme.equalsIgnoreCase(AuthenticationScheme.BASIC)) {
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

		AccountRole accountRole;

		if (role != null) {
			final var roleModel = RoleModel.fromValue(role);
			accountRole = roleModel != null ? accountRoleLocator.getByKey(account, roleModel).orElse(null) : null;
			if (accountRole == null) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
		} else {
			final var accountRoleList = accountRoleLocator.getForAccount(account);

			if (accountRoleList.size() == 0) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			accountRole = accountRoleList.get(0);
		}

		final var accessToken = new Token(accountRole);
		saveEntity(accessToken);

		updateAccountSaltHash(account, data);

		return activeLogin(account, accessToken, Duration.ofMinutes(10));
	}

	@SuppressWarnings("WeakerAccess")
	protected Response loginGuest() {
		final var utc = Instants.withZoneOffset(Instant.now(), ZoneOffset.UTC);

		final var year = utc.getYear();
		final var doy = utc.getDayOfYear() - 1;
		final var hour = utc.getHour();
		final var minute = utc.getMinute();
		final var second = utc.getSecond();
		final var milliSecond = utc.getNano() / 1_000_000;

		final var timeIndex = (0x1L << 35) + (((doy * 24L + hour) * 60L + minute) * 60L + second) * 1000L + milliSecond;
		final var guest = String.format("%04d%s", year % 10_000, Long.toString(timeIndex, 8));
		final var username = "g_" + guest;
		final var email = guest + "@mule_shell.guest.net";

		if (accountLocator.getByUsername(username).isPresent()) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		final var account = new Account(username, email);
		final var shellSession = new ShellSession(account);
		final var guestRole = roleLocator.getByKey(RoleModel.GUEST).orElseThrow(
				() -> new RuntimeException("Could not get role.")
		);
		final var accountRole = new AccountRole(account, guestRole, guestRole.getValue());
		final var accessToken = new Token(accountRole);

		saveEntity(account);
		saveEntity(shellSession);
		saveEntity(accountRole);
		saveEntity(accessToken);

		return activeLogin(account, accessToken, Duration.ofMinutes(360));
	}

	private Response activeLogin(Account account, Token accessToken, Duration duration) {
		final var tokenBuilder = JWT.create()
				.withJWTId(accessToken.getId().toString())
				.withExpiresAt(new Date(Instant.now().plus(duration).toEpochMilli()));

		final var signer = signerProvider.get();
		final var jwtToken = signer.sign(tokenBuilder);

		final var username = account.getUsername();
		final var accountRole = accessToken.getAccountRole();
		final var principal = new AccountPrincipal(username, accountRole.getRole());

		// Place in active cache.
		cache.put(accessToken.getId(), principal);

		JsonWebTokenModel tokenModel = new JsonWebTokenModel();
		tokenModel.setToken(jwtToken);

		return Response.ok(tokenModel).build();
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

	private void updateAccountSaltHash(final Account account, final byte[] data) {
		final var digest = accountSignerProvider.get();
		account.updateHash(digest, data);
		updateEntity(account);
	}

	private void setupRegisteredAccount(Account account, RoleModel roleModel) {
		// Check if account has this as an active role.
		if (accountRoleLocator.getByKey(account, roleModel).isEmpty()) {
			// Add with role.
			// TODO: Fix this with the needed checks.
			final var role = roleLocator.getByKey(Role.getKey(roleModel)).orElseThrow();
			final var accountRole = new AccountRole(account, role, role.getValue());
			saveEntity(accountRole);
		}

		// Check if account has active register role.
		accountRoleLocator.getByRoleName(account, RoleModel.REGISTER).ifPresent(registerRole -> {
			// Delete register account role.
			registerRole.delete();
			updateEntity(registerRole);
		});
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
