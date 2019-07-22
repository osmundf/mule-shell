package net.sf.zoftwhere.mule.resource;

import com.google.inject.Inject;
import jdk.jshell.JShell;
import net.sf.zoftwhere.mule.api.ExpressionApi;
import net.sf.zoftwhere.mule.model.ExpressionModel;
import net.sf.zoftwhere.mule.shell.JShellManager;
import net.sf.zoftwhere.mule.shell.UUIDBuffer;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Random;
import java.util.UUID;

public class ExpressionResource implements ExpressionApi {

	@Inject
	private JShellManager manager;

	@Inject
	private UUIDBuffer buffer;

	public ExpressionResource() {
	}

	@Override
	public Response postExpression(String sessionId, String expressionType, ExpressionModel expression) {
		final var sessionUUID = UUID.fromString(sessionId);

		final var entity = new ExpressionModel();
		entity.setInput(expression.getInput());
		entity.setOutput("Done.");

		return Response.ok(entity, MediaType.APPLICATION_JSON_TYPE).build();
	}

	public Response putExpression(String sessionId, ExpressionModel body, String expressionType) {

		int i = 0;
		while (i < 100) {
			try {
				final var a1 = manager.newJShell(new UUIDBuffer(new Random(0))).orElseThrow();
				++i;
				System.out.printf("k:%s (%s) %n", a1.getKey(), manager.size());
			} catch (RuntimeException e) {
				System.out.println(e.getMessage());
			}
		}

		JShell shell = manager.newJShell(buffer).orElseThrow().getValue();

		return Response.ok("{'size':" + manager.size() + " }", MediaType.APPLICATION_JSON_TYPE).build();
	}
}
