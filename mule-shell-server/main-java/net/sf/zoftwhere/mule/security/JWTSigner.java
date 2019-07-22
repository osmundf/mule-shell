package net.sf.zoftwhere.mule.security;

import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;

public class JWTSigner {

	private final Algorithm algorithm;

	public JWTSigner(Algorithm algorithm) {
		this.algorithm = algorithm;
	}

	public String sign(JWTCreator.Builder builder) {
		return builder.sign(algorithm);
	}
}
