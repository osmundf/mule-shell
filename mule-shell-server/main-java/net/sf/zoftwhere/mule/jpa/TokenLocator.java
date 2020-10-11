package net.sf.zoftwhere.mule.jpa;

import java.util.UUID;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractLocator;
import org.hibernate.Session;

public class TokenLocator extends AbstractLocator<Token, UUID> {

	@Inject
	public TokenLocator(Provider<Session> sessionProvider) {
		super(sessionProvider);
	}
}
