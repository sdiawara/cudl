package cudl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class InterpreterTest {
	private String url;
	private Interpreter interpreter;

	@Before
	public void setUp() throws IOException {
		url = "file://" + new File(".").getCanonicalPath() + "/test/docVxml/";
	}

	@Test
	public void testLogTrace() throws IOException, ParserConfigurationException, SAXException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 1");
		traceLog.add("LOG Hello 2");
		traceLog.add("LOG Hello 3");

		List<String> traceStat = new ArrayList<String>();
		traceStat.add("LOG Hello");

		interpreter = new Interpreter(url + "shello2.vxml");
		interpreter.start();

		assertEquals(traceStat, interpreter.getLogsWithLabel("stats"));
		assertEquals(traceLog, interpreter.getLogs());
	}

	@Test
	public void testLogTraceWithExit() throws IOException, ParserConfigurationException,
			SAXException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 1");

		List<String> traceStat = new ArrayList<String>();
		traceStat.add("LOG Hello");
		interpreter = new Interpreter(url + "shelloExit.vxml");
		interpreter.start();

		System.err.println(traceLog);
		System.err.println(interpreter.getLogs());
		assertEquals(traceLog, interpreter.getLogs());
		assertEquals(traceStat, interpreter.getLogsWithLabel("stats"));
		assertTrue(interpreter.hungup());
	}

	@Test
	public void testLogTraceWhithVariable() throws IOException, ParserConfigurationException,
			SAXException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 2 +356 689999");
		traceLog.add("LOG Hello 2");
		traceLog.add("LOG Hello 3");

		List<String> traceStat = new ArrayList<String>();
		traceStat.add("LOG Hello");
		interpreter = new Interpreter(url + "shelloVar.vxml");
		interpreter.start();

		assertEquals(traceStat, interpreter.getLogsWithLabel("stats"));

		assertEquals(traceLog, interpreter.getLogs());
		assertTrue(interpreter.hungup());
	}

	@Test
	public void testLogTraceWithIfElseifAndElse() throws IOException, ParserConfigurationException,
			SAXException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 1");
		traceLog.add("LOG Hello 2");
		traceLog.add("LOG Hello A");
		traceLog.add("LOG Hello B");
		traceLog.add("LOG Hello 3");
		traceLog.add("LOG Hello 4");
		traceLog.add("LOG Hello 5");

		interpreter = new Interpreter(url + "shelloWith_if_elseif_and_else.vxml");
		interpreter.start();

		assertTrue(interpreter.getLogsWithLabel("stats").isEmpty());

		System.err.println(traceLog);
		System.err.println(interpreter.getLogs());
		assertEquals(traceLog, interpreter.getLogs());
		assertTrue(interpreter.hungup());
	}

	@Test
	public void testLogManyBlock() throws IOException, ParserConfigurationException, SAXException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 1");
		traceLog.add("LOG Hello 2");

		interpreter = new Interpreter(url + "manyBlocks.vxml");
		interpreter.start();

		assertTrue(interpreter.getLogsWithLabel("stats").isEmpty());
		assertEquals(traceLog, interpreter.getLogs());
	}

	@Test
	public void testLogGotoInASameDialog() throws IOException, ParserConfigurationException,
			SAXException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 1");
		traceLog.add("LOG Hello 2");
		traceLog.add("LOG Hello 3");
		traceLog.add("LOG Hello 4");

		interpreter = new Interpreter(url + "goto.vxml");
		interpreter.start();

		assertTrue(interpreter.getLogsWithLabel("stats").isEmpty());
		System.err.println(traceLog);
		System.err.println(interpreter.getLogs());

		assertEquals(traceLog, interpreter.getLogs());
	}

	@Test
	public void testCollectPrompt() throws IOException, ParserConfigurationException, SAXException {
		List<Prompt> prompts = new ArrayList<Prompt>();
		Prompt promptExecepeted;

		promptExecepeted = new Prompt();
		promptExecepeted.timeout = "200ms";
		promptExecepeted.bargein = "true";
		promptExecepeted.audio = "/ROOT/prompts/WAV/ACCUEIL_CHOIX_TARIFS.wav";
		promptExecepeted.tts = "Pour avoir des informations détaillées sur ce tarif, dites : « tarif ».";
		prompts.add(promptExecepeted);

		promptExecepeted = new Prompt();
		promptExecepeted.tts = "dites tarif.";
		prompts.add(promptExecepeted);

		promptExecepeted = new Prompt();
		promptExecepeted.timeout = "300ms";
		promptExecepeted.bargein = "true";
		promptExecepeted.audio = "/ROOT/prompts/WAV/ACCUEIL_CHOIX_TARIFS.wav";
		promptExecepeted.tts = "Pour avoir des informations détaillées sur ce tarif, dites : « tarif ».";
		prompts.add(promptExecepeted);

		promptExecepeted = new Prompt();
		promptExecepeted.tts = "dites tarif.";
		prompts.add(promptExecepeted);

		interpreter = new Interpreter(url + "prompt.vxml");
		interpreter.start();

		System.err.println(prompts);
		System.err.println(interpreter.getPrompts());
		assertEquals(prompts, interpreter.getPrompts());
	}

	@Test
	public void testCombinattionPromptTrace() throws IOException, ParserConfigurationException,
			SAXException {
		List<Prompt> prompts = new ArrayList<Prompt>();
		Prompt promptExecepeted;

		promptExecepeted = new Prompt();
		promptExecepeted.audio = "rtsp://www.piafcauserie.example.com/grive.wav http://www.piafcauserie.example.com/tourterelle.wav";
		promptExecepeted.tts = "Bienvenue chez Graines a gogo. Ce mois-ci nous proposons le barril de 250 kg de graines de chardon a 299.95€ frais d'expédition et de transport compris.";
		prompts.add(promptExecepeted);

		interpreter = new Interpreter(url + "prompt1.vxml");
		interpreter.start();

		System.err.println(prompts);
		System.err.println(interpreter.getPrompts());

		assertTrue(interpreter.getPrompts().size() == 1);
		assertEquals(prompts, interpreter.getPrompts());
	}

	@Test()
	public void testNodeValue() throws IOException, ParserConfigurationException, SAXException {
		List<Prompt> prompts = new ArrayList<Prompt>();
		Prompt promptExecepeted;

		promptExecepeted = new Prompt();
		promptExecepeted.tts = "cette variable est defini partout dans le formulaire";
		prompts.add(promptExecepeted);

		promptExecepeted = new Prompt();
		promptExecepeted.tts = "ha ok: cette variable est defini partout dans le formulaire. c'est compris";
		prompts.add(promptExecepeted);

		interpreter = new Interpreter(url + "root.vxml");
		interpreter.start();

		assertEquals(prompts, interpreter.getPrompts());
	}

	@Test
	public void sideEffectInScript() throws IOException, ParserConfigurationException, SAXException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("bla bla");
		traceLog.add("un exemple de variable de global avec effet de bord");

		interpreter = new Interpreter(url + "sideEffectInScope.vxml");
		interpreter.start();

		assertEquals(traceLog, interpreter.getLogs());
	}

	@Test
	public void anonymeScopeVariable() throws IOException, ParserConfigurationException,
			SAXException {

		interpreter = new Interpreter(url + "anonymeScopeVariable.vxml");
		interpreter.start();

		assertFalse(interpreter.getPrompts().isEmpty());
		assertTrue(interpreter.getPrompts().get(0).tts.equals("pass"));
	}

	@Test
	public void dialogScopeVariable() throws IOException, ParserConfigurationException, SAXException {
		interpreter = new Interpreter(url + "dialogScopeVariable.vxml");
		interpreter.start();

		assertFalse(interpreter.getPrompts().isEmpty());
		assertTrue(interpreter.getPrompts().get(0).tts.equals("pass"));
	}

	@Test
	public void documentScopeVariableIsVisibleInAllDocument() throws IOException,
			URISyntaxException, ParserConfigurationException, SAXException {
		List<Prompt> prompts = new ArrayList<Prompt>();
		Prompt promptExecepeted;

		promptExecepeted = new Prompt();
		promptExecepeted.tts = "variable du document1";
		prompts.add(promptExecepeted);
		promptExecepeted = new Prompt();
		promptExecepeted.tts = "variable du document2";
		prompts.add(promptExecepeted);
		promptExecepeted = new Prompt();
		promptExecepeted.tts = "variable du document3";
		prompts.add(promptExecepeted);

		interpreter = new Interpreter(url + "documentScopeVariable.vxml");
		interpreter.start();

		System.err.println(prompts);
		System.err.println(interpreter.getPrompts());
		assertEquals(prompts, interpreter.getPrompts());
	}

	@Test
	public void RootVariableIsAllWaysVisible() throws IOException, ParserConfigurationException,
			SAXException {
		List<Prompt> prompts = new ArrayList<Prompt>();
		Prompt promptExecepeted;

		promptExecepeted = new Prompt();
		promptExecepeted.tts = "variable application";
		prompts.add(promptExecepeted);
		promptExecepeted = new Prompt();
		promptExecepeted.tts = "variable du document";
		prompts.add(promptExecepeted);

		interpreter = new Interpreter(url + "rootVariable.vxml");
		interpreter.start();

		assertEquals(prompts, interpreter.getPrompts());
	}

	@Test
	public void WhenRootChangeLastRootVariableIsNotLongerAccessible() throws IOException,
			ParserConfigurationException, SAXException {
		interpreter = new Interpreter(url + "rootChangeVariable.vxml");
		interpreter.start();
	}

	@Test
	public void comparerTwoVariableInDifferentScope() throws IOException,
			ParserConfigurationException, SAXException {

		interpreter = new Interpreter(url + "compareTwoVariableDeclaredIndifferentScope");
		interpreter.start();
	}

	@Test
	public void assignVariables() throws IOException, ParserConfigurationException, SAXException {
		interpreter = new Interpreter(url + "assignVariables.vxml");
		interpreter.start();

		assertEquals(1, interpreter.getPrompts().size());
		assertEquals("v", interpreter.getPrompts().get(0).tts);
	}

	@Test
	public void clearVariable() throws IOException, ParserConfigurationException, SAXException {
		List<Prompt> expectedPrompt = new ArrayList<Prompt>();
		Prompt prompt = new Prompt();
		prompt.tts = "pass";
		expectedPrompt.add(prompt);

		Interpreter interpreter = new Interpreter(url + "clear.vxml");
		interpreter.start();

		assertTrue(interpreter.getPrompts().size() == 1);
		assertEquals(expectedPrompt, interpreter.getPrompts());
	}

	@Test
	public void testEventCounterIsOneWhenDialogEntered() throws IOException, SAXException,
			ParserConfigurationException {
		List<Prompt> expectedPrompt = new ArrayList<Prompt>();
		Prompt prompt = new Prompt();
		prompt.tts = "pass";
		expectedPrompt.add(prompt);

		Interpreter interpreter = new Interpreter(url + "EventCounterIsOne.txml");
		interpreter.start();
		interpreter.noInput();
		interpreter.noInput();
		assertTrue(interpreter.getPrompts().size() == 1);
		assertEquals(expectedPrompt, interpreter.getPrompts());
	}

	@Test
	public void testMenuChoiceDtmfAutoGenerateForChoice() throws IOException,
			ParserConfigurationException, SAXException {
		List<Prompt> exceptedPrompts = new ArrayList<Prompt>();
		Prompt prompt = new Prompt();
		prompt.tts = "Pour le français tapez 1, pour l'anglais tapez 2, Pour le chinois tapez 3";
		exceptedPrompts.add(prompt);

		interpreter = new Interpreter(url + "menuBasic.txml");
		interpreter.start();
		interpreter.submitDtmf("3");

		System.err.println(interpreter.getPrompts());
		assertEquals(exceptedPrompts, interpreter.getPrompts());
		assertTrue(interpreter.hungup());
	}

	@Test
	public void testMenuChoiceVoiceAutoGenerateForChoice() throws IOException,
			ParserConfigurationException, SAXException {
		List<Prompt> exceptedPrompts = new ArrayList<Prompt>();
		Prompt prompt = new Prompt();
		prompt.tts = "Pour le français tapez 1, pour l'anglais tapez 2, Pour le chinois tapez 3";
		exceptedPrompts.add(prompt);

		interpreter = new Interpreter(url + "menuBasic.txml");
		interpreter.start();
		interpreter.talk("anglais");

		assertEquals(exceptedPrompts, interpreter.getPrompts());
		assertTrue(interpreter.hungup());
	}

	@Test
	public void choiceInMenuElementCanThrowEventWhenItSelected() throws IOException,
			ParserConfigurationException, SAXException {
		List<Prompt> exceptedPrompts = new ArrayList<Prompt>();
		Prompt prompt = new Prompt();
		prompt.tts = "Pour le français tapez 1, pour l'anglais tapez 2, Pour le chinois tapez 3";
		exceptedPrompts.add(prompt);

		prompt = new Prompt();
		prompt.tts = "vous avez choisi anglais";
		exceptedPrompts.add(prompt);

		interpreter = new Interpreter(url + "choiceThrowEvent.txml");
		interpreter.start();
		interpreter.talk("anglais");

		assertEquals(exceptedPrompts, interpreter.getPrompts());
		assertTrue(interpreter.hungup());
	}

	@Test
	public void whenThrowElementDefineExprEventandEventAnErrorOccur() throws IOException,
			ParserConfigurationException, SAXException {

		List<Prompt> expectedprompts = new ArrayList<Prompt>();
		Prompt prompt = new Prompt();
		prompt.tts = "a badfetch error occur";
		expectedprompts.add(prompt);

		interpreter = new Interpreter(url
				+ "whenThrowElementDefineExprEventandEventAnErrorOccur.vxml");
		interpreter.start();

		assertEquals(expectedprompts, interpreter.getPrompts());
	}

	@Test
	public void whenThenVarNameBeginByScopeNameTheInterpreterThrowAsemanticError()
			throws IOException, ParserConfigurationException, SAXException {

		List<Prompt> expectedprompts = new ArrayList<Prompt>();
		Prompt prompt = new Prompt();
		prompt.tts = "an semantic error occur";
		expectedprompts.add(prompt);

		interpreter = new Interpreter(url
				+ "whenThenVarNameBeginByScopeNameTheInterpreterThrowAsemanticError.vxml");
		interpreter.start();

		assertEquals(expectedprompts, interpreter.getPrompts());
	}

	@Test
	public void ifClearNameListContainsUndeclaredVariableHeThrowSemanticError() throws IOException,
			ParserConfigurationException, SAXException {

		List<Prompt> expectedprompts = new ArrayList<Prompt>();
		Prompt prompt = new Prompt();
		prompt.tts = "semantic error";
		expectedprompts.add(prompt);

		interpreter = new Interpreter(url
				+ "ifClearNameListContainsUndeclaredVariableHeThrowSemanticError.vxml");
		interpreter.start();

		assertEquals(expectedprompts, interpreter.getPrompts());
	}

	@Test
	public void whenClearTagNotIndicateNamelistAllFormitemIsCleared() throws IOException,
			ParserConfigurationException, SAXException {

		List<Prompt> expectedprompts = new ArrayList<Prompt>();
		Prompt prompt = new Prompt();
		prompt.tts = "It is good toto and tata is cleared.";
		expectedprompts.add(prompt);

		interpreter = new Interpreter(url
				+ "whenClearTagNotIndicateNamelistAllFormitemIsCleared.vxml");
		interpreter.start();

		assertEquals(expectedprompts, interpreter.getPrompts());
	}

	@Test
	public void testHeartWelcomeMessageAndSaySubmitDTMF() throws IOException,
			ParserConfigurationException, SAXException {
		List<Prompt> expectedprompts = new ArrayList<Prompt>();
		Prompt prompt = new Prompt();
		prompt.tts = "Bonjour, bienvenue chez Orange et France Télécom. "
				+ "Le temps d'attente avant la mise en relation avec votre conseiller "
				+ "est gratuit. Cet appel est facturé au tarif d'une communication "
				+ "locale si vous appelez d'une ligne fixe France Télécom ... .";
		expectedprompts.add(prompt);

		prompt = new Prompt();
		prompt.tts = "C'est à vous !";
		expectedprompts.add(prompt);

		prompt = new Prompt();
		prompt.tts = "Votre numero est le 0658585895. Merci de votre appel.";
		expectedprompts.add(prompt);

		interpreter = new Interpreter(url + "accueil.vxml");
		interpreter.start();
		interpreter.submitDtmf("0658585895");

		assertEquals(expectedprompts, interpreter.getPrompts());
	}

	@Test
	public void testIfExprIsIndicateToLogItIsExecuted() throws IOException,
			ParserConfigurationException, SAXException {
		List<String> expectedLogs = new ArrayList<String>();

		expectedLogs.add("labelThatMustBePresentInLogMessage This is a log message.");

		interpreter = new Interpreter(url + "1163.txml");
		interpreter.start();

		System.err.println(interpreter.getLogs());
		System.err.println(expectedLogs);
		assertEquals(expectedLogs, interpreter.getLogs());
	}

	@Test
	public void logTagSouldCombinatePCDATAAndValueElement() throws IOException,
			ParserConfigurationException, SAXException {
		// The <log> element may contain any combination of text (CDATA) and
		// <value> elements.
		List<String> expectedLogs = new ArrayList<String>();

		expectedLogs.add("This is a log message. "
				+ "firstVariable value must be 2: firstVariable = 2.0 "
				+ "secondVariable incremented by firstVariable must be 1002: "
				+ "secondVariable + firstVariable = 1002.0");

		interpreter = new Interpreter(url + "1152.txml");
		interpreter.start();

		System.err.println(interpreter.getLogs());
		System.err.println(expectedLogs);
		assertEquals(expectedLogs, interpreter.getLogs());
	}

	@Test
	public void testTalk() throws IOException, ParserConfigurationException, SAXException {
		List<Prompt> expectedprompts = new ArrayList<Prompt>();
		Prompt prompt = new Prompt();

		prompt.tts = "Bonjour, merci de prononcer une phrase.";
		expectedprompts.add(prompt);

		prompt = new Prompt();
		prompt.tts = "Vous avez dit salut. Merci de votre appel.";
		expectedprompts.add(prompt);

		interpreter = new Interpreter(url + "talk.vxml");
		interpreter.start();
		interpreter.talk("salut");

		assertEquals(expectedprompts, interpreter.getPrompts());
	}

	@Test
	public void testPromptIsWhenItHasGoodcount() throws Exception {
		List<Prompt> expectedPrompts = new ArrayList<Prompt>();
		List<String> expectedLogs = new ArrayList<String>();

		Interpreter interpreter = new Interpreter(url + "promptCounter.vxml");
		interpreter.start();

		assertEquals(expectedPrompts, interpreter.getPrompts());
		assertEquals(expectedLogs, interpreter.getLogs());
	}
}