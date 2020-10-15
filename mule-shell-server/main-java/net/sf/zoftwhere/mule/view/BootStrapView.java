package net.sf.zoftwhere.mule.view;

import java.nio.charset.Charset;

import lombok.Getter;
import net.sf.zoftwhere.dropwizard.ContextPath;
import net.sf.zoftwhere.dropwizard.ViewAssetPath;

public abstract class BootStrapView extends AbstractView {

	public static final String BOOTSTRAP_CSS_KEY = "bootstrapCSS";
	public static final String BOOTSTRAP_JS_KEY = "bootstrapJS";
	public static final String JQUERY_JS_KEY = "jQueryJS";
	public static final String JQUERY_SLIM_JS_KEY = "jQueryJS";
	public static final String POPPER_JS_KEY = "popperJS";

	public static final String DEFAULT_BOOTSTRAP_CSS;
	public static final String DEFAULT_BOOTSTRAP_JS;
	public static final String DEFAULT_JQUERY_SLIM_JS;
	public static final String DEFAULT_JQUERY_JS;
	public static final String DEFAULT_POPPER_JS;

	static {
		DEFAULT_BOOTSTRAP_CSS = HtmlTagBuilder.name("link")
			.with("rel", "stylesheet")
			.with("href", "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css")
			.build(false)
			.toString();

		DEFAULT_BOOTSTRAP_JS = HtmlTagBuilder.name("script")
			.with("src", "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js")
			.with("integrity", "sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM")
			.with("crossorigin", "anonymous")
			.build("")
			.toString();

		DEFAULT_JQUERY_JS = HtmlTagBuilder.name("script")
			.with("src", "https://code.jquery.com/jquery-3.4.1.min.js")
			.build("")
			.toString();

		DEFAULT_JQUERY_SLIM_JS = HtmlTagBuilder.name("script")
			.with("src", "https://code.jquery.com/jquery-3.3.1.slim.min.js")
			.with("integrity", "sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo")
			.with("crossorigin", "anonymous")
			.build("")
			.toString();

		DEFAULT_POPPER_JS = HtmlTagBuilder.name("script")
			.with("src", "https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js")
			.with("integrity", "sha384-UO2eT0CpHqdSJQ6hJty5KVphtPhzWj9WO1clHTMGa3JDZwrnQq4sF86dIHNDz0W1")
			.with("crossorigin", "anonymous")
			.build("")
			.toString();
	}

	@Getter
	private final String bootstrapCSS;

	@Getter
	private final String bootstrapJS;

	@Getter
	private final String jQueryJS;

	@Getter
	private final String jQuerySlimJS;

	@Getter
	private final String popperJS;

	public BootStrapView(String name, Charset charset, ContextPath contextPath, ViewAssetPath viewAssetPath) {
		super(name, charset, contextPath);
		this.bootstrapCSS = viewAssetPath.get(BOOTSTRAP_CSS_KEY).orElse(DEFAULT_BOOTSTRAP_CSS);
		this.bootstrapJS = viewAssetPath.get(BOOTSTRAP_JS_KEY).orElse(DEFAULT_BOOTSTRAP_JS);
		this.jQueryJS = viewAssetPath.get(JQUERY_JS_KEY).orElse(DEFAULT_JQUERY_JS);
		this.jQuerySlimJS = viewAssetPath.get(JQUERY_SLIM_JS_KEY).orElse(DEFAULT_JQUERY_SLIM_JS);
		this.popperJS = viewAssetPath.get(POPPER_JS_KEY).orElse(DEFAULT_POPPER_JS);
	}
}
