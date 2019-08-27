package net.sf.zoftwhere.mule.resource;

import com.google.inject.Injector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExpressionResourceTest extends TestResource<ExpressionResource> {

	private static final Logger logger = LoggerFactory.getLogger(ExpressionResourceTest.class);

	private final ExpressionResource resource;

	private final Injector guiceInjector;

	ExpressionResourceTest() {
		super(ExpressionResource::new);
		resource = super.getResource();
		guiceInjector = super.getGuiceInjector();
	}

	@BeforeEach
	void prepare() {
	}

	@AfterEach
	void tearDown() {
		// Close the session factory when we are done.
		try {
			super.close();
		} catch (Exception e) {
			logger.warn("There was an exception while closing.", e);
		}
	}

}