package net.sf.zoftwhere.mule.resource;

import jdk.jshell.JShell;
import net.sf.zoftwhere.mule.api.ExpressionApi;
import net.sf.zoftwhere.mule.model.ExpressionModel;
import net.sf.zoftwhere.mule.shell.JShellManager;
import net.sf.zoftwhere.mule.shell.UUIDBuffer;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Random;

public class ExpressionResource implements ExpressionApi {

	@Inject
	public JShellManager manager;
	private UUIDBuffer buffer;

	public ExpressionResource() {
		buffer = new UUIDBuffer(new Random(0));
	}

	@Override
	public Response putExpression(String sessionId, ExpressionModel body, String expressionType) {

		int i = 0;
		while (i < 100) {
			try {
				final var a1 = manager.newJShell(new UUIDBuffer(new Random(0)));
				++i;
				System.out.printf("k:%s (%s) %n", a1.getKey(), manager.size());
			} catch (RuntimeException e) {
				System.out.println(e.getMessage());
			}
		}

		JShell shell = manager.newJShell(new UUIDBuffer(new Random())).getValue();

		return Response.ok("{'size':" + manager.size() + " }", MediaType.APPLICATION_JSON_TYPE).build();
	}
}
