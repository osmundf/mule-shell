package net.sf.zoftwhere.mule.resource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.ContextPath;
import net.sf.zoftwhere.dropwizard.MuleInfo;
import net.sf.zoftwhere.dropwizard.ViewAssetPath;
import net.sf.zoftwhere.mule.model.RoleModel;
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

@Path("/")
public class WebResource {

	private static final URI index = URI.create("/mule-shell");

	@Inject
	private Provider<MuleInfo> muleInfoProvider;

	@Inject
	private Provider<ContextPath> contextPathProvider;

	@Inject
	private Provider<ViewAssetPath> viewAssetPathProvider;

	public WebResource() {
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public IndexView getRoot() {
		final var contextPath = contextPathProvider.get();
		final var muleInfo = muleInfoProvider.get();
		final var viewAssetPath = viewAssetPathProvider.get();

		return getIndexPage(contextPath, muleInfo, viewAssetPath);
	}

	@GET
	@Path("/{request-page}")
	public Response getRootIndex(@PathParam("request-page") String page) {
		final var contextPath = contextPathProvider.get();
		final var muleInfo = muleInfoProvider.get();
		final var viewAssetPath = viewAssetPathProvider.get();

		if (requestingPage(page, "index", "index.html")) {
			return Response.ok(new IntroView(contextPath)).build();
		}

		if (requestingPage(page, "intro", "intro.html")) {
			return Response.ok(new IntroView(contextPath)).build();
		}

		// TODO Access to the view should be updated to reflect client access.
		if (requestingPage(page, "console", "console.html")) {
			return Response.ok(new ConsoleView(RoleModel.CLIENT, contextPath, muleInfo, viewAssetPath)).build();
		}

		// TODO Access to the view should be updated to reflect guest access and login.
		if (requestingPage(page, "guest", "guest.html")) {
			return Response.ok(new ConsoleView(RoleModel.GUEST, contextPath, muleInfo, viewAssetPath)).build();
		}

		return Response.temporaryRedirect(index).build();
	}

	protected IndexView getIndexPage(ContextPath contextPath, MuleInfo muleInfo, ViewAssetPath viewAssetPath) {
		return new IndexView(contextPath, muleInfo, viewAssetPath);
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
