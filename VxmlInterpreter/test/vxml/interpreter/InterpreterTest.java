package vxml.interpreter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import org.xml.sax.SAXException;

import fr.mbs.vxml.interpreter.InterpreterContext;
import fr.mbs.vxml.interpreter.execption.InterpreterException;
import fr.mbs.vxml.utils.Prompt;

public class InterpreterTest {
	private Map<String, String> varExcepted = new TreeMap<String, String>();
	private InterpreterContext interpreterContext;

	@Test
	public void variableDeclarationTest() throws SAXException, IOException,
			InterpreterException {
	
		interpreterContext = new InterpreterContext("variable.vxml");
		//interpreterContext.launchInterpreter();

		//assertEquals("3.0", interpreterContext.interpreter.variableVxml.getValue("trois"));
	}

	@Test
	public void testLogTrace() throws SAXException, IOException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 1");
		traceLog.add("LOG Hello 2");
		traceLog.add("LOG Hello 3");

		List<String> traceStat = new ArrayList<String>();
		traceStat.add("[label:stats] LOG Hello");

		interpreterContext = new InterpreterContext("shello2.vxml");
		interpreterContext.launchInterpreter();

		assertEquals(traceStat, interpreterContext.interpreter.getTraceStat());
		assertEquals(traceLog, interpreterContext.interpreter.getTraceLog());
	}

	@Test
	public void testLogTraceWithExit() throws SAXException, IOException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 1");

		List<String> traceStat = new ArrayList<String>();
		traceStat.add("[label:stats] LOG Hello");

		interpreterContext = new InterpreterContext("shelloExit.vxml");
		interpreterContext.launchInterpreter();

		assertEquals(traceLog, interpreterContext.interpreter.getTraceLog());
		assertEquals(traceStat, interpreterContext.interpreter.getTraceStat());
	}

	@Test
	public void testLogTraceWhithVariable() throws SAXException, IOException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 2");
		traceLog.add("LOG Hello 2");
		traceLog.add("LOG Hello 3");

		List<String> traceStat = new ArrayList<String>();
		traceStat.add("[label:stats] LOG Hello");

		varExcepted = new TreeMap<String, String>();
		varExcepted.put("block_0", "defined");
		varExcepted.put("block_1", "defined");
		varExcepted.put("block_2", "defined");
		varExcepted.put("block_3", "defined");
		varExcepted.put("telephone", "+689 123456");
		varExcepted.put("telephone1", "+356 689999");

		interpreterContext = new InterpreterContext("shelloVar.vxml");
		interpreterContext.launchInterpreter();

		assertEquals(traceStat, interpreterContext.interpreter.getTraceStat());
		//sertEquals(varExcepted, interpreterContext.interpreter.getVar());

		assertEquals(traceLog, interpreterContext.interpreter.getTraceLog());
	}

//	@Test
//	public void testLogTraceWhithVariableWithClear() throws SAXException,
//			IOException {
//		List<String> traceLog = new ArrayList<String>();
//		traceLog.add("LOG Hello");
//		traceLog.add("LOG Hello 1");
//		traceLog.add("LOG Hello 2");
//		traceLog.add("LOG Hello 3");
//
//		List<String> traceStat = new ArrayList<String>();
//		traceStat.add("[label:stats] LOG Hello");
//
//		varExcepted = new TreeMap<String, String>();
//		varExcepted.put("block_0", "defined");
//		varExcepted.put("block_1", "defined");
//		varExcepted.put("block_2", "defined");
//		varExcepted.put("block_3", "defined");
//		varExcepted.put("telephone", "undefined");
//		varExcepted.put("telephone1", "undefined");
//
//		interpreterContext = new InterpreterContext("shelloVarClear.vxml");
//		interpreterContext.launchInterpreter();
//
//		assertEquals(traceLog, interpreterContext.interpreter.getTraceLog());
//		assertEquals(traceStat, interpreterContext.interpreter.getTraceStat());
//		assertEquals(varExcepted, interpreterContext.interpreter.getVar());
//	}

	@Test
	public void testLogTraceWithIfElseifAndElse() throws SAXException,
			IOException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 1");
		traceLog.add("LOG Hello 2");
		traceLog.add("LOG Hello A");
		traceLog.add("LOG Hello B");
		traceLog.add("LOG Hello 3");
		traceLog.add("LOG Hello 4");
		traceLog.add("LOG Hello 5");

		interpreterContext = new InterpreterContext(
				"shelloWith_if_elseif_and_else.vxml");
		interpreterContext.launchInterpreter();

		assertTrue(interpreterContext.interpreter.getTraceStat().isEmpty());
		assertEquals(traceLog, interpreterContext.interpreter.getTraceLog());
	}

	@Test
	public void testLogManyBlock() throws SAXException, IOException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 1");
		traceLog.add("LOG Hello 2");

		interpreterContext = new InterpreterContext("manyBlocks.vxml");
		interpreterContext.launchInterpreter();

		assertTrue(interpreterContext.interpreter.getTraceStat().isEmpty());
		assertEquals(traceLog, interpreterContext.interpreter.getTraceLog());
	}

	@Test
	public void testLogGotoInASameDialog() throws SAXException, IOException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 1");
		traceLog.add("LOG Hello 2");
		traceLog.add("LOG Hello 3");
		traceLog.add("LOG Hello 4");

		interpreterContext = new InterpreterContext("goto.vxml");
		interpreterContext.launchInterpreter();

		assertTrue(interpreterContext.interpreter.getTraceStat().isEmpty());
		assertEquals(traceLog, interpreterContext.interpreter.getTraceLog());
	}

	@Test
	public void testCollectPrompt() throws SAXException, IOException {
		List<Prompt> prompts = new ArrayList<Prompt>();
		Prompt promptExecepeted;

		promptExecepeted = new Prompt();
		promptExecepeted.timeout = "200ms";
		promptExecepeted.bargein = "true";
		promptExecepeted.audio = "/ROOT/prompts/WAV/ACCUEIL_CHOIX_TARIFS.wav ";
		promptExecepeted.tts = "Pour avoir des informations détaillées sur ce tarif, dites : « tarif ». ";
		prompts.add(promptExecepeted);

		promptExecepeted = new Prompt();
		promptExecepeted.tts = "dites tarif. ";
		prompts.add(promptExecepeted);

		promptExecepeted = new Prompt();
		promptExecepeted.timeout = "300ms";
		promptExecepeted.bargein = "true";
		promptExecepeted.audio = "/ROOT/prompts/WAV/ACCUEIL_CHOIX_TARIFS.wav ";
		promptExecepeted.tts = "Pour avoir des informations détaillées sur ce tarif, dites : « tarif ». ";
		prompts.add(promptExecepeted);

		promptExecepeted = new Prompt();
		promptExecepeted.tts = "dites tarif. ";
		prompts.add(promptExecepeted);

		interpreterContext = new InterpreterContext("prompt.vxml");
		interpreterContext.launchInterpreter();

		assertEquals(prompts, interpreterContext.interpreter.getPrompts());
	}

	@Test
	public void testCombinattionPromptTrace() throws SAXException, IOException {
		List<Prompt> prompts = new ArrayList<Prompt>();
		Prompt promptExecepeted;

		promptExecepeted = new Prompt();
		promptExecepeted.audio = "rtsp://www.piafcauserie.example.com/grive.wav http://www.piafcauserie.example.com/tourterelle.wav ";
		promptExecepeted.tts = "Bienvenue chez Graines a gogo. Ce mois-ci nous proposons le barril de 250 kg de graines de chardon a 299.95€ frais d'expédition et de transport compris. ";
		prompts.add(promptExecepeted);

		interpreterContext = new InterpreterContext("prompt1.vxml");
		interpreterContext.launchInterpreter();

		assertTrue(interpreterContext.interpreter.getPrompts().size() == 1);
		assertEquals(prompts, interpreterContext.interpreter.getPrompts());
	}

	@Test()
	public void testNodeValue() throws SAXException, IOException {
		varExcepted = new TreeMap<String, String>();
		varExcepted.put("block_0", "defined");
		varExcepted.put("appli_scope_var",
				"cette variable est defini partout dans le formulaire");

		List<Prompt> prompts = new ArrayList<Prompt>();
		Prompt promptExecepeted;

		promptExecepeted = new Prompt();
		promptExecepeted.tts = "cette variable est defini partout dans le formulaire";
		prompts.add(promptExecepeted);

		promptExecepeted = new Prompt();
		promptExecepeted.tts = "ha ok: cette variable est defini partout dans le formulaire. c'est compris ";
		prompts.add(promptExecepeted);

		interpreterContext = new InterpreterContext("root.vxml");
		interpreterContext.launchInterpreter();

	//ssertEquals(varExcepted, interpreterContext.interpreter.getVar());
		assertEquals(prompts, interpreterContext.interpreter.getPrompts());
	}


	@Test
	public void sideEffectInScript() throws SAXException, IOException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("bla bla");
		traceLog.add("un exemple de variable de global avec effet de bord");

		interpreterContext = new InterpreterContext("sideEffectInScope.vxml");
		interpreterContext.launchInterpreter();

		assertEquals(traceLog, interpreterContext.interpreter.getTraceLog());
	}

	@Test(expected = IllegalArgumentException.class)
	public void anonymeScopeVariable() throws SAXException, IOException {
		interpreterContext = new InterpreterContext("anonymeScopeVariable.vxml");
		interpreterContext.launchInterpreter();
	}

	@Test(expected = IllegalArgumentException.class)
	public void dialogScopeVariable() throws SAXException, IOException {
		interpreterContext = new InterpreterContext("dialogScopeVariable.vxml");
		interpreterContext.launchInterpreter();
	}
}