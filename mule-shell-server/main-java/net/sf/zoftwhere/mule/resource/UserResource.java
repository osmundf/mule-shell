package net.sf.zoftwhere.mule.resource;

import com.codahale.metrics.annotation.Timed;
import org.jdbi.v3.core.Jdbi;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

	private final Jdbi jdbi;

	@Inject
	public UserResource(Jdbi jdbi) {
		this.jdbi = jdbi;
	}

	@GET
	@Path("/{type}")
	public String getType(@PathParam("type") String type) {
//		jdbi.open().begin().createCall("").invoke();

		return "(" + type + ")";
	}

	@GET
	@Timed
	public String sayHello(@QueryParam("name") Optional<String> name) {
		return "{ \"Hello\": \"World\" }";
	}
}
