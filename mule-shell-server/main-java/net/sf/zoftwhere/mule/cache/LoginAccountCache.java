package net.sf.zoftwhere.mule.cache;

import com.google.common.cache.Cache;
import net.sf.zoftwhere.mule.security.AccountPrincipal;

import java.util.UUID;

public interface LoginAccountCache extends Cache<UUID, AccountPrincipal> {

}
