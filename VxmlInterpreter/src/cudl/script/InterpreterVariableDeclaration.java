package cudl.script;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import cudl.utils.RemoteFileAccess;
import cudl.utils.SessionFileCreator;

public class InterpreterVariableDeclaration {
	private ScriptEngineManager manager;
	private ScriptEngine engine;
	private InterpreterScriptContext context;

	private ScriptableObject sharedScope;
	private ScriptableObject anonymeScope;
	private ScriptableObject dialogScope;
	private ScriptableObject documentScope;
	private ScriptableObject applicationScope;
	private ScriptableObject sessionScope;

	private String location;
	private List<String> normalizedApplicationVariable = new ArrayList<String>() {
		{
			add("lastresult$ = new Array()");
			add("lastresult$[0] = new Object()");
			add("lastresult$[0].confidence = 1");
			add("lastresult$[0].utterance = undefined");
			add("lastresult$[0].inputmode = undefined");
			add("lastresult$[0].interpretation = undefined");
		}
	};

	public InterpreterVariableDeclaration(String scriptLocation)
			throws IOException, ScriptException {
		manager = new ScriptEngineManager();
		engine = manager.getEngineByName("ecmascript");
		System.err.println("ENGINE VERSION "
				+ engine.getFactory().getEngineVersion());
		context = new DefaultInterpreterScriptContext();
		// dialogItemName = new Hashtable<Node, String>();
		location = scriptLocation;

		// RHINO
		Context context = Context.enter();
		sharedScope = context.initStandardObjects();

		sessionScope = (ScriptableObject) context.newObject(sharedScope);
		sessionScope.put("session", sessionScope, sessionScope);
		sessionScope.setPrototype(sharedScope);

		applicationScope = (ScriptableObject) context.newObject(sessionScope);
		applicationScope.put("application", applicationScope, applicationScope);
		applicationScope.setPrototype(sessionScope);

		documentScope = (ScriptableObject) context.newObject(applicationScope);
		documentScope.put("document", documentScope, documentScope);
		documentScope.setPrototype(applicationScope);

		dialogScope = (ScriptableObject) context.newObject(documentScope);
		dialogScope.put("dialog", dialogScope, dialogScope);
		dialogScope.setPrototype(documentScope);

		anonymeScope = (ScriptableObject) context.newObject(dialogScope);
		anonymeScope.setPrototype(dialogScope);

		addVariableNormalized();
	}

	private void addVariableNormalized() throws IOException, ScriptException {
		try {
			// declarareScope(InterpreterScriptContext.SESSION_SCOPE);
			File sessionFile = new SessionFileCreator().get3900DefaultSession();
			if (null != sessionFile) {
				// engine.eval(new FileReader(sessionFile),
				// getBindings(InterpreterScriptContext.SESSION_SCOPE));
				Context ctxt = Context.enter();
				ctxt.evaluateReader(sessionScope, new FileReader(sessionFile),
						sessionFile.getName(), 1, null);
				sessionFile.delete();
			}

			// declarareScope(InterpreterScriptContext.APPLICATION_SCOPE);
			declarareNormalizeApplication();
			// declarareScope(InterpreterScriptContext.DOCUMENT_SCOPE);
			// declarareScope(InterpreterScriptContext.DIALOG_SCOPE);
		} catch (ScriptException e) {
			throw new ScriptException("Vxml interpreter internal error "
					+ e.toString());
		}
	}

	private void declarareNormalizeApplication() throws ScriptException {
		for (Iterator<String> appliVar = normalizedApplicationVariable
				.iterator(); appliVar.hasNext();) {
			String script = (String) appliVar.next();
			// engine.eval(script, context);
			String[] split = script.split("=");
			// applicationScope.put(split[0], applicationScope, split[1]);
			Context.enter().evaluateString(applicationScope, script, script, 1,
					null);
		}
	}

	public void declareVariable(String name, String value, int scope)
			throws ScriptException {
		// getBindings(scope).put(name,
		// engine.eval(getReplaceBindingName(value), context));

		getScope(scope)
				.put(name, getScope(scope), evaluateScript(value, scope));

	}

	public Object evaluateScript(String script, int scope)
			throws ScriptException {
		Context ctxt = Context.enter();
		Object ret = ctxt.evaluateString(getScope(scope), script, script
				+ scope, 1, null);
		System.err.println(ret + "\t\tin\t\t" + script);
		return ret;
		// return engine.eval(getReplaceBindingName(script), context) + "";
	}

	public void setValue(String name, String value, int scope)
			throws ScriptException {
		Context ctxt = Context.enter();
		ctxt.evaluateString(getScope(scope), name + "=" + value, name + "="
				+ value, 1, null);

		ScriptableObject start = getScope(scope);
		while (start != null) {
			if (start.has(name, start))
				start.put(name, start, evaluateScript(value, scope));
			start = (ScriptableObject) start.getPrototype();
		}
	}

	public Object getValue(String name) throws ScriptException {
		Context ctxt = Context.enter();
		Object ret = ctxt.evaluateString(anonymeScope, name, "<Eval>", 1, null);
		// Object eval = null;
		// eval = engine.eval(getReplaceBindingName(name), context);

		return ret; // (null == eval) ? "undefined" : eval;
	}

	public void resetScopeBinding(int scope) throws ScriptException {
		Context context = Context.enter();
		switch (scope) {
		case InterpreterScriptContext.APPLICATION_SCOPE:
			applicationScope = (ScriptableObject) context
					.newObject(sessionScope);
			applicationScope.put("application", applicationScope,
					applicationScope);
			applicationScope.setPrototype(sessionScope);
			declarareNormalizeApplication();
		case InterpreterScriptContext.DOCUMENT_SCOPE:
			documentScope = (ScriptableObject) context
					.newObject(applicationScope);
			documentScope.put("document", documentScope, documentScope);
			documentScope.setPrototype(applicationScope);
		case InterpreterScriptContext.DIALOG_SCOPE:
			dialogScope = (ScriptableObject) context.newObject(documentScope);
			dialogScope.put("dialog", dialogScope, dialogScope);
			dialogScope.setPrototype(documentScope);
		case InterpreterScriptContext.ANONYME_SCOPE:
			anonymeScope = (ScriptableObject) context.newObject(dialogScope);
			anonymeScope.setPrototype(dialogScope);
			break;

		default:
			break;
		}

		if (scope <= 90)
			getBindings(scope).clear();
		try {
			if (scope != 50)
				declarareScope(scope);
			if (scope == InterpreterScriptContext.APPLICATION_SCOPE)
				declarareNormalizeApplication();
		} catch (ScriptException e) {
		}
	}

	private void declarareScope(int scope) throws ScriptException {
		engine.eval(getScopeName(scope) + " = new Object();", context);

	}

	private String getScopeName(int scope) {
		String scopeName;
		switch (scope) {
		case InterpreterScriptContext.APPLICATION_SCOPE:
			scopeName = "application";
			break;
		case InterpreterScriptContext.DOCUMENT_SCOPE:
			scopeName = "document";
			break;
		case InterpreterScriptContext.DIALOG_SCOPE:
			scopeName = "dialog";
			break;
		case InterpreterScriptContext.SESSION_SCOPE:
			scopeName = "session";
			System.err.println("session");
			break;
		default:
			scopeName = "anonyme";
		}

		return scopeName;
	}

	private String getReplaceBindingName(String name) {
		return name.replaceAll(
				"session\\.|application\\.|document\\.|dialog\\.", "");
	}

	private Bindings getBindings(int scope) {
		return context.getBindings(scope);
	}

	public Object evaluateFileScript(String fileName, int scope)
			throws ScriptException, IOException {
		File remoteFile = RemoteFileAccess.getRemoteFile(fileName);
		Context ctxt = Context.enter();
		return ctxt.evaluateReader(getScope(scope), new FileReader(remoteFile),
				remoteFile.getName(), 1, null);

		// return engine.eval(new FileReader(remoteFile), context);
	}

	private ScriptableObject getScope(int scope) {
		switch (scope) {
		case InterpreterScriptContext.ANONYME_SCOPE:
			return anonymeScope;

		case InterpreterScriptContext.DIALOG_SCOPE:
			return dialogScope;
		case InterpreterScriptContext.DOCUMENT_SCOPE:
			return documentScope;

		case InterpreterScriptContext.APPLICATION_SCOPE:
			return applicationScope;
		case InterpreterScriptContext.SESSION_SCOPE:
			return sessionScope;
		default:
			throw new IllegalArgumentException("Undefined Scope");
		}
	}
}
