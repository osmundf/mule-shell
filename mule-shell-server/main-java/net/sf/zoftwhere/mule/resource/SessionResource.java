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
import net.sf.zoftwhere.mule.jpa.SessionLocator;
import net.sf.zoftwhere.mule.model.ImportSnippetModel;
import net.sf.zoftwhere.mule.model.MethodSnippetModel;
import net.sf.zoftwhere.mule.model.SessionModel;
import net.sf.zoftwhere.mule.model.SnippetModel;
import net.sf.zoftwhere.mule.model.VariableSnippetModel;
import net.sf.zoftwhere.mule.shell.JShellManager;
import net.sf.zoftwhere.mule.shell.UUIDBuffer;
import org.hibernate.Session;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SessionResource extends AbstractResource implements SessionApi {

	private final SessionLocator sessionLocator;
	@Inject
	private JShellManager manager;
	@Inject
	private UUIDBuffer buffer;

	@Inject
	public SessionResource(Provider<Session> sessionProvider) {
		this.sessionLocator = new SessionLocator(sessionProvider);
	}

	public SessionResource() {
		this.sessionLocator = null;
	}

	@GET
	@Path("/{id}")
	public Response getShellSession(@PathParam("id") String id) {
//		Letter letter = tryAsInteger(id).map(letterLocator::getById).orElseThrow(() -> entityNotFound("Letter", id));
//		LetterModel model = Letter.asLetterModel(letter);
//		return Response.ok().entity(model).build();
		return Response.ok().build();
	}

	@Override
	public Response getSession() {
		final var entry = manager.newJShell(buffer);
		final var uuid = entry.getKey();
		final var jshell = entry.getValue();

		final var entity = new SessionModel();
		entity.setId(uuid);
		entity.setVariableCount(jshell.variables().count());

		return Response.ok(entity).build();
	}

	@Override
	public Response getSessionSnippetArray(String sessionId) {
		return this.transform(sessionId, JShell::snippets, this::generalSnippet);
	}

	@Override
	public Response getSessionImportArray(String sessionId) {
		return this.transform(sessionId, JShell::imports, this::importSnippet);
	}

	@Override
	public Response getSessionVariableArray(String sessionId) {
		return this.transform(sessionId, JShell::variables, this::variableSnippet);
	}

	@Override
	public Response getSessionMethodArray(String sessionId) {
		return this.transform(sessionId, JShell::methods, this::methodSnippet);
	}

	public <S, M> Response transform(String sessionId, Function<JShell, Stream<S>> getSnippetList, BiFunction<S, String, M> populateModel) {
		final var jshell = manager.getJShell(UUID.fromString(sessionId));
		final var entity = new ArrayList<M>();

		List<S> snippetList = getSnippetList.apply(jshell).collect(Collectors.toList());
		for (int i = 0, size = snippetList.size(); i < size; i++) {
			entity.add(populateModel.apply(snippetList.get(i), Integer.toString(i)));
		}

		return Response.ok(entity).build();
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
