package net.sf.zoftwhere.mule.shell;

import com.google.common.base.Strings;
import net.sf.zoftwhere.mule.model.SnippetTypeModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MuleShellTest extends MuleShell implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(MuleShellTest.class);

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private final ByteArrayOutputStream err = new ByteArrayOutputStream();
	private MuleShell shell;

	@BeforeEach
	void setUp() {
		shell = new MuleShell(builder -> builder
				.out(new PrintStream(out))
				.err(new PrintStream(err))
				.compilerOptions("-Xlint:all")
				.remoteVMOptions("-Xms32m", "-Xmx64m"));
	}

	@AfterEach
	void tearDown() {
		try {
			this.close();
		} catch (Exception e) {
			logger.warn("There was an exception while closing.", e);
		}
	}

	@Override
	public void close() throws Exception {
		shell.close();
	}

	@Test
	void testShutdown() {
		final PrintStream oldError = System.err;

		try {
			// Temporary "disable" error output to console.
			System.setErr(new PrintStream(new ByteArrayOutputStream()));

			final var eval = shell.eval("System.exit(0);");
			assertNotNull(eval);
			assertTrue(Strings.isNullOrEmpty(eval.getRemainingCode()));
			assertTrue(shell.isClosed());

		} finally {
			// Restore changes.
			System.setErr(oldError);
		}
	}

	@Test
	void testCompilerWarning() {
		final var eval = shell.eval("" +
				"public int abuseLong() {\n" +
				"  String s = (String) \"redundant\";\n" +
				"  return 0xfffff;\n" +
				"}\n");
		assertNotNull(eval);
		assertTrue(Strings.isNullOrEmpty(eval.getRemainingCode()));
		assertNotNull(eval.getSnippetList());
		assertEquals(1, eval.getSnippetList().size());
	}

	@Test
	void testExpressionSyntaxError() {
		final var eval = shell.eval("int x = unknown * 5.0; int y = x + 1;");
		final var remaining = eval.getRemainingCode();
		final var modelList = eval.getSnippetList();
		assertNull(eval.getException());
	}

	@Test
	void testHugeArray() {
		final var eval = shell.eval("int[][] array = new int[512][1048576];array[256][1048575] = 10;");
		final var remaining = eval.getRemainingCode();
		final var modelList = eval.getSnippetList();
		assertNull(eval.getException());
	}

	@Test
	void testExpressionTypeError() {
		final var eval = shell.eval("long me = 90;\n\n\n    int d = '0'+\"\";");
		final var remaining = eval.getRemainingCode();
		final var modelList = eval.getSnippetList();
		assertNull(eval.getException());
	}

	@Test
	void analyzeCode() {
		final var inputCode = "int x = 10, y = 20, z = 55.5d;";
		final var eval = shell.analyzeCode(inputCode);
		assertEquals(3, eval.getSnippetList().size());

		assertEquals(SnippetTypeModel.VARIABLE, eval.getSnippetList().get(0).getType());
		assertEquals("int", eval.getSnippetList().get(0).getTypeName());
		assertEquals("x", eval.getSnippetList().get(0).getName());

		assertEquals(SnippetTypeModel.VARIABLE, eval.getSnippetList().get(1).getType());
		assertEquals("int", eval.getSnippetList().get(1).getTypeName());
		assertEquals("y", eval.getSnippetList().get(1).getName());

		assertEquals(SnippetTypeModel.VARIABLE, eval.getSnippetList().get(2).getType());
		assertEquals("int", eval.getSnippetList().get(2).getTypeName());
		assertEquals("z", eval.getSnippetList().get(2).getName());

		assertEquals("", eval.getRemainingCode());
		assertNull(eval.getException());
	}
}
