package cudl.old;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import cudl.InterpreterContext;
import cudl.utils.InterpreterRequierement;
import cudl.utils.Prompt;

public class InterpreterTest extends TestCase {

	private InterpreterContext interpreterContext;

	@Before
	public void setUp() throws IOException {
		InterpreterRequierement.url = "file://"
				+ new File(".").getCanonicalPath() + "/test/docVxml/";
	}

	@Test
	public void testCallServiceNavigateUntilNextInteractionAndCollectsNavigationTraces()
			throws IOException, ScriptException {
		List<String> expectedLogs = new ArrayList<String>();
		expectedLogs.add("LOG PHASE init");
		expectedLogs.add("new call");
		expectedLogs.add("LOG PHASE relai");
		expectedLogs.add("info:+@_relai.enter");
		expectedLogs.add("LOG PHASE interaction");

		List<String> expectedStats = new ArrayList<String>();
		expectedStats.add("new call");

		List<Prompt> expectedPrompts = new ArrayList<Prompt>();
		Prompt expectedPrompt;

		expectedPrompt = new Prompt();
		expectedPrompt.timeout = "300ms";
		expectedPrompt.bargein = "false";
		expectedPrompt.tts = "Pour avoir des informations détaillées sur ce tarif, dites : « tarif ».";
		expectedPrompts.add(expectedPrompt);

		expectedPrompt = new Prompt();
		expectedPrompt.timeout = "100ms";
		expectedPrompt.bargein = "false";
		expectedPrompt.audio = "file:///usr2/sons/3900sonsappli/AT_REROUTAGE_SAUI.wav ";
		expectedPrompts.add(expectedPrompt);

		expectedPrompt = new Prompt();
		expectedPrompt.timeout = "200ms";
		expectedPrompt.bargein = "true";
		expectedPrompt.bargeinType = "hotword";
		expectedPrompt.audio = "/ROOT/prompts/WAV/ACCUEIL_CHOIX_TARIFS.wav ";
		expectedPrompt.tts = "Pour avoir des informations détaillées sur ce tarif, dites : « tarif ».";
		// expectedPrompt.interruptionGrammar = "/data/modeles/TARIF.srg";
		expectedPrompts.add(expectedPrompt);

		interpreterContext = new InterpreterContext(
				"oldtestfile/VxmlGlobalServletService");
		interpreterContext.launchInterpreter();

		assertEquals(expectedLogs, interpreterContext.interpreter.getTraceLog());
		assertEquals(expectedStats, interpreterContext.interpreter
				.getTracetWithLabel("stats"));
		
		assertEquals(expectedPrompts, interpreterContext.interpreter
				.getPrompts());
		assertFalse(interpreterContext.interpreter.raccrochage());
	}

	@Test
	public void testNoInputAndNoMatchNavigatesUntilNextInteraction()
			throws IOException, ScriptException {
		List<String> expectedLogs = new ArrayList<String>();
		expectedLogs.add("LOG PHASE init");
		expectedLogs.add("new call");
		expectedLogs.add("LOG PHASE relai");
		expectedLogs.add("info:+@_relai.enter");
		expectedLogs.add("LOG PHASE interaction");
		expectedLogs.add("LOG PHASE silence");
		expectedLogs.add("LOG PHASE interaction");
		expectedLogs.add("LOG PHASE incompris");
		expectedLogs.add("LOG PHASE interaction");

		interpreterContext = new InterpreterContext(
				"oldtestfile/VxmlGlobalServletService");
		interpreterContext.launchInterpreter();
		interpreterContext.event("noinput");
		interpreterContext.event("nomatch");

		assertEquals(expectedLogs, interpreterContext.interpreter.getTraceLog());
		assertFalse(interpreterContext.interpreter.raccrochage());
	}

	@Test
	public void testNavigationCollectsTransfertInformationButDoNotTransfer()
			throws IOException, ScriptException {
		InterpreterRequierement.url = "file://"
				+ new File(".").getCanonicalPath()
				+ "/test/docVxml/oldtestfile/transfer";
		List<String> expectedLogs = new ArrayList<String>();
		expectedLogs.add("LOG PHASE init");
		expectedLogs.add("LOG PHASE transfert");

		interpreterContext = new InterpreterContext("VxmlGlobalServletService");
		interpreterContext.launchInterpreter();

		assertEquals("sup:4700810C810106830783105506911808",
				interpreterContext.interpreter.transfertDestination);
		assertFalse(interpreterContext.interpreter.raccrochage());
		// si
		// transfert
		// alors il
		// n'y
		// a pas raccrochage
		assertFalse(interpreterContext.interpreter.getTraceLog().isEmpty());
		assertEquals(expectedLogs, interpreterContext.interpreter.getTraceLog());
		assertFalse(interpreterContext.interpreter.raccrochage());
	}

	@Test
	public void testTransferOKSimulatesTransferOnTransferPage()
			throws IOException, ScriptException {
		InterpreterRequierement.url = "file://"
				+ new File(".").getCanonicalPath()
				+ "/test/docVxml/oldtestfile/transfer";
		List<String> expectedLogs = new ArrayList<String>();
		expectedLogs.add("LOG PHASE init");
		expectedLogs.add("LOG PHASE transfert");
		expectedLogs.add("LOG PHASE fin transfert");

		interpreterContext = new InterpreterContext("VxmlGlobalServletService");
		interpreterContext.launchInterpreter();
		interpreterContext.callerHangup(0);

		assertEquals("sup:4700810C810106830783105506911808",
				interpreterContext.interpreter.transfertDestination);
		// si
		// transfert
		// alors il
		// n'y
		// a pas raccrochage
		assertFalse(interpreterContext.interpreter.getTraceLog().isEmpty());
		assertEquals(expectedLogs, interpreterContext.interpreter.getTraceLog());
		System.err.println(interpreterContext.interpreter.raccrochage()
				+ "   ---->");
		assertTrue(interpreterContext.interpreter.raccrochage());
	}

	@Test
	public void testTransferKOSimulatesBadTransferOnTransferPage()
			throws IOException, ScriptException {
		InterpreterRequierement.url = "file://"
				+ new File(".").getCanonicalPath()
				+ "/test/docVxml/oldtestfile/transfer";
		List<String> expectedLogs = new ArrayList<String>();
		expectedLogs.add("LOG PHASE init");
		expectedLogs.add("LOG PHASE transfert");
		expectedLogs.add("LOG PHASE fin transfert KO");

		interpreterContext = new InterpreterContext("VxmlGlobalServletService");
		interpreterContext.launchInterpreter();
		interpreterContext.noAnswer();

		assertEquals("sup:4700810C810106830783105506911808",
				interpreterContext.interpreter.transfertDestination);
		assertTrue(interpreterContext.interpreter.raccrochage());
		assertFalse(interpreterContext.interpreter.getTraceLog().isEmpty());
		assertEquals(expectedLogs, interpreterContext.interpreter.getTraceLog());
	}
}
