package net.sf.zoftwhere.mule.resource;

import net.sf.zoftwhere.mule.model.RoleModel;
import net.sf.zoftwhere.mule.view.ConsoleView;
import net.sf.zoftwhere.mule.view.LogInView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/")
public class WebResource {

	private static final URI index = URI.create("/mule-shell");

	@GET
	@Produces(MediaType.TEXT_HTML)
	public LogInView getRoot() {
		return getIndexPage();
	}

	@GET
	@Path("/{request-page}")
	public Response getRootIndex(@PathParam("request-page") String page) {
		if (requestingPage(page, "intro", "intro.html")) {
			return Response.ok(new LogInView()).build();
		}

		if (requestingPage(page, "index", "index.html")) {
			return Response.ok(new LogInView()).build();
		}

		if (requestingPage(page, "console", "console.html")) {
			return Response.ok(new ConsoleView(RoleModel.CLIENT)).build();
		}

		if (requestingPage(page, "guest", "guest.html")) {
			return Response.ok(new ConsoleView(RoleModel.GUEST)).build();
		}

		return Response.temporaryRedirect(index).build();
	}

	protected LogInView getIndexPage() {
		return new LogInView();
	}

	protected boolean requestingPage(String page, String... pages) {
		if (page == null) {
			return false;
		}

		for (String entry : pages) {
			if (entry.equalsIgnoreCase(page)) {
				return true;
			}
		}

		return false;
	}
}
