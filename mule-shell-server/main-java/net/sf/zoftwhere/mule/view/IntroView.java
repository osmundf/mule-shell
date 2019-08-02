package net.sf.zoftwhere.mule.view;

import net.sf.zoftwhere.dropwizard.ContextPath;

import java.nio.charset.StandardCharsets;

public class IntroView extends AbstractView {

	private static final String templateName = getTemplateName(IntroView.class, FTL_SUFFIX).toLowerCase();

	public IntroView(ContextPath contextPath) {
		super(templateName, StandardCharsets.UTF_8, contextPath);
	}
}
