package net.sf.zoftwhere.mule.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractLocator;
import org.hibernate.Session;

import java.util.UUID;

public class ShellSessionLocator extends AbstractLocator<ShellSession, UUID> {

	@Inject
	public ShellSessionLocator(Provider<Session> sessionProvider) {
		super(sessionProvider);
	}
}
