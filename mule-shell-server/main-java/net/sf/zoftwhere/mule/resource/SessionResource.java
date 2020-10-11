package net.sf.zoftwhere.mule.resource;

import java.time.ZoneOffset;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import jdk.jshell.MethodSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.TypeDeclSnippet;
import jdk.jshell.VarSnippet;
import net.sf.zoftwhere.dropwizard.AbstractResource;
import net.sf.zoftwhere.mule.api.SessionApi;
import net.sf.zoftwhere.mule.jpa.AccountLocator;
import net.sf.zoftwhere.mule.jpa.ShellSession;
import net.sf.zoftwhere.mule.jpa.ShellSessionLocator;
import net.sf.zoftwhere.mule.model.MethodSnippetModel;
import net.sf.zoftwhere.mule.model.TypeSnippetModel;
import net.sf.zoftwhere.mule.model.VariableSnippetModel;
import net.sf.zoftwhere.mule.shell.MuleShell;
import net.sf.zoftwhere.mule.shell.MuleSnippet;
import org.hibernate.Session;

import static net.sf.zoftwhere.mule.jpa.ShellSession.asSessionModel;

public class SessionResource extends AbstractResource implements SessionApi {

	@Inject
	private Provider<SecurityContext> securityContextProvider;

	@Inject
	private Cache<UUID, MuleShell> shellCache;

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

		final var username = security.getUserPrincipal().getName();
		final var account = accountLocator.getByUsername(username).orElseThrow();
		final var sessionList = shellSessionLocator.getForAccount(account);

		final var list = sessionList.stream()
			.map(shellSession -> asSessionModel(shellSession, ZoneOffset.UTC))
			.collect(Collectors.toList());

		return Response.ok(list).build();
	}

	@RolesAllowed({CLIENT_ROLE, GUEST_ROLE})
	@Override
	public Response getSessionSnippetArray(@Nonnull String sessionId, @Nullable String snippetId) {
		Function<MuleShell, Stream<Snippet>> getList = snippetId == null
			? MuleShell::snippets
			: muleShell -> muleShell.snippets().filter(s -> snippetId.equals(s.id()));

		return this.getModelList(sessionId, getList, MuleSnippet::generalSnippet);
	}

	@RolesAllowed({CLIENT_ROLE, GUEST_ROLE})
	@Override
	public Response getSessionImportArray(@Nonnull String sessionId) {
		return this.getModelList(sessionId, MuleShell::imports, (m, snippet) -> MuleSnippet.importSnippet(snippet));
	}

	@RolesAllowed({CLIENT_ROLE, GUEST_ROLE})
	@Override
	public Response getSessionVariableArray(@Nonnull String sessionId, @Nullable String variableName) {
		Function<MuleShell, Stream<VarSnippet>> getStream = (variableName == null)
			? MuleShell::variables
			: muleShell -> muleShell.variables().filter(s -> variableName.equals(s.name()));

		BiFunction<MuleShell, VarSnippet, VariableSnippetModel> toModel = (variableName == null)
			? (muleShell, snippet) -> MuleSnippet.variableSnippet(snippet, null)
			: (muleShell, snippet) -> MuleSnippet.variableSnippet(snippet, muleShell.varValue(snippet));

		return this.getModelList(sessionId, getStream, toModel);
	}

	@RolesAllowed({CLIENT_ROLE, GUEST_ROLE})
	@Override
	public Response getSessionMethodArray(@Nonnull String sessionId, @Nullable String methodName) {
		Function<MuleShell, Stream<MethodSnippet>> getStream = methodName == null
			? MuleShell::methods
			: muleShell -> muleShell.methods().filter(s -> methodName.equals(s.name()));

		BiFunction<MuleShell, MethodSnippet, MethodSnippetModel> toModel =
			(m, snippet) -> MuleSnippet.methodSnippet(snippet);

		return this.getModelList(sessionId, getStream, toModel);
	}

	@RolesAllowed({CLIENT_ROLE, GUEST_ROLE})
	@Override
	public Response getSessionTypeArray(@Nonnull String sessionId, @Nullable String typeName) {
		Function<MuleShell, Stream<TypeDeclSnippet>> getStream = typeName == null
			? MuleShell::types
			: muleShell -> muleShell.types().filter(s -> typeName.equals(s.name()));

		BiFunction<MuleShell, TypeDeclSnippet, TypeSnippetModel> toModel =
			(m, snippet) -> MuleSnippet.typeSnippet(snippet);

		return this.getModelList(sessionId, getStream, toModel);
	}

	public <S, M> Response getModelList(String shellId, Function<MuleShell, Stream<S>> getStream,
		BiFunction<MuleShell, S, M> toModel)
	{
		final var username = securityContextProvider.get().getUserPrincipal().getName();
		return wrapMuleShell(shellCache, username, shellId, (MuleShell shell) -> {
			if (shell == null) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}

			final var entity = MuleSnippet.getModelList(shell, getStream, toModel);
			return Response.ok(entity).build();
		});
	}
}
