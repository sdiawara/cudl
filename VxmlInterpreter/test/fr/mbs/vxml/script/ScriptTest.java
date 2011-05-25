package fr.mbs.vxml.script;

import static org.junit.Assert.*;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.*;

public class ScriptTest {
	ScriptEngineManager engineManager = new ScriptEngineManager();
	ScriptEngine engine = engineManager.getEngineByName("ecmascript");
	private Bindings dialog;
	private Bindings anonyme;
	private Bindings document;

	@Before
	public void setup() {
		engine.setContext(new DefaultInterpreterScriptContext());
		anonyme = engine.getContext().getBindings(
				InterpreterScriptContext.ANONYME_SCOPE);

		dialog = engine.getContext().getBindings(
				InterpreterScriptContext.DIALOG_SCOPE);
		document = engine.getContext().getBindings(
				InterpreterScriptContext.DOCUMENT_SCOPE);
	}

	@Test(expected = ScriptException.class)
	public void onlyDeclarationBindingContain() throws ScriptException {

		Bindings anonyme = engine.getContext().getBindings(
				InterpreterScriptContext.ANONYME_SCOPE);

		anonyme.put("x", "toto");
		
		engine.eval("x", engine.getContext().getBindings(
				InterpreterScriptContext.SESSION_SCOPE));

	}

	@Test()
	public void whenDeclareAsameVariableInManyScopeTheLowestScopeDeclationIsReturned()
			throws ScriptException {

		anonyme.put("x", "anonyme");
		dialog.put("x", "dialog");
		dialog.put("y", "dialog");
		document.put("y", "document");
		
		assertEquals("anonyme", engine.eval("x"));
		assertEquals("dialog", engine.eval("println(x); y"));
	}

}
