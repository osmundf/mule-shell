package net.sf.zoftwhere.mule.view;

import lombok.Getter;
import net.sf.zoftwhere.dropwizard.ContextPath;
import net.sf.zoftwhere.mule.model.RoleModel;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class ConsoleView extends AbstractView {

	private static final String templateName = getTemplateName(ConsoleView.class, FTL_SUFFIX).toLowerCase();

	@Getter
	private final RoleModel roleModel;

	@Getter
	private final String time = Instant.now().toString();

	public ConsoleView(RoleModel roleModel, ContextPath contextPath) {
		super(templateName, StandardCharsets.UTF_8, contextPath);
		this.roleModel = roleModel;
	}
}
