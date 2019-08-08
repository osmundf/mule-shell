package net.sf.zoftwhere.mule.jdk.jshell;

import jdk.jshell.ExpressionSnippet;
import jdk.jshell.JShellException;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import jdk.jshell.VarSnippet;
import lombok.Getter;

public class MuleSnippetEvent {

	@Getter
	private final Snippet snippet;
	@Getter
	private final Status previousStatus;
	@Getter
	private final Status status;
	@Getter
	private final boolean isSignatureChange;
	@Getter
	private final Snippet causeSnippet;
	@Getter
	private final String value;
	@Getter
	private final JShellException exception;

	public MuleSnippetEvent(SnippetEvent event) {
		this( //
				event.snippet(), //
				event.previousStatus(), //
				event.status(), //
				event.isSignatureChange(), //
				event.causeSnippet(), //
				event.value(), //
				event.exception() //
		);
	}

	public MuleSnippetEvent(Snippet snippet, Status previousStatus, Status status, boolean isSignatureChange, Snippet causeSnippet, String value, JShellException exception) {
		this.snippet = snippet;
		this.previousStatus = previousStatus;
		this.status = status;
		this.isSignatureChange = isSignatureChange;
		this.causeSnippet = causeSnippet;
		this.value = value;
		this.exception = exception;
	}

	public String getEventOutput() {
		if (snippet instanceof VarSnippet) {
			if (status == Status.OVERWRITTEN) {
				return String.format("%s (Over Written)", ((VarSnippet) snippet).name());
			}

			return String.format("%s ==> %s", ((VarSnippet) snippet).name(), value);
		}

		if (snippet instanceof ExpressionSnippet) {
			return String.format("%s ==> %s", ((ExpressionSnippet) snippet).name(), value);
		}

		return String.format("(%s) ==> %s", snippet.id(), value);
	}

	@Override
	public String toString() {
		return "MuleSnippetEvent(snippet=" + snippet +
				",previousStatus=" + previousStatus +
				",status=" + status +
				",isSignatureChange=" + isSignatureChange +
				(causeSnippet == null ? "" : ",causeSnippet" + causeSnippet) +
				(value == null ? "" : "value=" + value) +
				(exception == null ? "" : "exception=" + exception) +
				")";
	}
}
