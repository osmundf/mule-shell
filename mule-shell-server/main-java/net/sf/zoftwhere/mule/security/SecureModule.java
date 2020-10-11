package net.sf.zoftwhere.mule.security;

import java.util.function.Supplier;

import com.auth0.jwt.JWTVerifier;
import com.google.inject.Provides;
import net.sf.zoftwhere.mule.MuleConfiguration;
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule;

public class SecureModule extends DropwizardAwareModule<MuleConfiguration> {

	private final Supplier<JWTVerifier> verifier;

	private final Supplier<JWTSigner> signer;

	public SecureModule(Supplier<JWTSigner> signerProducer, Supplier<JWTVerifier> verifierProducer) {
		this.verifier = verifierProducer;
		this.signer = signerProducer;
	}

	@Provides
	public JWTVerifier getJWTVerifier() {
		return verifier.get();
	}

	@Provides
	public JWTSigner getJWTSigner() {
		return signer.get();
	}
}
