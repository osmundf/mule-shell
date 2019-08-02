package net.sf.zoftwhere.mule.resource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.ContextPath;
import net.sf.zoftwhere.mule.model.RoleModel;
import net.sf.zoftwhere.mule.view.AbstractView;
import net.sf.zoftwhere.mule.view.ConsoleView;
import net.sf.zoftwhere.mule.view.IndexView;
import net.sf.zoftwhere.mule.view.IntroView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Path("/")
public class WebResource {

	private static final URI index = URI.create("/mule-shell");

	@Inject
	private Provider<ContextPath> contextPathProvider;

	public WebResource() {
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public IndexView getRoot() {
		final var contextPath = contextPathProvider.get();
		return getIndexPage(contextPath);
	}

	@GET
	@Path("/{request-page}")
	public Response getRootIndex(@PathParam("request-page") String page) {
		final var contextPath = contextPathProvider.get();

		if (requestingPage(page, "index", "index.html")) {
			return Response.ok(new IntroView(contextPath)).build();
		}

		if (requestingPage(page, "home", "home.html")) {
			// net/sf/zoftwhere/mule/view/home.ftl
			var view = new AbstractView("/net/sf/zoftwhere/mule/view/home.ftl", StandardCharsets.UTF_8, contextPath) {};
			return Response.ok(view).build();
		}

		if (requestingPage(page, "intro", "intro.ftl", "intro.html")) {
			return Response.ok(new IntroView(contextPath)).build();
		}

		if (requestingPage(page, "console", "console.html")) {
			return Response.ok(new ConsoleView(RoleModel.CLIENT, contextPath)).build();
		}

		if (requestingPage(page, "guest", "guest.html")) {
			return Response.ok(new ConsoleView(RoleModel.GUEST, contextPath)).build();
		}

		return Response.temporaryRedirect(index).build();
	}

	protected IndexView getIndexPage(ContextPath contextPath) {
		return new IndexView(contextPath);
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
