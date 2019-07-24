package net.sf.zoftwhere.mule.resource;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/asset")
@Produces(MediaType.APPLICATION_JSON)
public class AssetResource {

	public AssetResource() {
	}

	@GET
	@Path("/{type}")
	public String getType(@PathParam("type") String type) {
		return "(" + type + ")";
	}

	@GET
	@Timed
	public String sayHello(@QueryParam("name") @DefaultValue("World") String name) {
		return "{ \"Hello\": \"" + name + "\" }";
	}
}
