package net.sf.zoftwhere.mule.security;

import lombok.Getter;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class AccountSigner {

	private final MessageDigest algorithm;

	@Getter
	private final int minimumPasswordLength;

	public AccountSigner(final MessageDigest algorithm, final int minimumPasswordLength) {
		this.algorithm = algorithm;
		this.minimumPasswordLength = minimumPasswordLength;
	}

	public byte[] generateSalt(final int size) {
		final byte[] salt = new byte[size];
		new SecureRandom().nextBytes(salt);
		return salt;
	}

	public byte[] getHash(final byte[] salt, final byte[] data) {
		algorithm.update(salt);
		return algorithm.digest(data);
	}

	public boolean validate(final byte[] data, final byte[] salt, final byte[] hash) {
		algorithm.update(salt);
		byte[] candidate = algorithm.digest(data);
		return arraysEqual(candidate, hash);
	}

	private boolean arraysEqual(byte[] a1, byte[] a2) {
		if (a1 == null || a2 == null || a1.length != a2.length) {
			return false;
		}

		for (int i = 0, size = a1.length; i < size; i++) {
			if (a1[i] != a2[i]) {
				return false;
			}
		}

		return true;
	}
}
