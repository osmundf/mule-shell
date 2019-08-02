package net.sf.zoftwhere.mule.view;

import net.sf.zoftwhere.dropwizard.ContextPath;

import java.nio.charset.StandardCharsets;

public class LogInView extends AbstractView {

	private static final String templateName = getTemplateName(LogInView.class, FTL_SUFFIX).toLowerCase();

	public LogInView(ContextPath contextPath) {
		super(templateName, StandardCharsets.UTF_8, contextPath);
	}
}
