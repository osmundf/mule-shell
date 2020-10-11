package net.sf.zoftwhere.mule.shell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import jdk.jshell.Diag;
import jdk.jshell.ImportSnippet;
import jdk.jshell.JShell;
import jdk.jshell.MethodSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis.CompletionInfo;
import jdk.jshell.TypeDeclSnippet;
import jdk.jshell.VarSnippet;
import lombok.Getter;
import net.sf.zoftwhere.mule.model.DiagnosticModel;
import net.sf.zoftwhere.mule.model.SnippetModel;
import net.sf.zoftwhere.mule.model.SnippetTypeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jdk.jshell.SourceCodeAnalysis.Completeness.COMPLETE_WITH_SEMI;
import static jdk.jshell.SourceCodeAnalysis.Completeness.DEFINITELY_INCOMPLETE;

public class MuleShell implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(MuleShell.class);

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

	@Getter
	private final UUID id;
	@Getter
	private boolean closed = false;
	@Getter
	private final PrintStream writeInput;
	@Getter
	private final InputStreamReader readOutput = null;
	private final ByteArrayOutputStream outputPrinterStream;
	@Getter
	private final JShell jShell;

	public MuleShell() {
		this(UUID.randomUUID(), builder -> builder);
	}

	public MuleShell(UUID id, Function<JShell.Builder, JShell.Builder> setting) {
		this.id = id;
		final var builder = setting.apply(JShell.builder());

		try {
			final var inputReadPipe = new PipedInputStream();
			final var inputWritePipe = new PipedOutputStream(inputReadPipe);
			this.writeInput = new PrintStream(inputWritePipe);

			final var outputWriteStream = new ByteArrayOutputStream();
			outputPrinterStream = outputWriteStream;

			final var printer = new PrintStream(outputWriteStream);

			builder.in(inputReadPipe);
			builder.out(printer);
			builder.err(printer);
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		this.jShell = builder.build();
		Consumer<JShell> closeConsumer = (JShell jShell) -> {
			closeMule();
			logger.debug("JShell for MuleShell {} closed.", id);
		};
		this.jShell.onShutdown(closeConsumer);
	}

	@Override
	public void close() throws Exception {
		jShell.close();
	}

	private void closeMule() {
		this.closed = true;
		try {
			readOutput.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		writeInput.close();
	}

	@SuppressWarnings("WeakerAccess")
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

			if (info.completeness() == DEFINITELY_INCOMPLETE || info.completeness() == COMPLETE_WITH_SEMI) {
				final var evaluationModelList = Lists.newCopyOnWriteArrayList(modelList);
				return new MuleShellEvaluation(evaluationModelList, remainingCode, null);
			}

			for (Snippet sourceSnippet : analyzer.sourceToSnippets(info.source())) {
				modelList.add(MuleSnippet.generalSnippet(sourceSnippet));
			}

			remainingCode = info.remaining();
		}

		final var evaluationModelList = Lists.newCopyOnWriteArrayList(modelList);
		return new MuleShellEvaluation(evaluationModelList, remainingCode, null);
	}

	/**
	 * Source Code evaluation. Special thanks to: https://github.com/CSchoel/arbitrary-but-fixed/blob/master/_posts/2018-10-18-jshell-exceptions.md
	 * https://arbitrary-but-fixed.net/teaching/java/jshell/2018/10/18/jshell-exceptions.html
	 *
	 * @param inputCode source code
	 * @return MuleShellEvaluation
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

			if (info.completeness() == DEFINITELY_INCOMPLETE || info.completeness() == COMPLETE_WITH_SEMI) {
				final var evaluationModelList = Lists.newCopyOnWriteArrayList(modelList);
				return new MuleShellEvaluation(evaluationModelList, remainingCode, null);
			}

			remainingCode = info.remaining().trim();

			final var code = info.source();
			final List<SnippetEvent> eventList;
			try {
				eventList = jShell.eval(code);
			}
			catch (RuntimeException e) {
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
		}
		while (!Strings.isNullOrEmpty(remainingCode) && errorCode == null);

		readConsoleBuffer().ifPresent(s -> {
			modelList.add(new SnippetModel().type(SnippetTypeModel.CONSOLE).value(s));
		});

		final var evaluationModelList = Lists.newCopyOnWriteArrayList(modelList);
		return new MuleShellEvaluation(evaluationModelList, remainingCode, errorCode, null, null);
	}

	private Optional<String> readConsoleBuffer() {
		if (isClosed()) {
			return Optional.empty();
		}

		if (outputPrinterStream.size() == 0) {
			return Optional.empty();
		}

		final var output = new String(outputPrinterStream.toByteArray());
		outputPrinterStream.reset();
		return Optional.of(output);
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
}
