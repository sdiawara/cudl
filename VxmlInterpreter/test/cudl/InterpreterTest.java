package cudl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;
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
	public void testLogTrace() throws IOException, ScriptException,
			ParserConfigurationException, SAXException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 1");
		traceLog.add("LOG Hello 2");
		traceLog.add("LOG Hello 3");

		List<String> traceStat = new ArrayList<String>();
		traceStat.add("LOG Hello");

		interpreter = new Interpreter(url + "shello2.vxml");
		interpreter.start();
		
		assertEquals(traceStat, interpreter.getTracetWithLabel("stats"));
		assertEquals(traceLog, interpreter.getTraceLog());
		// assertTrue(interpreter.raccrochage());
	}

	@Test
	public void testLogTraceWithExit() throws IOException, ScriptException,
			ParserConfigurationException, SAXException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 1");

		List<String> traceStat = new ArrayList<String>();
		traceStat.add("LOG Hello");
		interpreter = new Interpreter(url + "shelloExit.vxml");
		interpreter.start();

		System.err.println(traceLog);
		System.err.println(interpreter.getTraceLog());
		assertEquals(traceLog, interpreter.getTraceLog());
		assertEquals(traceStat, interpreter.getTracetWithLabel("stats"));
		// assertTrue(interpreter.raccrochage());
	}

	@Test
	public void testLogTraceWhithVariable() throws IOException,
			ScriptException, ParserConfigurationException, SAXException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 2 +356 689999");
		traceLog.add("LOG Hello 2");
		traceLog.add("LOG Hello 3");

		List<String> traceStat = new ArrayList<String>();
		traceStat.add("LOG Hello");
		interpreter = new Interpreter(url + "shelloVar.vxml");
		interpreter.start();

		assertEquals(traceStat, interpreter.getTracetWithLabel("stats"));

		assertEquals(traceLog, interpreter.getTraceLog());
		// assertTrue(interpreter.raccrochage());
	}

	// @Test
	// public void testLogTraceWhithVariableWithClear() throws
	// IOException {
	// List<String> traceLog = new ArrayList<String>();
	// traceLog.add("LOG Hello");
	// traceLog.add("LOG Hello 1");
	// traceLog.add("LOG Hello 2");
	// traceLog.add("LOG Hello 3");
	//
	// List<String> traceStat = new ArrayList<String>();
	// traceStat.add("[label:stats] LOG Hello");
	//
	// varExcepted = new TreeMap<String, String>();
	// varExcepted.put("block_0", "defined");
	// varExcepted.put("block_1", "defined");
	// varExcepted.put("block_2", "defined");
	// varExcepted.put("block_3", "defined");
	// varExcepted.put("telephone", "undefined");
	// varExcepted.put("telephone1", "undefined");
	//
	// interpreter = new Interpreter(url+"shelloVarClear.vxml");
	// interpreterContext.interpreter.start();
	//
	// assertEquals(traceLog, interpreterContext.interpreter.getTraceLog());
	// assertEquals(traceStat, interpreterContext.interpreter.getTraceStat());
	// assertEquals(varExcepted, interpreterContext.interpreter.getVar());
	// }

	@Test
	public void testLogTraceWithIfElseifAndElse() throws IOException,
			ScriptException, ParserConfigurationException, SAXException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 1");
		traceLog.add("LOG Hello 2");
		traceLog.add("LOG Hello A");
		traceLog.add("LOG Hello B");
		traceLog.add("LOG Hello 3");
		traceLog.add("LOG Hello 4");
		traceLog.add("LOG Hello 5");

		interpreter = new Interpreter(url
				+ "shelloWith_if_elseif_and_else.vxml");
		interpreter.start();

		assertTrue(interpreter.getTracetWithLabel("stats").isEmpty());

		System.err.println(traceLog);
		System.err.println(interpreter.getTraceLog());
		assertEquals(traceLog, interpreter.getTraceLog());
		// assertTrue(interpreter.raccrochage());
	}

	@Test
	public void testLogManyBlock() throws IOException, ScriptException,
			ParserConfigurationException, SAXException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 1");
		traceLog.add("LOG Hello 2");

		interpreter = new Interpreter(url + "manyBlocks.vxml");
		interpreter.start();

		assertTrue(interpreter.getTracetWithLabel("stats").isEmpty());
		assertEquals(traceLog, interpreter.getTraceLog());
	}

	@Test
	public void testLogGotoInASameDialog() throws IOException, ScriptException,
			ParserConfigurationException, SAXException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 1");
		traceLog.add("LOG Hello 2");
		traceLog.add("LOG Hello 3");
		traceLog.add("LOG Hello 4");

		interpreter = new Interpreter(url + "goto.vxml");
		interpreter.start();

		assertTrue(interpreter.getTracetWithLabel("stats").isEmpty());
		System.err.println(traceLog);
		System.err.println(interpreter.getTraceLog());

		assertEquals(traceLog, interpreter.getTraceLog());
	}

	@Test
	public void testCollectPrompt() throws IOException, ScriptException,
			ParserConfigurationException, SAXException {
		List<Prompt> prompts = new ArrayList<Prompt>();
		Prompt promptExecepeted;

		promptExecepeted = new Prompt();
		promptExecepeted.timeout = "200ms";
		promptExecepeted.bargein = "true";
		promptExecepeted.audio = "/ROOT/prompts/WAV/ACCUEIL_CHOIX_TARIFS.wav ";
		promptExecepeted.tts = "Pour avoir des informations détaillées sur ce tarif, dites : « tarif ».";
		prompts.add(promptExecepeted);

		promptExecepeted = new Prompt();
		promptExecepeted.tts = "dites tarif.";
		prompts.add(promptExecepeted);

		promptExecepeted = new Prompt();
		promptExecepeted.timeout = "300ms";
		promptExecepeted.bargein = "true";
		promptExecepeted.audio = "/ROOT/prompts/WAV/ACCUEIL_CHOIX_TARIFS.wav ";
		promptExecepeted.tts = "Pour avoir des informations détaillées sur ce tarif, dites : « tarif ».";
		prompts.add(promptExecepeted);

		promptExecepeted = new Prompt();
		promptExecepeted.tts = "dites tarif.";
		prompts.add(promptExecepeted);

		interpreter = new Interpreter(url + "prompt.vxml");
		interpreter.start();

		assertEquals(prompts, interpreter.getPrompts());
	}

	@Test
	public void testCombinattionPromptTrace() throws IOException,
			ScriptException, ParserConfigurationException, SAXException {
		List<Prompt> prompts = new ArrayList<Prompt>();
		Prompt promptExecepeted;

		promptExecepeted = new Prompt();
		promptExecepeted.audio = "rtsp://www.piafcauserie.example.com/grive.wav http://www.piafcauserie.example.com/tourterelle.wav ";
		promptExecepeted.tts = "Bienvenue chez Graines a gogo. Ce mois-ci nous proposons le barril de 250 kg de graines de chardon a 299.95€ frais d'expédition et de transport compris.";
		prompts.add(promptExecepeted);

		interpreter = new Interpreter(url + "prompt1.vxml");
		interpreter.start();

		assertTrue(interpreter.getPrompts().size() == 1);
		assertEquals(prompts, interpreter.getPrompts());
	}

	@Test()
	public void testNodeValue() throws IOException, ScriptException,
			ParserConfigurationException, SAXException {
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
	public void sideEffectInScript() throws IOException, ScriptException,
			ParserConfigurationException, SAXException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("bla bla");
		traceLog.add("un exemple de variable de global avec effet de bord");

		interpreter = new Interpreter(url + "sideEffectInScope.vxml");
		interpreter.start();

		assertEquals(traceLog, interpreter.getTraceLog());
	}

	@Test(expected = ScriptException.class)
	public void anonymeScopeVariable() throws IOException, ScriptException,
			ParserConfigurationException, SAXException {

		interpreter = new Interpreter(url + "anonymeScopeVariable.vxml");
		interpreter.start();
	}

	@Test(expected = ScriptException.class)
	public void dialogScopeVariable() throws IOException, ScriptException,
			ParserConfigurationException, SAXException {
		interpreter = new Interpreter(url + "dialogScopeVariable.vxml");
		interpreter.start();
	}

	@Test
	public void documentScopeVariableIsVisibleInAllDocument()
			throws IOException, ScriptException, URISyntaxException,
			ParserConfigurationException, SAXException {
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
	public void RootVariableIsAllWaysVisible() throws IOException,
			ScriptException, ParserConfigurationException, SAXException {
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

	@Test(expected = ScriptException.class)
	public void WhenRootChangeLastRootVariableIsNotLongerAccessible()
			throws IOException, ScriptException, ParserConfigurationException,
			SAXException {
		interpreter = new Interpreter(url + "rootChangeVariable.vxml");
		interpreter.start();
	}

	@Test
	public void comparerTwoVariableInDifferentScope() throws IOException,
			ScriptException, ParserConfigurationException, SAXException {

		interpreter = new Interpreter(url
				+ "compareTwoVariableDeclaredIndifferentScope");
		interpreter.start();
	}
}