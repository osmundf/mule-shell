package net.sf.zoftwhere.mule.resource;

import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractResource;
import net.sf.zoftwhere.mule.api.LetterApi;
import net.sf.zoftwhere.mule.jpa.Letter;
import net.sf.zoftwhere.mule.jpa.LetterLocator;
import net.sf.zoftwhere.mule.model.LetterModel;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/letter")
@Produces(MediaType.APPLICATION_JSON)
public class LetterResource extends AbstractResource implements LetterApi {

	private final LetterLocator letterLocator;

	@Inject
	public LetterResource(Provider<Session> sessionProvider) {
		letterLocator = new LetterLocator(sessionProvider);
	}

	@GET
	@Path("/{id}")
	public Response getLetter(@PathParam("id") String id) {
		Letter letter = tryAsInteger(id).map(letterLocator::getById).orElseThrow(() -> entityNotFound("Letter", id));
		LetterModel model = Letter.asLetterModel(letter);
		return Response.ok().entity(model).build();
	}
}
