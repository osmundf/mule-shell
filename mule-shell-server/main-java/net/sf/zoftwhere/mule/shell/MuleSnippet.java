package net.sf.zoftwhere.mule.shell;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jdk.jshell.ErroneousSnippet;
import jdk.jshell.ExpressionSnippet;
import jdk.jshell.ImportSnippet;
import jdk.jshell.MethodSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.StatementSnippet;
import jdk.jshell.TypeDeclSnippet;
import jdk.jshell.VarSnippet;
import net.sf.zoftwhere.mule.model.ImportSnippetModel;
import net.sf.zoftwhere.mule.model.MethodSnippetModel;
import net.sf.zoftwhere.mule.model.SnippetModel;
import net.sf.zoftwhere.mule.model.SnippetStatusModel;
import net.sf.zoftwhere.mule.model.TypeSnippetModel;
import net.sf.zoftwhere.mule.model.VariableSnippetModel;

import static net.sf.zoftwhere.mule.model.SnippetTypeModel.ERROR;
import static net.sf.zoftwhere.mule.model.SnippetTypeModel.EXPRESSION;
import static net.sf.zoftwhere.mule.model.SnippetTypeModel.IMPORT;
import static net.sf.zoftwhere.mule.model.SnippetTypeModel.METHOD;
import static net.sf.zoftwhere.mule.model.SnippetTypeModel.OTHER;
import static net.sf.zoftwhere.mule.model.SnippetTypeModel.STATEMENT;
import static net.sf.zoftwhere.mule.model.SnippetTypeModel.TYPE;
import static net.sf.zoftwhere.mule.model.SnippetTypeModel.VARIABLE;

public abstract class MuleSnippet {

	public static <S, M> List<M> getModelList(MuleShell shell, Function<MuleShell, Stream<S>> getList,
		BiFunction<MuleShell, S, M> toModel)
	{
		return getList.apply(shell).map(s -> toModel.apply(shell, s)).collect(Collectors.toList());
	}

	public static SnippetStatusModel status(Snippet.Status status) {
		return SnippetStatusModel.fromValue(status.name().toLowerCase());
	}

	public static SnippetModel generalSnippet(final MuleShell shell, final Snippet snippet, final String value) {
		return generalSnippet(snippet).status(status(shell.status(snippet))).value(value);
	}

	public static SnippetModel generalSnippet(final MuleShell shell, final Snippet snippet) {
		return generalSnippet(snippet).status(status(shell.status(snippet)));
	}

	public static SnippetModel generalSnippet(final Snippet snippet) {
		final var result = new SnippetModel();
		result.setId(snippet.id());
		result.setSource(snippet.source());

		if (snippet instanceof ErroneousSnippet) {
			result.setType(ERROR);
			result.setName(snippet.getClass().getSimpleName());
			result.setTypeName(snippet.getClass().getTypeName());
			result.setFullName(snippet.getClass().getName());
			result.error("snippet.error");
		}
		else if (snippet instanceof ExpressionSnippet) {
			result.setType(EXPRESSION);
			final var var = (ExpressionSnippet) snippet;
			result.setTypeName(var.typeName());
			result.setName(var.name());
		}
		else if (snippet instanceof ImportSnippet) {
			result.setType(IMPORT);
			final var var = (ImportSnippet) snippet;
			result.setName(var.name());
		}
		else if (snippet instanceof MethodSnippet) {
			result.setType(METHOD);
			final var var = (MethodSnippet) snippet;
			result.setName(var.name());
			result.setFullName(var.name() + "(" + var.parameterTypes() + ")");
			result.setSignature(var.signature());
		}
		else if (snippet instanceof StatementSnippet) {
			result.setType(STATEMENT);
		}
		else if (snippet instanceof TypeDeclSnippet) {
			result.type(TYPE);
			final var var = (TypeDeclSnippet) snippet;
			result.setName(var.name());
			if (var.subKind() == Snippet.SubKind.ANNOTATION_TYPE_SUBKIND) {
				result.setTypeName("annotation");
			}
			else if (var.subKind() == Snippet.SubKind.CLASS_SUBKIND) {
				result.setTypeName("class");
			}
			else if (var.subKind() == Snippet.SubKind.ENUM_SUBKIND) {
				result.setTypeName("enum");
			}
			else if (var.subKind() == Snippet.SubKind.INTERFACE_SUBKIND) {
				result.setTypeName("interface");
			}
		}
		else if (snippet instanceof VarSnippet) {
			result.setType(VARIABLE);
			final var var = (VarSnippet) snippet;
			result.setTypeName(var.typeName());
			result.setName(var.name());
		}
		else {
			result.setType(OTHER);
			result.setTypeName(snippet.getClass().getSimpleName());
			result.setFullName(snippet.getClass().getName());
			result.setError("error.unhandled.type");
		}

		result.setSource(snippet.source());
		return result;
	}

	public static ImportSnippetModel importSnippet(final ImportSnippet snippet) {
		final var result = new ImportSnippetModel();
		result.setId(snippet.id());
		result.setName(snippet.name());
		result.setSource(snippet.source());
		return result;
	}

	public static VariableSnippetModel variableSnippet(final VarSnippet snippet, final String value) {
		final var result = new VariableSnippetModel();
		result.setId(snippet.id());
		result.setName(snippet.name());
		result.setTypeName(snippet.typeName());
		result.setValue(value);
		result.setSource(snippet.source());
		return result;
	}

	public static MethodSnippetModel methodSnippet(final MethodSnippet snippet) {
		final var result = new MethodSnippetModel();
		result.setId(snippet.id());
		result.setName(snippet.name());
		result.setSignature(snippet.signature());
		result.setSource(snippet.source());
		return result;
	}

	public static TypeSnippetModel typeSnippet(final TypeDeclSnippet snippet) {
		final var result = new TypeSnippetModel();
		result.setId(snippet.id());
		result.setName(snippet.name());
		result.setSource(snippet.source());
		return result;
	}
}
