package net.sf.zoftwhere.mule.security;

import com.auth0.jwt.JWTVerifier;
import com.google.inject.Provides;
import io.dropwizard.setup.Bootstrap;
import net.sf.zoftwhere.mule.MuleConfiguration;
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule;

import java.util.function.Supplier;

public class SecureModule extends DropwizardAwareModule<MuleConfiguration> {

	private final Supplier<JWTVerifier> verifier;

	private final Supplier<JWTSigner> signer;

	public SecureModule(Supplier<JWTSigner> signerProducer, Supplier<JWTVerifier> verifierProducer) {
		this.verifier = verifierProducer;
		this.signer = signerProducer;
	}

	@Override
	protected Bootstrap<MuleConfiguration> bootstrap() {
		return super.bootstrap();
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
