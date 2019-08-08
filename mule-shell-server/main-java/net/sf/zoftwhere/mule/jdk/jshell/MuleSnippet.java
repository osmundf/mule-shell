package net.sf.zoftwhere.mule.jdk.jshell;

import jdk.jshell.ErroneousSnippet;
import jdk.jshell.ExpressionSnippet;
import jdk.jshell.ImportSnippet;
import jdk.jshell.JShell;
import jdk.jshell.MethodSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.VarSnippet;
import net.sf.zoftwhere.mule.model.ImportSnippetModel;
import net.sf.zoftwhere.mule.model.MethodSnippetModel;
import net.sf.zoftwhere.mule.model.SnippetModel;
import net.sf.zoftwhere.mule.model.VariableSnippetModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class MuleSnippet {

	public static <S, I, M> List<M> getModelList(JShell shell, Function<JShell, Stream<S>> getList, Function<Integer, I> indexer, BiFunction<S, I, M> combiner) {
		final var modelList = new ArrayList<M>();
		final var snippetList = getList.apply(shell).collect(Collectors.toList());

		for (int i = 0, size = snippetList.size(); i < size; i++) {
			final var snippet = snippetList.get(i);
			final var index = indexer.apply(i);
			modelList.add(combiner.apply(snippet, index));
		}

		return modelList;
	}

	public static SnippetModel generalSnippet(final Snippet snippet, final String index) {
		final var result = new SnippetModel();
		result.setId(snippet.id());
		result.setIndex(index);

		if (snippet instanceof ImportSnippet) {
			result.setType("Import Snippet");
			final var var = (ImportSnippet) snippet;
			result.setName(var.name());

		} else if (snippet instanceof VarSnippet) {
			result.setType("Variable Snippet");
			final var var = (VarSnippet) snippet;
			result.setName(var.typeName() + " " + var.name());

		} else if (snippet instanceof ExpressionSnippet) {
			result.setType("Expression Snippet");
			final var var = (ExpressionSnippet) snippet;
			result.setName(var.typeName() + " " + var.name());

		} else if (snippet instanceof MethodSnippet) {
			result.setType("Method Snippet");
			final var var = (MethodSnippet) snippet;
			result.setName(var.name());

		} else if (snippet instanceof ErroneousSnippet) {
			result.setType("Erroneous Snippet");
			// final var var = (ErroneousSnippet) snippet;
			result.setName(null);

		} else {
			result.setType("Generic");
			result.setName("");
		}

		result.setSource(snippet.source());
		return result;
	}

	public static ImportSnippetModel importSnippet(final ImportSnippet snippet, final String index) {
		final var result = new ImportSnippetModel();
		result.setId(snippet.id());
		result.setIndex(index);
		result.setName(snippet.name());
		result.setSource(snippet.source());
		return result;
	}

	public static VariableSnippetModel variableSnippet(final VarSnippet snippet, final String index) {
		final var result = new VariableSnippetModel();
		result.setId(snippet.id());
		result.setIndex(index);
		result.setName(snippet.typeName() + " " + snippet.name());
		result.setSource(snippet.source());
		return result;
	}

	public static MethodSnippetModel methodSnippet(final MethodSnippet snippet, final String index) {
		final var result = new MethodSnippetModel();
		result.setId(snippet.id());
		result.setIndex(index);
		result.setName(snippet.name() + " " + snippet.signature());
		result.setSource(snippet.source());
		return result;
	}
}
