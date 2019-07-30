package net.sf.zoftwhere.mule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MuleUniverse {

	private static final Logger logger = LoggerFactory.getLogger(MuleUniverse.class);

	public static void main(String[] args) throws Exception {
		long time = -System.nanoTime();
		new MuleApplication().run("server", "../mule-shell-universe/config.yaml");
		time += System.nanoTime();
		logger.info("Started: " + ((time / 1_000) / 1e3) + " ms");
	}
}
