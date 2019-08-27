package net.sf.zoftwhere.mule.shell;

import lombok.Getter;
import net.sf.zoftwhere.mule.model.SnippetModel;
import net.sf.zoftwhere.mule.model.StackTraceElementModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MuleShellEvaluation {

	@Getter
	private final List<SnippetModel> snippetList;

	@Getter
	private final String remainingCode;

	@Getter
	private final String errorCode;

	@Getter
	private final String errorMessage;

	@Getter
	private final Exception exception;

	// @Getter
	private final String exceptionMessage;

	// @Getter
	private final List<StackTraceElementModel> exceptionStackTrace;

	public MuleShellEvaluation(List<SnippetModel> snippetList, String remainingCode, Exception exception) {
		this.snippetList = snippetList;
		this.remainingCode = remainingCode;
		this.errorCode = null;
		this.errorMessage = null;
		this.exception = exception;
		this.exceptionMessage = getMessage(exception);
		this.exceptionStackTrace = getStackTrace(exception);
	}

	public MuleShellEvaluation(List<SnippetModel> snippetList, String remainingCode, String errorCode, Locale locale, Exception exception) {
		this.snippetList = snippetList;
		this.remainingCode = remainingCode;
		this.errorCode = errorCode;
		this.errorMessage = getMessage(errorCode, locale);
		this.exception = exception;
		this.exceptionMessage = getMessage(exception);
		this.exceptionStackTrace = getStackTrace(exception);
	}

	/**
	 * TODO: Implement code list per locale.
	 *
	 * @param code
	 * @param locale
	 * @return
	 */
	protected String getMessage(String code, Locale locale) {
		return code;
	}

	protected String getMessage(Exception exception) {
		return exception != null ? exception.getMessage() : null;
	}

	protected List<StackTraceElementModel> getStackTrace(Exception exception) {
		final var stack = exception != null ? exception.getStackTrace() : new StackTraceElement[0];
		final var size = stack.length;
		final var list = new ArrayList<StackTraceElementModel>(size);

		for (var element : stack) {
			StackTraceElementModel model = new StackTraceElementModel();
			model.setLine(element.toString());
			list.add(model);
		}

		return list;
	}
}
