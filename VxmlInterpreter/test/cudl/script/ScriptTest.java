package cudl.script;

import static org.junit.Assert.assertEquals;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Before;
import org.junit.Test;

import cudl.script.DefaultInterpreterScriptContext;
import cudl.script.InterpreterScriptContext;

public class ScriptTest {
	ScriptEngineManager engineManager = new ScriptEngineManager();
	ScriptEngine engine = engineManager.getEngineByName("ecmascript");
	private Bindings dialog;
	private Bindings anonyme;
	private Bindings document;
	private Bindings session;
	private Bindings appli;
	private DefaultInterpreterScriptContext context;

	@Before
	public void setup() throws ScriptException {
		context = new DefaultInterpreterScriptContext();
		anonyme = context.getBindings(InterpreterScriptContext.ANONYME_SCOPE);
		dialog = context.getBindings(InterpreterScriptContext.DIALOG_SCOPE);
		engine.eval("dialog = new Object()", dialog);
		document = context.getBindings(InterpreterScriptContext.DOCUMENT_SCOPE);
		engine.eval("document = new Object()", document);
		appli = context.getBindings(InterpreterScriptContext.APPLICATION_SCOPE);
		engine.eval("application = new Object()", appli);
		session = context.getBindings(InterpreterScriptContext.SESSION_SCOPE);
		engine.eval("session = new Object()", session);
	}

	@Test(expected = ScriptException.class)
	public void onlyDeclarationBindingContain() throws ScriptException {
		anonyme.put("x", "toto");
		System.err.println(engine.eval("x +' is declared in scope anonyme'",
				anonyme));
		try {
			engine.eval("x", dialog);
		} catch (ScriptException e) {
			System.err.println("this scope dialog not contains x");
			try {
				engine.eval("x", document);
			} catch (ScriptException e1) {
				System.err.println("this scope document not contains x");
				try {
					engine.eval("x", appli);
				} catch (ScriptException e2) {
					System.err.println("this scope applicaton not contains x");

					try {
						engine.eval("x", session);
					} catch (ScriptException e3) {
						System.err.println("this scope session not contains x");
						throw new ScriptException(e3);
					}
				}
			}

		}

	}

	@Test()
	public void whenDeclareAsameVariableInManyScopeTheLowestScopeDeclationIsReturned()
			throws ScriptException {

		anonyme.put("x", "anonyme");
		dialog.put("x", "dialog");
		document.put("x", "document");
		appli.put("x", "application");
		session.put("x", "session");

		assertEquals("anonyme", engine.eval("println(x);x", context));
		anonyme.clear();
		assertEquals("dialog", engine.eval("println(x);x", context));
		dialog.clear();
		assertEquals("document", engine.eval("println(x);x", context));
		document.clear();
		assertEquals("application", engine.eval("println(x);x", context));
		appli.clear();
		assertEquals("session", engine.eval("println(x);x", context));
	}

	@Test(expected = ScriptException.class)
	public void whenScripScopeChangeTheScripIsNoLongerAccessible()
			throws ScriptException {
		engine
				.eval(" function cat(s1, s2) {      return s1 + s2;    }",
						dialog);
		System.err.println(engine.eval("cat('test ', 'passed.')", context));
		dialog.clear();
		System.err.println(engine.eval("cat('test ', 'passed.')", context));
	}
}
