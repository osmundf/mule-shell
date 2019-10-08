package net.sf.zoftwhere.mule.resource;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractResource;
import net.sf.zoftwhere.mule.api.ExpressionApi;
import net.sf.zoftwhere.mule.model.ExpressionModel;
import net.sf.zoftwhere.mule.model.ExpressionResultModel;
import net.sf.zoftwhere.mule.shell.MuleShell;
import org.hibernate.Session;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class ExpressionResource extends AbstractResource implements ExpressionApi {

	@Inject
	private Provider<ExecutorService> executorServiceProvider;

	@Inject
	private Provider<SecurityContext> securityContextProvider;

	@Inject
	private Cache<UUID, MuleShell> shellCache;

	@Inject
	public ExpressionResource(Provider<Session> sessionProvider) {
		super(sessionProvider);
	}

	@RolesAllowed({CLIENT_ROLE, GUEST_ROLE})
	@Override
	public Response postExpression(@Nonnull String sessionId, @Nonnull ExpressionModel expression, @Nonnull String expressionType) {
		final var username = securityContextProvider.get().getUserPrincipal().getName();
		return wrapMuleShell(shellCache, username, sessionId, (MuleShell shell) -> {
			final var code = expression.getInput();

			if (shell == null) {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity("The session does not exist or is no longer active.")
						.build();
			}

			if (shell.isClosed()) {
				final var entity = new ExpressionResultModel()
						.input(code)
						.output(null)
						.continuation(false)
						.error("error.shell.closed");
				return Response.ok(entity, MediaType.APPLICATION_JSON_TYPE).build();
			}

			final var eval = shell.eval(code);

			final var entity = new ExpressionResultModel()
					.input(code)
					.output(eval.getSnippetList())
					.remainingCode(eval.getRemainingCode())
					.continuation(!Strings.isNullOrEmpty(eval.getRemainingCode()));

			return Response.ok(entity, MediaType.APPLICATION_JSON_TYPE).build();
		});
	}
}
