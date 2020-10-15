package net.sf.zoftwhere.mule.view;

import java.nio.charset.StandardCharsets;

import lombok.Getter;
import net.sf.zoftwhere.dropwizard.ContextPath;
import net.sf.zoftwhere.dropwizard.MuleInfo;
import net.sf.zoftwhere.dropwizard.ViewAssetPath;
import net.sf.zoftwhere.mule.model.RoleModel;

public class ConsoleView extends BootStrapView {

	private static final String templateName = getTemplateName(ConsoleView.class, FTL_SUFFIX).toLowerCase();

	@Getter
	private final RoleModel roleModel;

	@Getter
	private final String version;

	public ConsoleView(RoleModel roleModel, ContextPath contextPath, MuleInfo muleInfo, ViewAssetPath viewAssetPath) {
		super(templateName, StandardCharsets.UTF_8, contextPath, viewAssetPath);
		this.roleModel = roleModel;
		this.version = muleInfo.get("version").orElse("<Version>");
	}

	@Override
	public String getBootstrapCSS() {
		return super.getBootstrapCSS();
	}

	@Override
	public String getBootstrapJS() {
		return super.getBootstrapJS();
	}

	@Override
	public String getJQueryJS() {
		return super.getJQueryJS();
	}

	@Override
	public String getPopperJS() {
		return super.getPopperJS();
	}
}
