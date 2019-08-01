package net.sf.zoftwhere.mule.view;

import java.nio.charset.StandardCharsets;

public class LogInView extends AbstractView {

	public static final String templateName = getTemplateName(LogInView.class, FTL_SUFFIX);

	public LogInView() {
		super(templateName, StandardCharsets.UTF_8);
	}
}
