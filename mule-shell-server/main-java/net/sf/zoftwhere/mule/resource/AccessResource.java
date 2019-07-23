package net.sf.zoftwhere.mule.resource;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import net.sf.zoftwhere.mule.api.SecureApi;
import net.sf.zoftwhere.mule.model.BasicUserModel;
import net.sf.zoftwhere.mule.security.AccountPrincipal;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

public class AccessResource implements SecureApi {

	@Inject
	private Cache<UUID, AccountPrincipal> cache;

	@Override
	public Response login(List<String> authorization) {
		return Response.ok().build();
	}

	@Override
	public Response logout(BasicUserModel body) {
		return Response.ok().build();
	}
}
