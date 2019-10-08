package net.sf.zoftwhere.dropwizard;

import java.util.function.Supplier;

public class ContextPath implements Supplier<String> {

	private final String contextPath;

	public ContextPath(String contextPath) {
		if (contextPath == null || contextPath.equals("/")) {
			this.contextPath = "";
		} else {
			this.contextPath = contextPath;
		}
	}

	@Override
	public String get() {
		return contextPath;
	}
}
