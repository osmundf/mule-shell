package net.sf.zoftwhere.mule.view;

import io.dropwizard.views.View;
import lombok.Getter;
import net.sf.zoftwhere.dropwizard.ContextPath;

import javax.annotation.Nonnull;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Optional;

@SuppressWarnings({"unused", "WeakerAccess", "RedundantSuppression"})
public class AbstractView extends View {

	public static final String FTL_SUFFIX = "ftl";
	public static final String FTLH_SUFFIX = "ftlh";
	public static final String FTLX_SUFFIX = "ftlx";
	public static final String MUSTACHE_SUFFIX = "mustache";

	@Getter
	private final String contextPath;

	private AbstractView(String name, ContextPath contextPath) {
		this(name, null, contextPath);
	}

	protected AbstractView(String name, Charset charset, ContextPath contextPath) {
		super(name, charset);
		this.contextPath = contextPath.get();
	}

	/**
	 * Helper for package deployed views.
	 *
	 * @param viewClass The view class.
	 * @param suffix    The file extension (eg. "ftl").
	 */
	protected static <V extends View> String getTemplateName(Class<V> viewClass, String suffix) {
		final var regex = "(.*)View";
		final var replacement = "$1";
		return viewClass.getSimpleName().replaceFirst(regex, replacement) + '.' + suffix;
	}

	protected static <V extends View> String getTemplateName(Class<V> viewClass, String suffix, String regex, String replacement) {
		return viewClass.getSimpleName().replaceFirst(regex, replacement) + '.' + suffix;
	}

	public static class HtmlTag {
		public final String composed;

		public HtmlTag(String name, LinkedHashMap<String, Optional<String>> attributeMap, String content) {
			composed = compose(name, attributeMap, true, content);
		}

		public HtmlTag(String name, LinkedHashMap<String, Optional<String>> attributeMap, boolean hasEndTag) {
			composed = compose(name, attributeMap, hasEndTag, null);
		}

		@Override
		public String toString() {
			return composed;
		}

		private String compose(String name, LinkedHashMap<String, Optional<String>> attributeMap, boolean hasEndTag, String content) {
			final var basicString = basicString(name, attributeMap);

			if (!hasEndTag) {
				return basicString.append(">").toString();
			} else if (content == null) {
				return basicString.append("/>").toString();
			}

			return basicString.append(">").append(content).append("</").append(name).append(">").toString();
		}

		private StringBuilder basicString(String name, LinkedHashMap<String, Optional<String>> attributeMap) {
			final var builder = new StringBuilder();
			builder.append("<").append(name);
			attributeMap.forEach((k, v) -> {
				builder.append(" ").append(k);
				v.ifPresent(s -> builder.append("=\"").append(s).append("\""));
			});
			return builder;
		}
	}

	public static class HtmlTagBuilder {
		private final String name;
		private final LinkedHashMap<String, Optional<String>> attributeMap;

		/**
		 * User HtmlTagBuilder.name(String name) for new instance.
		 *
		 * @param name tag name.
		 */
		private HtmlTagBuilder(String name) {
			this.name = name;
			this.attributeMap = new LinkedHashMap<>();
		}

		public HtmlTagBuilder with(String attribute) {
			attributeMap.put(attribute, Optional.empty());
			return this;
		}

		public HtmlTagBuilder with(String attribute, String value) {
			attributeMap.put(attribute, Optional.of(value));
			return this;
		}

		public HtmlTag build(boolean withEndTag) {
			return new HtmlTag(name, attributeMap, withEndTag);
		}

		public HtmlTag build(String content) {
			return new HtmlTag(name, attributeMap, content);
		}

		public static HtmlTagBuilder name(@Nonnull String name) {
			return new HtmlTagBuilder(name);
		}
	}
}
