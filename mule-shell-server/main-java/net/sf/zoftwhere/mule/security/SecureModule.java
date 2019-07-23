package net.sf.zoftwhere.mule.security;

import com.auth0.jwt.interfaces.JWTVerifier;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import net.sf.zoftwhere.mule.MuleConfiguration;
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule;

public class SecureModule extends DropwizardAwareModule<MuleConfiguration> {

	private final JWTVerifier verifier;

	private final JWTSigner signer;

	public SecureModule(JWTVerifier verifier, JWTSigner signer) {
		this.verifier = verifier;
		this.signer = signer;
	}

	@Provides
	@Singleton
	public JWTVerifier getJWTVerifier() {
		return verifier;
	}

	@Provides
	@Singleton
	public JWTSigner getJWTSigner() {
		return signer;
	}

}
