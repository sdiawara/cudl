package fr.mbs.vxml.interpreter.old;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import fr.mbs.vxml.interpreter.InterpreterContext;
import fr.mbs.vxml.utils.InterpreterRequierement;
import fr.mbs.vxml.utils.Prompt;

public class InterpreterTest extends TestCase {

	private InterpreterContext interpreterContext;

	@Before
	public void setUp() throws IOException {
		InterpreterRequierement.url = "file://"
				+ new File(".").getCanonicalPath() + "/test/docVxml1/";
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
		expectedStats.add("[label:stats] new call");

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
				.getTraceStat());
		assertEquals(expectedPrompts, interpreterContext.interpreter
				.getPrompts());
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
	}

	// @Test
	// public void testNavigationCollectsTransfertInformationButDoNotTransfer()
	// throws IOException, ScriptException {
	// InterpreterRequierement.url = "file://"
	// + new File(".").getCanonicalPath()
	// + "/test/docVxml1/oldtestfile/transfer";
	// List<String> expectedLogs = new ArrayList<String>();
	// expectedLogs.add("LOG PHASE init");
	// expectedLogs.add("LOG PHASE transfert");
	//
	// interpreterContext = new InterpreterContext("VxmlGlobalServletService");
	// interpreterContext.launchInterpreter();
	//		
	//		
	// assertEquals("sup:4700810C810106830783105506911808",
	// interpreterContext.interpreter.transfertDestination);
	// assertFalse(interpreterContext.interpreter.raccrochage()); // si
	// // transfert
	// // alors il
	// // n'y
	// // a pas raccrochage
	// assertFalse(interpreterContext.interpreter.getTraceLog().isEmpty());
	// assertEquals(expectedLogs, interpreterContext.interpreter.getTraceLog());
	// }

	// @Test
	// public void testTransferOKSimulatesTransferOnTransferPage()
	// throws XPathExpressionException, IOException, TransformerException {
	// DefinitionDeLEnvironnement.urlServeurVoicexml =
	// buildUrlServeurVoiceXml("test/fr/olm/app3900plus/vxmlbrowsing/pagesTransfert/");
	// List<String> expectedLogs = new ArrayList<String>();
	// expectedLogs.add("LOG PHASE init");
	// expectedLogs.add("LOG PHASE transfert");
	// expectedLogs.add("LOG PHASE fin transfert");
	//
	// vxml3900Browser.call3900();
	// vxml3900Browser.transferOk();
	//
	// assertEquals("sup:4700810C810106830783105506911808",
	// vxml3900Browser.transfertDestination);
	// assertFalse(vxml3900Browser.raccrochage()); // si transfert alors il n'y
	// // a pas raccrochage
	// assertFalse(vxml3900Browser.navigationTracesLogs.isEmpty());
	// assertEquals(expectedLogs, vxml3900Browser.navigationTracesLogs);
	// }

	// @Test
	// public void testTransferKOSimulatesBadTransferOnTransferPage()
	// throws XPathExpressionException, IOException,
	// TransformerException {
	// DefinitionDeLEnvironnement.urlServeurVoicexml =
	// buildUrlServeurVoiceXml("test/fr/olm/app3900plus/vxmlbrowsing/pagesTransfert/");
	// List<String> expectedLogs = new ArrayList<String>();
	// expectedLogs.add("LOG PHASE init");
	// expectedLogs.add("LOG PHASE transfert");
	// expectedLogs.add("LOG PHASE fin transfert KO");
	//
	// vxml3900Browser.call3900();
	// vxml3900Browser.transferKo();
	//
	// assertEquals("sup:4700810C810106830783105506911808",
	// vxml3900Browser.transfertDestination);
	// assertFalse(vxml3900Browser.raccrochage()); // si transfert alors il n'y
	// // a pas raccrochage
	// assertFalse(vxml3900Browser.navigationTracesLogs.isEmpty());
	// assertEquals(expectedLogs, vxml3900Browser.navigationTracesLogs);
	// }
	//
	// @Test
	// public void testExpandWithoutNamespace() {
	// String input = "//vxml:toto[@titi] | //vxml:TOTO[@titi=\"tetete\"]";
	// String expected =
	// "//vxml:toto[@titi] | //vxml:TOTO[@titi=\"tetete\"]|//toto[@titi] | //TOTO[@titi=\"tetete\"]";
	//
	// assertEquals(expected, vxml3900Browser.expandWithoutNamespace(input));
	// }
}
