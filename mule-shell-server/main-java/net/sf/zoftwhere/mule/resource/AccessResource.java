package net.sf.zoftwhere.mule.resource;

import net.sf.zoftwhere.mule.api.SecureApi;
import net.sf.zoftwhere.mule.model.BasicUserModel;

import javax.ws.rs.core.Response;
import java.util.List;

public class AccessResource implements SecureApi {

	@Override
	public Response login(List<String> authorization) {
		return Response.ok().build();
	}

	@Override
	public Response logout(BasicUserModel body) {
		return Response.ok().build();
	}
}
