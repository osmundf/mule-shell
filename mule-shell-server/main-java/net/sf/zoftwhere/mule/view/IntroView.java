package net.sf.zoftwhere.mule.view;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import lombok.Getter;
import net.sf.zoftwhere.dropwizard.ContextPath;
import net.sf.zoftwhere.dropwizard.MuleInfo;
import net.sf.zoftwhere.dropwizard.ViewAssetPath;

public class IntroView extends BootStrapView {

	private static final String templateName = getTemplateName(IntroView.class, FTL_SUFFIX).toLowerCase();

	@Getter
	private final String version;

	public IntroView(ContextPath contextPath, MuleInfo muleInfo, ViewAssetPath viewAssetPath) {
		super(templateName, StandardCharsets.UTF_8, contextPath, viewAssetPath);
		this.version = muleInfo.get("version").orElse("<version>");

		final var me = (Supplier<String>) () -> "Hello World %s!%n";
		System.out.printf("%n").println();
	}
}
