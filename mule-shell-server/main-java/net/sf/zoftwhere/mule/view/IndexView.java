package net.sf.zoftwhere.mule.view;

import lombok.Getter;
import net.sf.zoftwhere.dropwizard.ContextPath;
import net.sf.zoftwhere.dropwizard.MuleInfo;
import net.sf.zoftwhere.dropwizard.ViewAssetPath;

import java.nio.charset.StandardCharsets;

public class IndexView extends BootStrapView {

	private static final String templateName = getTemplateName(IndexView.class, FTL_SUFFIX).toLowerCase();

	@Getter
	private final String version;

	public IndexView(ContextPath contextPath, MuleInfo muleInfo, ViewAssetPath viewAssetPath) {
		super(templateName, StandardCharsets.UTF_8, contextPath, viewAssetPath);
		this.version = muleInfo.get("version").orElse("<version>");
	}
}
