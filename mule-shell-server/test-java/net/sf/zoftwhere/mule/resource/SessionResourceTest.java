package net.sf.zoftwhere.mule.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jdk.jshell.Diag;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.VarSnippet;
import net.sf.zoftwhere.mule.shell.JShellManager;
import net.sf.zoftwhere.mule.shell.UUIDBuffer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

class SessionResourceTest {

	private SessionResource resource = new SessionResource(null, null);

	@Disabled
	@Test
	public void debugExpressions() {
		final var jshell = JShell.create();

		jshell.eval("int a = 10;");
		jshell.eval("" +
				"Integer getNext() {\n" +
				"   return 0;\n" +
				"}");
		jshell.eval("String a = \"10\";");

		Gson gson;
		new GsonBuilder().create();

		jshell.eval("import com.google.gson.GSon;");
		jshell.eval("import com.google.gson.GSonBuilder;");
		jshell.eval("Gson gson = new GsonBuilder().create();");

		jshell.eval("import java.util.Random;");
		jshell.eval("Random r = new Random(0);");

		String call = "" +
				"int[] getPrimeList(final int size) {\n" +
				"   final var result = new int[size];\n" +
				"   result[0] = 1;\n" +
				"   result[1] = 2;\n" +
				"   result[2] = 3;\n" +
				"   int i = 3 - 1;\n" +
				"   int number = 5;\n" +
				"   while (i < size) for (int k = 3; k < number; ++k)\n" +
				"      if (number % k == 0) {\n" +
				"         number += 2;\n" +
				"         k = number;\n" +
				"      } else if (k * k > number) {\n" +
				"         result[i] = number;\n" +
				"         i += 1;\n" +
				"         number += 2;\n" +
				"         k = number;\n" +
				"      }\n" +
				"   return result;\n" +
				"}";

		System.out.println(jshell.eval(call).toString());
		jshell.eval("int[] primeArray = getPrimeList(4_000_000_000L);");

		jshell.sourceCodeAnalysis()
				.sourceToSnippets("int b = 13; int c = 15; String getName() { return \"test.import\" }; ")
				.forEach(line -> System.out.println(line));

//		var d = jshell.varValue(jshell.variables().collect(Collectors.toList()).get(2));
//		System.out.println(d.length());
//		System.out.println(d);

//		entity.setImports(jshell.imports().map(this::importSnippet).collect(Collectors.toList()));
//		entity.setMethods(jshell.methods().map(this::methodSnippet).collect(Collectors.toList()));
//		entity.setVariables(jshell.variables().map(this::variableSnippet).collect(Collectors.toList()));
//		entity.setSnippets(jshell.snippets().map(this::generalSnippet).collect(Collectors.toList()));

		//jshell.snippets().
	}

	/**
	 * Debug code to compare snippet entries with those listing in JShell executed in terminal.
	 */
	@Disabled
	@Test
	public void testCodeAnalysis() {
		JShellManager manager = new JShellManager();
		final var jshell = manager.newJShell(new UUIDBuffer(new Random(0))).orElseThrow().getValue();

		final var inputList = new ArrayList<String>();
//		inputList.add("int a = 10;");
//		inputList.add("int g = 10, h = 20, i = 30;");
//		inputList.add("int size = 100;");
//		inputList.add("int[] huge = new int[size];");
//		inputList.add("String a = \"10\";");
//		inputList.add("/list");
		// The statements must be split by semi-colon?
		inputList.add("int a = 10; String a = \"10\"; double a = 1.1d;");

		for (final String line : inputList) {
			System.out.println(line);
			var split = jshell.sourceCodeAnalysis().sourceToSnippets(line);
			if (split.size() >= 1) {
				for (final Snippet p : split) {
					System.out.println(resource.variableSnippet((VarSnippet) p, ""));
				}
				jshell.eval(line);
			}
			int index = 0;
			jshell.eval(line).forEach(p -> {
				if (p.snippet() instanceof VarSnippet) {
					final var snippet = (VarSnippet) p.snippet();
					final var name = snippet.name();
					final var value = tryGetValue(jshell, snippet).orElse("[error]");
					System.out.println("=> " + name + " = " + value);
					jshell.diagnostics(snippet).forEach(diagLine -> {
						System.out.println(diagLine.toString());
					});

				} else {
					System.out.println(resource.generalSnippet(p.snippet(), Integer.toString(index)));
				}
//				++index;
			});
		}

		System.out.println();

		jshell.snippets().forEach(snippet -> {
			System.out.println(":: " + snippet.source());

			if (snippet instanceof VarSnippet) {
				System.out.println(resource.variableSnippet((VarSnippet) snippet, ""));
			} else {
				System.out.println(resource.generalSnippet(snippet, ""));
				List<Diag> diagStream = jshell.diagnostics(snippet).collect(Collectors.toList());
				diagStream.forEach(System.out::println);

//				var t = jshell.sourceCodeAnalysis().wrapper(snippet);
//				System.out.println(t.toString());
			}
		});
	}

	private Optional<String> tryGetValue(JShell jShell, VarSnippet snippet) {
		try {
			return Optional.of(jShell.varValue(snippet));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}
}