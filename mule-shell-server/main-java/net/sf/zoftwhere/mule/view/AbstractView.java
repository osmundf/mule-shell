package net.sf.zoftwhere.mule.view;

import io.dropwizard.views.View;
import lombok.Getter;
import net.sf.zoftwhere.dropwizard.ContextPath;

import java.nio.charset.Charset;

@SuppressWarnings({"unused", "WeakerAccess", "RedundantSuppression"})
public abstract class AbstractView extends View {

	public static final String FTL_SUFFIX = "ftl";
	public static final String FTLH_SUFFIX = "ftlh";
	public static final String FTLX_SUFFIX = "ftlx";
	public static final String MUSTACHE_SUFFIX = "mustache";

	@Getter
	private final String contextPath;

	public AbstractView(String name, ContextPath contextPath) {
		this(name, null, contextPath);
	}

	public AbstractView(String name, Charset charset, ContextPath contextPath) {
		super(name, charset);
		this.contextPath = contextPath.get();
	}

	/**
	 * Helper for package deployed views.
	 *
	 * @param viewClass The view class.
	 * @param suffix    The file extension (eg. "ftl").
	 */
	public static <V extends View> String getTemplateName(Class<V> viewClass, String suffix) {
		final var regex = "(.*)View";
		final var replacement = "$1";
		return viewClass.getSimpleName().replaceFirst(regex, replacement) + '.' + suffix;
	}

	public static <V extends View> String getTemplateName(Class<V> viewClass, String suffix, String regex, String replacement) {
		return viewClass.getSimpleName().replaceFirst(regex, replacement) + '.' + suffix;
	}
}
