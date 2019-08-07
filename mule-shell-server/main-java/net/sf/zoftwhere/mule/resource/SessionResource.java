package net.sf.zoftwhere.mule.resource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import jdk.jshell.ImportSnippet;
import jdk.jshell.JShell;
import jdk.jshell.MethodSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.VarSnippet;
import net.sf.zoftwhere.dropwizard.AbstractResource;
import net.sf.zoftwhere.mule.api.SessionApi;
import net.sf.zoftwhere.mule.jpa.ShellSession;
import net.sf.zoftwhere.mule.jpa.ShellSessionLocator;
import net.sf.zoftwhere.mule.model.ImportSnippetModel;
import net.sf.zoftwhere.mule.model.MethodSnippetModel;
import net.sf.zoftwhere.mule.model.SnippetModel;
import net.sf.zoftwhere.mule.model.VariableSnippetModel;
import net.sf.zoftwhere.mule.shell.JShellManager;
import org.hibernate.Session;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SessionResource extends AbstractResource implements SessionApi {

	@Inject
	private Provider<SecurityContext> securityContextProvider;

	@Inject
	private JShellManager manager;

	private final ShellSessionLocator shellLocator;

	@Inject
	public SessionResource(Provider<Session> sessionProvider) {
		super(sessionProvider);
		this.shellLocator = new ShellSessionLocator(sessionProvider);
	}

	@Path("/time")
	@GET
	public Response getTime() {
		return Response.ok(Instant.now().atOffset(ZoneOffset.UTC)).build();
	}

	@RolesAllowed({CLIENT_ROLE})
	@Override
	public Response newSession() {
		final var security = securityContextProvider.get();

		final ShellSession shellSession = new ShellSession();

		wrapSession(session -> {
			session.beginTransaction();
			session.persist(shellSession);
			session.getTransaction().commit();
		});

		return Response.ok(ShellSession.asSessionModel(shellSession, ZoneOffset.UTC)).build();
	}

	@RolesAllowed({CLIENT_ROLE})
	@Override
	public Response getSession(String id, String tz) {
		final var security = securityContextProvider.get();

		// Get zone offset may throw a date time exception for invalid time zone.
		final var zoneOffset = getZoneOffset(tz).orElse(ZoneOffset.UTC);
		final var shellSession = tryFetchEntity(id, this::tryAsUUID, shellLocator::getById);

		// TODO: Add shell session access tokens (owner, user, visitor)
		if (shellSession.isEmpty()) {
			return Response.ok(Response.Status.BAD_REQUEST).entity("Session unavailable.").build();
		}

		final var model = ShellSession.asSessionModel(shellSession.get(), zoneOffset);
		return Response.ok().entity(model).build();
	}

	@Override
	public Response getSessionSnippetArray(String sessionId) {
		return this.transform(sessionId, JShell::snippets, Objects::toString, this::generalSnippet);
	}

	@Override
	public Response getSessionImportArray(String sessionId) {
		return this.transform(sessionId, JShell::imports, Objects::toString, this::importSnippet);
	}

	@Override
	public Response getSessionVariableArray(String sessionId) {
		return this.transform(sessionId, JShell::variables, Objects::toString, this::variableSnippet);
	}

	@Override
	public Response getSessionMethodArray(String sessionId) {
		return this.transform(sessionId, JShell::methods, Objects::toString, this::methodSnippet);
	}

	public <S, I, M> Response transform(String id, Function<JShell, Stream<S>> getList, Function<Integer, I> indexer, BiFunction<S, I, M> combiner) {
		final var jshell = manager.getJShell(UUID.fromString(id));
		final var modelList = new ArrayList<M>();
		final var snippetList = getList.apply(jshell).collect(Collectors.toList());

		for (int i = 0, size = snippetList.size(); i < size; i++) {
			final var snippet = snippetList.get(i);
			final var index = indexer.apply(i);
			modelList.add(combiner.apply(snippet, index));
		}

		return Response.ok(modelList).build();
	}

	public SnippetModel generalSnippet(final Snippet snippet, final String index) {
		final var result = new SnippetModel();
		result.setId(snippet.id());
		result.setIndex(index);

		if (snippet instanceof ImportSnippet) {
			result.setType("Import Snippet");
			final var var = (ImportSnippet) snippet;
			result.setName(var.name());

		} else if (snippet instanceof VarSnippet) {
			result.setType("Variable Snippet");
			final var var = (VarSnippet) snippet;
			result.setName(var.typeName() + " " + var.name());

		} else if (snippet instanceof MethodSnippet) {
			result.setType("Method Snippet");
			final var var = (MethodSnippet) snippet;
			result.setName(var.name());

		} else {
			result.setType("Generic");
			result.setName("");
		}

		result.setSource(snippet.source());
		return result;
	}

	public ImportSnippetModel importSnippet(final ImportSnippet snippet, final String index) {
		final var result = new ImportSnippetModel();
		result.setId(snippet.id());
		result.setIndex(index);
		result.setName(snippet.name());
		result.setSource(snippet.source());
		return result;
	}

	public VariableSnippetModel variableSnippet(final VarSnippet snippet, final String index) {
		final var result = new VariableSnippetModel();
		result.setId(snippet.id());
		result.setIndex(index);
		result.setName(snippet.typeName() + " " + snippet.name());
		result.setSource(snippet.source());
		return result;
	}

	public MethodSnippetModel methodSnippet(final MethodSnippet snippet, final String index) {
		final var result = new MethodSnippetModel();
		result.setId(snippet.id());
		result.setIndex(index);
		result.setName(snippet.name() + " " + snippet.signature());
		result.setSource(snippet.source());
		return result;
	}
}
