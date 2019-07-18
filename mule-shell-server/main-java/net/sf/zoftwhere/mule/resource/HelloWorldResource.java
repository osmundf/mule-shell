package net.sf.zoftwhere.mule.resource;

import net.sf.zoftwhere.mule.api.HelloApi;
import net.sf.zoftwhere.mule.model.HelloWorldObjectModel;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class HelloWorldResource implements HelloApi {

	public HelloWorldResource() {
	}

	@Override
	public Response getHelloWorlds() {
		List<HelloWorldObjectModel> result = new ArrayList<>();
		result.add(new HelloWorldObjectModel());
		return Response.ok().entity(result).build();
	}

	@Override
	public Response putHelloWorld(HelloWorldObjectModel request) {
		return Response.ok().build();
	}
}
