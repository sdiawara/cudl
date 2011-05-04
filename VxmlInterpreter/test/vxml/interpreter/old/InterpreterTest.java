package vxml.interpreter.old;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import vxml.interpreter.InterpreterContext;
import vxml.interpreter.execption.InterpreterException;
import vxml.utils.Prompt;

public class InterpreterTest extends TestCase {

	private InterpreterContext interpreterContext;

	@Test
	public void testCallServiceNavigateUntilNextInteractionAndCollectsNavigationTraces()
			throws IOException, SAXException, XPathExpressionException,
			TransformerException {
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
		expectedPrompt.tts = "Pour avoir des informations détaillées sur ce tarif, dites : « tarif ». ";
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
		expectedPrompt.tts = "Pour avoir des informations détaillées sur ce tarif, dites : « tarif ». ";
		// expectedPrompt.interruptionGrammar = "/data/modeles/TARIF.srg";
		expectedPrompts.add(expectedPrompt);

		interpreterContext = new InterpreterContext(
				"oldtestfile/VxmlGlobalServletService");
		interpreterContext.launchInterpreter();

		// System.out.println("logs "
		// + interpreterContext.interpreter.getTraceLog());
		// System.out.println("stats "
		// + interpreterContext.interpreter.getTraceStat());
		// System.out.println(interpreterContext.interpreter.getPrompts());
		// System.out.println(expectedPrompts);

		assertEquals(expectedLogs, interpreterContext.interpreter.getTraceLog());
		assertEquals(expectedStats, interpreterContext.interpreter
				.getTraceStat());
		assertEquals(expectedPrompts, interpreterContext.interpreter
				.getPrompts());
	}

	@Test
	public void testNoInputAndNoMatchNavigatesUntilNextInteraction()
			throws InterpreterException, SAXException, IOException {
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
		interpreterContext.noInput();
		interpreterContext.noMatch();
		
		assertEquals(expectedLogs, interpreterContext.interpreter.getTraceLog());
	}

	@Test

	public void testTalkNavigatesUntilNextInteraction()
			throws XPathExpressionException, IOException, SAXException,
			TransformerException {
		List<String> expectedLogs = new ArrayList<String>();
		expectedLogs.add("LOG PHASE init");
		expectedLogs.add("new call");
		expectedLogs.add("LOG PHASE relai");
		expectedLogs.add("info:+@_relai.enter");
		expectedLogs.add("LOG PHASE interaction");
		expectedLogs.add("LOG PHASE talk");
		expectedLogs.add("LOG PHASE interaction");

		interpreterContext = new InterpreterContext(
				"oldtestfile/VxmlGlobalServletService");
		interpreterContext.launchInterpreter();
		//interpreterContext.talk();
		
		assertEquals(expectedLogs, interpreterContext.interpreter.getTraceLog());

	}

	// @Test
	// public void testDisconnectCallsTheCorrespondingEventManager()
	// throws XPathExpressionException, IOException, SAXException,
	// TransformerException {
	// List<String> expectedLogs = new ArrayList<String>();
	// expectedLogs.add("LOG PHASE init");
	// expectedLogs.add("LOG PHASE relai");
	// expectedLogs.add("info:+@_relai.enter");
	// expectedLogs.add("LOG PHASE interaction");
	// expectedLogs.add("LOG PHASE raccrocher");
	//
	// vxml3900Browser.call3900();
	// vxml3900Browser.disconnect();
	//
	// assertFalse(vxml3900Browser.navigationTracesLogs.isEmpty());
	// assertEquals(expectedLogs, vxml3900Browser.navigationTracesLogs);
	// }
	//
	// @Test
	// public void
	// testNavigatorConstructorClearsVxmlCollectedPagesDirectoryWhenOptionActivated()
	// throws XPathExpressionException, IOException, SAXException {
	//
	// DefinitionDeLEnvironnement.logPagesDirectory = "/tmp/test_log_pages/";
	//
	// File testDirectory = new File(
	// DefinitionDeLEnvironnement.logPagesDirectory + "TEST");
	// testDirectory.delete();
	// assertTrue(testDirectory.mkdirs());
	//
	// new Vxml3900Browser();
	//
	// assertFalse(testDirectory.exists());
	// }
	//
	// @Test
	// public void
	// testNavigatorConstructorCreatesVxmlCollectedPagesDirectoryWhenOptionActivated()
	// throws XPathExpressionException, IOException, SAXException {
	//
	// DefinitionDeLEnvironnement.logPagesDirectory = "/tmp/test_log_pages/";
	//
	// File logDirectory = new File(
	// DefinitionDeLEnvironnement.logPagesDirectory);
	// deltree(logDirectory);
	// assertFalse(logDirectory.exists());
	//
	// new Vxml3900Browser();
	//
	// assertTrue(logDirectory.exists());
	// }
	//
	// private void deltree(File file) {
	// if (file.isDirectory()) {
	// File[] listFiles = file.listFiles();
	// for (int i = 0; i < listFiles.length; i++) {
	// deltree(listFiles[i]);
	// }
	// }
	// file.delete();
	// }
	//
	// @Test
	// public void testNavigatorCollectsVxmlWhenOptionActivated()
	// throws XPathExpressionException, IOException, SAXException,
	// TransformerException {
	//
	// DefinitionDeLEnvironnement.logPagesDirectory = "/tmp/test_log_pages/";
	//
	// vxml3900Browser.nd = "0123456789";
	// vxml3900Browser.numeroAppele = "0892683613";
	// vxml3900Browser.origine = "33";
	// vxml3900Browser.channel = "10";
	// vxml3900Browser.call3900();
	//
	// assertTrue(new File(
	// DefinitionDeLEnvironnement.logPagesDirectory
	// +
	// "pagesInteractionLoop_VxmlGlobalServletService?currentState=InitDial&toState=InitDial&callReason=normal&callingNumber=tel:%200123456789&calledNumber=0892683613&origine=33&channel=10--0")
	// .exists());
	// assertTrue(new File(
	// DefinitionDeLEnvironnement.logPagesDirectory
	// +
	// "pagesInteractionLoop_PhaseRelai?currentState=InitDial&toState=&callReason=normal&--1")
	// .exists());
	// assertTrue(new File(
	// DefinitionDeLEnvironnement.logPagesDirectory
	// +
	// "pagesInteractionLoop_PhaseInteraction?currentState=InitDial&toState=&callReason=normal&--2")
	// .exists());
	// }
	//
	// @Test
	// public void testNavigationCollectsTransfertInformationButDoNotTransfer()
	// throws XPathExpressionException, IOException, SAXException,
	// TransformerException {
	// DefinitionDeLEnvironnement.urlServeurVoicexml =
	// buildUrlServeurVoiceXml("test/fr/olm/app3900plus/vxmlbrowsing/pagesTransfert/");
	// List<String> expectedLogs = new ArrayList<String>();
	// expectedLogs.add("LOG PHASE init");
	// expectedLogs.add("LOG PHASE transfert");
	//
	// vxml3900Browser.call3900();
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
	// public void testTransferOKSimulatesTransferOnTransferPage()
	// throws XPathExpressionException, IOException, SAXException,
	// TransformerException {
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
	//
	// @Test
	// public void testTransferKOSimulatesBadTransferOnTransferPage()
	// throws XPathExpressionException, IOException, SAXException,
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
