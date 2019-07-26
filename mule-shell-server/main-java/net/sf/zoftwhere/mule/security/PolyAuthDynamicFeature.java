package net.sf.zoftwhere.mule.security;

import com.auth0.jwt.JWTVerifier;
import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import io.dropwizard.auth.PolymorphicAuthDynamicFeature;
import io.dropwizard.auth.PolymorphicAuthValueFactoryProvider;
import io.dropwizard.setup.Environment;
import net.sf.zoftwhere.dropwizard.security.AuthorizationAuthFilter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.hibernate.SessionFactory;

import java.util.UUID;

/**
 * Special thanks to Dropwizard-Guicey.
 */
//@Provider
public class PolyAuthDynamicFeature extends PolymorphicAuthDynamicFeature<AccountPrincipal> {

	@Inject
	public PolyAuthDynamicFeature(Environment environment, Cache<UUID, AccountPrincipal> cache, JWTVerifier verifier, SessionFactory sessionFactory) {
//		super(null);
		super(ImmutableMap.of(
				AccountPrincipal.class, new AuthorizationAuthFilter.Builder<AccountPrincipal>()
						.setAuthenticator(new AccountAuthenticator(cache, verifier, sessionFactory::openSession))
						.setAuthorizer(new AccountAuthorizer())
						.setPrefix("Bearer")
						.setRealm("public")
						.buildAuthFilter()));

		final AbstractBinder binder = new PolymorphicAuthValueFactoryProvider.Binder<>(
				ImmutableSet.of(AccountPrincipal.class));

		environment.jersey().register(binder);
		environment.jersey().register(RolesAllowedDynamicFeature.class);
	}
}