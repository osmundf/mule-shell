package net.sf.zoftwhere.mule.resource;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import jdk.jshell.JShell;
import net.sf.zoftwhere.dropwizard.AbstractResource;
import net.sf.zoftwhere.mule.api.ExpressionApi;
import net.sf.zoftwhere.mule.jdk.jshell.MuleSnippetEvent;
import net.sf.zoftwhere.mule.jpa.AccountLocator;
import net.sf.zoftwhere.mule.jpa.ShellSessionLocator;
import net.sf.zoftwhere.mule.model.ExpressionModel;
import net.sf.zoftwhere.mule.model.ExpressionResultModel;
import net.sf.zoftwhere.mule.server.JShellManager;
import org.hibernate.Session;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;
import java.util.stream.Collectors;

import static jdk.jshell.Snippet.Status.OVERWRITTEN;

public class ExpressionResource extends AbstractResource implements ExpressionApi {

	@Inject
	private Provider<SecurityContext> securityContextProvider;

	@Inject
	private Cache<UUID, JShell> shellCache;

	private final AccountLocator accountLocator;

	private final ShellSessionLocator shellSessionLocator;

	@Inject
	public ExpressionResource(Provider<Session> sessionProvider) {
		super(sessionProvider);
		this.accountLocator = new AccountLocator(sessionProvider);
		this.shellSessionLocator = new ShellSessionLocator(sessionProvider);
	}

	@RolesAllowed({CLIENT_ROLE, GUEST_ROLE})
	@Override
	@SuppressWarnings("Duplicates")
	public Response postExpression(@Nonnull String sessionId, ExpressionModel expression, @Nonnull String expressionType) {
		final var security = securityContextProvider.get();
		final var account = accountLocator.getByUsername(security.getUserPrincipal().getName()).orElseThrow();
		final var manager = new JShellManager(shellCache, shellSessionLocator);
		final var shell = manager.getJShell(tryAsUUID(sessionId).orElse(null), account).orElse(null);
		final var code = expression != null && expression.getInput() != null ? expression.getInput() : null;

		if (shell == null) {
			// TODO: Extend to use session privileges (owner, viewer, visitor)
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("The session does not exist or is no longer active.")
					.build();
		}

		final var result = shell.eval(code);

		// TODO: Handle sections of complete code as well as possible "incomplete" code.
		// TODO: Handle exceptions in code input submitted.
		final var list = result.stream()
				.map(MuleSnippetEvent::new)
				.filter(event -> event.getStatus() != OVERWRITTEN)
				.map(MuleSnippetEvent::getEventOutput)
				.collect(Collectors.toList());

		final var entity = new ExpressionResultModel()
				.input(code)
				.output(list)
				.continuation(false);

		return Response.ok(entity, MediaType.APPLICATION_JSON_TYPE).build();
	}
}
