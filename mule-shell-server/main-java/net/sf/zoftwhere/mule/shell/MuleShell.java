package net.sf.zoftwhere.mule.shell;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import jdk.jshell.Diag;
import jdk.jshell.ImportSnippet;
import jdk.jshell.JShell;
import jdk.jshell.MethodSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis.Completeness;
import jdk.jshell.SourceCodeAnalysis.CompletionInfo;
import jdk.jshell.TypeDeclSnippet;
import jdk.jshell.VarSnippet;
import lombok.Getter;
import net.sf.zoftwhere.mule.model.DiagnosticModel;
import net.sf.zoftwhere.mule.model.SnippetModel;
import net.sf.zoftwhere.mule.model.SnippetStatusModel;
import net.sf.zoftwhere.mule.model.SnippetTypeModel;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static jdk.jshell.SourceCodeAnalysis.Completeness.COMPLETE;
import static jdk.jshell.SourceCodeAnalysis.Completeness.COMPLETE_WITH_SEMI;

public class MuleShell implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(MuleShell.class);

	protected final static Function<@NonNull Completeness, @NonNull Boolean> JSHELL_COMPLETENESS =
			completeness -> completeness == COMPLETE || completeness == COMPLETE_WITH_SEMI;

	protected final static Function<@NonNull Completeness, @NonNull Boolean> MULE_SHELL_COMPLETENESS =
			completeness -> completeness == COMPLETE;

	@Getter
	private boolean closed = false;

	@Getter
	private final PrintStream writeInput;

	@Getter
	private final InputStreamReader readOutput;

	@Getter
	private final JShell jShell;

	private final Consumer<JShell> consumer = (JShell jShell) -> closeMule();

	public MuleShell() {
		this(builder -> builder);
	}

	public MuleShell(Function<JShell.Builder, JShell.Builder> setting) {
		final var builder = setting.apply(JShell.builder());
		OutputStream writeInput = new NullOutputStream();
		InputStream readOutput = new NullInputStream(1024);

		try {
			final var inputPipe = new PipedInputStream();
			final var outputPipe = new PipedOutputStream(inputPipe);
			builder.in(inputPipe);
			writeInput = outputPipe;
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			final var inputPipe = new PipedInputStream();
			final var outputPipe = new PipedOutputStream(inputPipe);
			builder.in(inputPipe);
			writeInput = outputPipe;
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			final var inputPipe = new PipedInputStream();
			final var outputPipe = new PipedOutputStream(inputPipe);
			readOutput = inputPipe;
			final var printer = new PrintStream(outputPipe);
			builder.out(printer);
			builder.err(printer);
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.writeInput = new PrintStream(writeInput, false, StandardCharsets.UTF_8);
		this.readOutput = new InputStreamReader(readOutput, StandardCharsets.UTF_8);
		this.jShell = builder.build();
		this.jShell.onShutdown(consumer);
	}

	@Override
	public void close() throws IOException, Exception {
		jShell.close();
	}

	private void closeMule() {
		this.closed = true;
		try {
			readOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		writeInput.close();
	}

	public MuleShellEvaluation analyzeCode(String inputCode) {
		final var modelList = new ArrayList<SnippetModel>();

		if (closed) {
			// TODO: Update to use user's locale.
			return new MuleShellEvaluation(modelList, inputCode, "error.shell.closed", null, null);
		}

		var remainingCode = inputCode;
		final var analyzer = jShell.sourceCodeAnalysis();
		while (!Strings.isNullOrEmpty(remainingCode)) {
			CompletionInfo info = analyzer.analyzeCompletion(remainingCode);
			var complete = JSHELL_COMPLETENESS.apply(info.completeness());
			if (!complete) {
				final var evaluationModelList = Lists.newCopyOnWriteArrayList(modelList);
				return new MuleShellEvaluation(evaluationModelList, remainingCode, null);
			}

			for (Snippet sourceSnippet : analyzer.sourceToSnippets(info.source())) {
				modelList.add(MuleSnippet.generalSnippet(this, sourceSnippet, null));
			}

			remainingCode = info.remaining();
		}

		final var evaluationModelList = Lists.newCopyOnWriteArrayList(modelList);
		return new MuleShellEvaluation(evaluationModelList, remainingCode, null);
	}

	/**
	 * Special thanks to:
	 * https://github.com/CSchoel/arbitrary-but-fixed/blob/master/_posts/2018-10-18-jshell-exceptions.md
	 * https://arbitrary-but-fixed.net/teaching/java/jshell/2018/10/18/jshell-exceptions.html
	 */
	public MuleShellEvaluation eval(String inputCode) {
		if (closed) {
			throw new RuntimeException("MuleShell is closed.");
		}

		final var modelList = new ArrayList<SnippetModel>();
		final var analyzer = jShell.sourceCodeAnalysis();
		var remainingCode = inputCode;
		String errorCode = null;

		do {
			CompletionInfo info = analyzer.analyzeCompletion(remainingCode);
			var complete = JSHELL_COMPLETENESS.apply(info.completeness());

			if (info.completeness() == Completeness.UNKNOWN) {
				final var diagnostic = new DiagnosticModel();
				diagnostic.setMessage("illegal start of expression");
				diagnostic.setInput(remainingCode);
				diagnostic.setPosition("" + 0);
				diagnostic.setStart("" + 0);
				diagnostic.setEnd("" + inputCode.length());

				final var model = new SnippetModel();
				model.setType(SnippetTypeModel.EXPRESSION);
				model.setStatus(SnippetStatusModel.REJECTED);
				model.setDiagnostic(List.of(diagnostic));

				modelList.add(model);
				final var evaluationModelList = Lists.newCopyOnWriteArrayList(modelList);
				return new MuleShellEvaluation(evaluationModelList, "", "error.code.unknown", null, null);
			}

			if (!complete) {
				final var evaluationModelList = Lists.newCopyOnWriteArrayList(modelList);
				return new MuleShellEvaluation(evaluationModelList, remainingCode, null);
			}

			remainingCode = info.remaining().trim();

			final var code = info.source();
			final List<SnippetEvent> eventList;
			try {
				eventList = jShell.eval(code);
			} catch (RuntimeException e) {
				final var evaluationModelList = Lists.newCopyOnWriteArrayList(modelList);
				return new MuleShellEvaluation(evaluationModelList, remainingCode, "error.code.analysis", null, e);
			}

			for (var event : eventList) {
				final var snippet = event.snippet();
				final var snippetDiagnostic = jShell.diagnostics(snippet).collect(Collectors.toList());

				final var model = MuleSnippet.generalSnippet(this, snippet, event.value());
				final var diagnosticList = new ArrayList<DiagnosticModel>();

				if (event.exception() != null) {
					model.setException(event.exception().getMessage());
					errorCode = "error.snippet.exception";
				}

				if (model.getType() != SnippetTypeModel.STATEMENT) {
					readConsoleBuffer().ifPresent(s -> {
						modelList.add(new SnippetModel().type(SnippetTypeModel.CONSOLE).value(s));
					});
				}

				for (var diag : snippetDiagnostic) {
					diagnosticList.add(getDiagnosticModel(event.snippet(), diag));
				}

				model.setDiagnostic(Lists.newCopyOnWriteArrayList(diagnosticList));
				modelList.add(model);

				if (diagnosticList.size() > 0) {
					errorCode = "code.analysis.diagnostic";
				}
			}

		} while (!Strings.isNullOrEmpty(remainingCode) && errorCode == null);

		readConsoleBuffer().ifPresent(s -> {
			modelList.add(new SnippetModel().type(SnippetTypeModel.CONSOLE).value(s));
		});

		final var evaluationModelList = Lists.newCopyOnWriteArrayList(modelList);
		return new MuleShellEvaluation(evaluationModelList, remainingCode, errorCode, null, null);
	}

	private Optional<String> readConsoleBuffer() {
		StringBuilder builder = new StringBuilder();
		try {
			if (!readOutput.ready()) {
				return Optional.empty();
			}
			while (readOutput.ready()) {
				builder.appendCodePoint(readOutput.read());
			}
			return Optional.of(builder.toString());
		} catch (IOException e) {
			logger.error("Failed to retrieve all console output.", e);
			return Optional.empty();
		}
	}

	public String varValue(VarSnippet varSnippet) {
		return jShell.varValue(varSnippet);
	}

	public Stream<Snippet> snippets() {
		return this.jShell.snippets();
	}

	public Stream<ImportSnippet> imports() {
		return this.jShell.imports();
	}

	public Stream<VarSnippet> variables() {
		return this.jShell.variables();
	}

	public Stream<MethodSnippet> methods() {
		return this.jShell.methods();
	}

	public Stream<TypeDeclSnippet> types() {
		return this.jShell.types();
	}

	public Snippet.Status status(Snippet snippet) {
		return this.jShell.status(snippet);
	}

	public static DiagnosticModel getDiagnosticModel(Snippet snippet, Diag diagnostic) {
		final var model = new DiagnosticModel();
		final var locale = Locale.getDefault();
		model.setInput(snippet.source());
		model.setLocal(locale.getDisplayName());
		model.setMessage(diagnostic.getMessage(locale));
		model.setPosition(Long.toString(diagnostic.getPosition()));
		model.setStart(Long.toString(diagnostic.getStartPosition()));
		model.setEnd(Long.toString(diagnostic.getEndPosition()));
		return model;
	}
}
