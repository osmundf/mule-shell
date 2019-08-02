package net.sf.zoftwhere.mule.view;

import net.sf.zoftwhere.dropwizard.ContextPath;

import java.nio.charset.StandardCharsets;

public class IndexView extends AbstractView {

	private static final String templateName = getTemplateName(IndexView.class, FTL_SUFFIX);

	public IndexView(ContextPath contextPath) {
		super(templateName, StandardCharsets.UTF_8, contextPath);
	}
}
