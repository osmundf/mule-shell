package net.sf.zoftwhere.mule.resource;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import jdk.jshell.JShell;
import net.sf.zoftwhere.dropwizard.AbstractResource;
import net.sf.zoftwhere.mule.api.SessionApi;
import net.sf.zoftwhere.mule.jdk.jshell.MuleSnippet;
import net.sf.zoftwhere.mule.jpa.AccountLocator;
import net.sf.zoftwhere.mule.jpa.ShellSession;
import net.sf.zoftwhere.mule.jpa.ShellSessionLocator;
import net.sf.zoftwhere.mule.server.JShellManager;
import org.hibernate.Session;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.sf.zoftwhere.mule.jpa.ShellSession.asSessionModel;

public class SessionResource extends AbstractResource implements SessionApi {

	@Inject
	private Provider<SecurityContext> securityContextProvider;

	@Inject
	private Cache<UUID, JShell> shellCache;

	private final AccountLocator accountLocator;

	private final ShellSessionLocator shellSessionLocator;

	@Inject
	public SessionResource(Provider<Session> sessionProvider) {
		super(sessionProvider);
		this.accountLocator = new AccountLocator(sessionProvider);
		this.shellSessionLocator = new ShellSessionLocator(sessionProvider);
	}

	@RolesAllowed({CLIENT_ROLE})
	@Override
	public Response newSession() {
		final var security = securityContextProvider.get();

		final var account = accountLocator.getByUsername(security.getUserPrincipal().getName()).orElseThrow();
		final var shellSession = new ShellSession(account);
		saveEntity(shellSession);

		return Response.ok(asSessionModel(shellSession, ZoneOffset.UTC)).build();
	}

	@RolesAllowed({CLIENT_ROLE, GUEST_ROLE})
	@Override
	public Response getSession(String id, String tz) {
		final var security = securityContextProvider.get();

		// TODO: !security.isSecure()
		if (security == null || security.getUserPrincipal() == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		// Get zone offset may throw a date time exception for invalid time zone.
		final var zoneOffset = getZoneOffset(tz).orElse(ZoneOffset.UTC);
		final var account = accountLocator.getByUsername(security.getUserPrincipal().getName()).orElse(null);
		final var shellSession = shellSessionLocator.getForIdAndAccount(tryAsUUID(id).orElse(null), account);

		// TODO: Add shell session access tokens (owner, user, visitor)
		if (shellSession.isEmpty()) {
			return Response.ok(Response.Status.BAD_REQUEST).entity("Session unavailable.").build();
		}

		final var model = asSessionModel(shellSession.get(), zoneOffset);
		return Response.ok().entity(model).build();
	}

	@RolesAllowed({CLIENT_ROLE, GUEST_ROLE})
	@Override
	public Response getSessionList() {
		final var security = securityContextProvider.get();

		// TODO: !security.isSecure()
		if (security == null || security.getUserPrincipal() == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		final var name = security.getUserPrincipal().getName();
		final var account = accountLocator.getByUsername(name).orElseThrow();
		final var sessionList = shellSessionLocator.getForAccount(account);

		final var list = sessionList.stream()
				.map(shellSession -> asSessionModel(shellSession, ZoneOffset.UTC))
				.collect(Collectors.toList());

		return Response.ok(list).build();
	}

	@RolesAllowed({CLIENT_ROLE, GUEST_ROLE})
	@Override
	public Response getSessionSnippetArray(@Nonnull String sessionId) {
		return this.getModelList(sessionId, JShell::snippets, Objects::toString, MuleSnippet::generalSnippet);
	}

	@RolesAllowed({CLIENT_ROLE, GUEST_ROLE})
	@Override
	public Response getSessionImportArray(@Nonnull String sessionId) {
		return this.getModelList(sessionId, JShell::imports, Objects::toString, MuleSnippet::importSnippet);
	}

	@RolesAllowed({CLIENT_ROLE, GUEST_ROLE})
	@Override
	public Response getSessionVariableArray(@Nonnull String sessionId) {
		return this.getModelList(sessionId, JShell::variables, Objects::toString, MuleSnippet::variableSnippet);
	}

	@RolesAllowed({CLIENT_ROLE, GUEST_ROLE})
	@Override
	public Response getSessionMethodArray(@Nonnull String sessionId) {
		return this.getModelList(sessionId, JShell::methods, Objects::toString, MuleSnippet::methodSnippet);
	}

	@SuppressWarnings("Duplicates")
	public <S, I, M> Response getModelList(String id, Function<JShell, Stream<S>> getList, Function<Integer, I> indexer, BiFunction<S, I, M> combiner) {
		final var security = securityContextProvider.get();
		final var account = accountLocator.getByUsername(security.getUserPrincipal().getName()).orElseThrow();
		final var manager = new JShellManager(shellCache, shellSessionLocator);
		final var shell = manager.getJShell(tryAsUUID(id).orElse(null), account).orElse(null);

		if (shell == null) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		return Response.ok(MuleSnippet.getModelList(shell, getList, indexer, combiner)).build();
	}
}
