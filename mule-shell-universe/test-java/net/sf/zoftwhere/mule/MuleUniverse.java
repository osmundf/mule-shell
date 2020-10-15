package net.sf.zoftwhere.mule;

import net.sf.zoftwhere.mule.data.TryParse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.System.getProperty;
import static net.sf.zoftwhere.mule.MuleApplication.SHELL_CACHE_SIZE_PROPERTY;
import static net.sf.zoftwhere.mule.MuleApplication.USER_CACHE_SIZE_PROPERTY;
import static net.sf.zoftwhere.mule.MuleApplicationBuilder.create;

public class MuleUniverse {

	public static void main(String[] args) throws Exception {
		long time = -System.nanoTime();
		create(MuleApplication::new)
			.realm("mule-shell-dev")
			.userCacheSize(TryParse.toInteger(getProperty(USER_CACHE_SIZE_PROPERTY)).orElse(1))
			.shellCacheSize(TryParse.toInteger(getProperty(SHELL_CACHE_SIZE_PROPERTY)).orElse(1))
			.run("server", "config.yaml");
		time += System.nanoTime();
		logger.info("Started: " + ((time / 1_000) / 1e3) + " ms");
	}

	private static final Logger logger = LoggerFactory.getLogger(MuleUniverse.class);
}
