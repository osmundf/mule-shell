package net.sf.zoftwhere.mule.security;

import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;

public class JWTSigner {

	private final String issuer;

	private final Algorithm algorithm;

	public JWTSigner(String issuer, Algorithm algorithm) {
		this.issuer = issuer;
		this.algorithm = algorithm;
	}

	public String sign(JWTCreator.Builder builder) {
		return builder.withIssuer(issuer).sign(algorithm);
	}
}
