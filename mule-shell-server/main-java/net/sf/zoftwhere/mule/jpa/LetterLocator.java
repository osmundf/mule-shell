package net.sf.zoftwhere.mule.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.sf.zoftwhere.dropwizard.AbstractLocator;
import org.hibernate.Session;

public class LetterLocator extends AbstractLocator<Letter, Integer> {

	@Inject
	public LetterLocator(Provider<Session> sessionProvider) {
		super(sessionProvider);
	}

	void persistCollection(Letter letter) {
		//currentSession().
	}
}
