package net.sf.zoftwhere.dropwizard;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.UUID;
import javax.ws.rs.core.SecurityContext;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import net.sf.zoftwhere.mule.data.Variable;
import net.sf.zoftwhere.mule.security.AccountPrincipal;
import net.sf.zoftwhere.mule.security.AccountSigner;
import net.sf.zoftwhere.mule.security.JWTSigner;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public abstract class TestInjection {

	public static Injector newTestGuiceInjector(final SessionFactory sessionFactory) {

		final var securityContext = new Variable<SecurityContext>();
		final var algorithm = Algorithm.HMAC256("test-public-secret");
		final var issuer = "mule-server-test";
		final var signer = new JWTSigner(issuer, algorithm);
		final var verifier = JWT.require(algorithm)
			.withIssuer(issuer)
			.acceptIssuedAt(0)
			.acceptNotBefore(0)
			.acceptExpiresAt(0)
			.build();

		return Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(SessionFactory.class).toProvider(() -> sessionFactory).in(Singleton.class);
				bind(Session.class).toProvider(sessionFactory::openSession);
				bind(JWTVerifier.class).toInstance(verifier);
				bind(JWTSigner.class).toInstance(signer);

				final var cacheKey = new Key<Cache<UUID, AccountPrincipal>>() { };
				bind(cacheKey).toProvider(this::getLoginCache).in(Singleton.class);
				bind(AccountSigner.class).toProvider(this::getAccountSigner);

				final var securityKey = new Key<Variable<SecurityContext>>() { };
				bind(securityKey).toInstance(securityContext);
				bind(SecurityContext.class).toProvider(securityContext::get);
			}

			private Cache<UUID, AccountPrincipal> getLoginCache() {
				return CacheBuilder.newBuilder()
					.maximumSize(10000)
					.expireAfterWrite(Duration.ofMinutes(30))
					.build();
			}

			private AccountSigner getAccountSigner() {
				try {
					return new AccountSigner(MessageDigest.getInstance("SHA-256"), 6);
				}
				catch (NoSuchAlgorithmException e) {
					return null;
				}
			}
		});
	}
}
