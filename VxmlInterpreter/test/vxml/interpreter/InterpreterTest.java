package vxml.interpreter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import vxml.interpreter.execption.InterpreterExecption;

public class InterpreterTest {
	private List<VariableVxml> varExcepted;
	private Interpreter interpreter;
	private InterpreterContext interpreterContext;

	@Before
	public void setUp() throws Exception {
		varExcepted = new ArrayList<VariableVxml>();
	}

	@Test
	public void variableDeclarationTest() throws SAXException, IOException,
			InterpreterExecption {
		varExcepted.add(new VariableVxml("un", "1"));
		varExcepted.add(new VariableVxml("deux", "un+1"));
		varExcepted.add(new VariableVxml("trois", "deux+1"));
		varExcepted.add(new VariableVxml("continuer", "undefined"));

		interpreterContext = new InterpreterContext("variable.vxml");
		interpreterContext.launchInterpreter();

		assertEquals(varExcepted,
				interpreterContext.interpreter.currentNodeVariables);
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

		assertEquals(traceLog, interpreterContext.interpreter.getTraceLog());
		assertEquals(traceStat, interpreterContext.interpreter.getTraceStat());
	}

	@Test
	public void testLogTraceWithExit() throws SAXException, IOException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");

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

		List<VariableVxml> varExecpeted = new ArrayList<VariableVxml>();
		varExecpeted.add(new VariableVxml("block_0", "undefined"));
		varExecpeted.add(new VariableVxml("block_1", "undefined"));
		varExecpeted.add(new VariableVxml("block_2", "undefined"));
		varExecpeted.add(new VariableVxml("block_3", "undefined"));
		varExecpeted.add(new VariableVxml("telephone", "+689 123456"));
		varExecpeted.add(new VariableVxml("telephone1", "+356 689999"));

		interpreterContext = new InterpreterContext("shelloVar.vxml");
		interpreterContext.launchInterpreter();

		assertEquals(traceLog, interpreterContext.interpreter.getTraceLog());
		assertEquals(traceStat, interpreterContext.interpreter.getTraceStat());
		assertEquals(varExecpeted,
				interpreterContext.interpreter.currentNodeVariables);
	}

	@Test
	public void testLogTraceWhithVariableWithClear() throws SAXException,
			IOException {
		List<String> traceLog = new ArrayList<String>();
		traceLog.add("LOG Hello");
		traceLog.add("LOG Hello 1");
		traceLog.add("LOG Hello 2");
		traceLog.add("LOG Hello 3");

		List<String> traceStat = new ArrayList<String>();
		traceStat.add("[label:stats] LOG Hello");

		List<VariableVxml> varExecpeted = new ArrayList<VariableVxml>();
		varExecpeted.add(new VariableVxml("block_0", "undefined"));
		varExecpeted.add(new VariableVxml("block_1", "undefined"));
		varExecpeted.add(new VariableVxml("block_2", "undefined"));
		varExecpeted.add(new VariableVxml("block_3", "undefined"));
		varExecpeted.add(new VariableVxml("telephone", "undefined"));
		varExecpeted.add(new VariableVxml("telephone1", "undefined"));

		interpreterContext = new InterpreterContext("shelloVarClear.vxml");
		interpreterContext.launchInterpreter();

		assertEquals(traceLog, interpreterContext.interpreter.getTraceLog());
		assertEquals(traceStat, interpreterContext.interpreter.getTraceStat());
		assertEquals(varExecpeted,
				interpreterContext.interpreter.currentNodeVariables);
	}

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

		// System.out.println(prompts);
		// System.out.println(interpreterContext.interpreter.getPrompts());

		assertTrue(interpreterContext.interpreter.getPrompts().size() == 1);
		assertEquals(prompts, interpreterContext.interpreter.getPrompts());
	}

}