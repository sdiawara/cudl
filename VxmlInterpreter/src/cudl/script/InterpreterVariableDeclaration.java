package cudl.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import cudl.utils.CudlSession;

public class InterpreterVariableDeclaration {
	private static final int SCOPE_STEP = 10;
	private static final String APPLICATION_VARIABLES = "lastresult$ = new Array(); "
			+ "lastresult$[0] = new Object(); " + "lastresult$[0].confidence = 1; "
			+ "lastresult$[0].utterance = undefined;" + "lastresult$[0].inputmode = undefined;"
			+ "lastresult$[0].interpretation = undefined;";
	public static final int SESSION_SCOPE = 90;
	public static final int APPLICATION_SCOPE = 80;
	public static final int DOCUMENT_SCOPE = 70;
	public static final int DIALOG_SCOPE = 60;
	public static final int ANONYME_SCOPE = 50;

	private ScriptableObject sharedScope;
	private ScriptableObject sessionScope;
	private ScriptableObject applicationScope;
	private ScriptableObject documentScope;
	private ScriptableObject dialogScope;
	private ScriptableObject anonymeScope;

	private Stack<ScriptableObject> peekStack = new Stack<ScriptableObject>();
	private Stack<ScriptableObject> tmpStatck = new Stack<ScriptableObject>();
	private int currentScope;
	private final String sessionVariables;

	public InterpreterVariableDeclaration(String sessionVariables) throws IOException {
		this.sessionVariables = sessionVariables;
		Context context = new ContextFactory().enterContext();

		sessionScope = context.initStandardObjects();
		sessionScope.put("session", sessionScope, sessionScope);

		applicationScope = (ScriptableObject) context.newObject(sessionScope);
		applicationScope.put("application", applicationScope, applicationScope);
		applicationScope.setParentScope(sessionScope);

		documentScope = (ScriptableObject) context.newObject(applicationScope);
		documentScope.put("document", documentScope, documentScope);
		documentScope.setParentScope(applicationScope);

		dialogScope = (ScriptableObject) context.newObject(documentScope);
		dialogScope.put("dialog", dialogScope, dialogScope);
		dialogScope.setParentScope(documentScope);

		anonymeScope = (ScriptableObject) context.newObject(dialogScope);
		anonymeScope.setParentScope(dialogScope);

		peekStack.push(sessionScope);
		declareNormalizedSessionVariables();
		peekStack.push(applicationScope);
		declarareNormalizedApplicationVariables();

		tmpStatck.push(anonymeScope);
		tmpStatck.push(dialogScope);
		tmpStatck.push(documentScope);

		currentScope = 80;
	}

	public void declareVariable(String name, Object value, int scope) {
		getScope(scope).put(name, getScope(scope), evaluateScript(value + "", scope));
	}

	// ***
	public void declareVariableNew(String name, Object value) {
		ScriptableObject currentScope = peekStack.peek();
		currentScope.put(name, currentScope, evaluateScriptNew(value + ""));
	}

	public Object evaluateScript(String script, int scope) {
		Context context = Context.enter();
		ScriptableObject scope2 = getScope(scope);
		return context.evaluateString(scope2, script, script + " " + scope, 1, null);
	}

	// ***
	public Object evaluateScriptNew(String script) {
		Context context = Context.enter();
		return context.evaluateString(peekStack.peek(), script, script, 1, null);
	}

	public Object evaluateFileScript(String fileName, int scope) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new URL(fileName).openStream()));
		try {
			return Context.enter().evaluateReader(getScope(scope), in, fileName, 1, null);
		} finally {
			in.close();
		}
	}

	public Object evaluateFileScriptNew(String fileName, int scope) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new URL(fileName).openStream()));
		try {
			return Context.enter().evaluateReader(peekStack.peek(), in, fileName, 1, null);
		} finally {
			in.close();
		}
	}

	public void setValue(String name, Object value) {
		Context ctxt = Context.enter();
		Object valueEvaluation = ctxt.evaluateString(anonymeScope, value.toString(), value.toString(), 1, null);

		if (valueEvaluation instanceof String)
			valueEvaluation = "'" + valueEvaluation + "'";

		if (valueEvaluation instanceof Undefined)
			valueEvaluation = "undefined";

		ctxt.evaluateString(searchDeclarationScope(name), name + "=" + valueEvaluation + "", name + "="
				+ valueEvaluation + ";", 1, null);
	}

	public Object getValue(String name) {
		return Context.enter().evaluateString(anonymeScope, name, name, 1, null);
	}

	public Object getValueNew(String name) {
		return Context.enter().evaluateString(anonymeScope, name, name, 1, null);
	}

	public void resetScopeBinding(int scope) {
		System.err.println("reset scope" + scope);
		Context context = Context.enter();
		switch (scope) {
		case APPLICATION_SCOPE:
			System.err.println("reset scope APPLICATION");
			applicationScope = (ScriptableObject) context.newObject(sessionScope);
			applicationScope.put("application", applicationScope, applicationScope);
			applicationScope.setParentScope(sessionScope);
			declarareNormalizedApplicationVariables();
			break;
		case DOCUMENT_SCOPE:
			documentScope = (ScriptableObject) context.newObject(applicationScope);
			documentScope.put("document", documentScope, documentScope);
			documentScope.setParentScope(applicationScope);
		case DIALOG_SCOPE:
			dialogScope = (ScriptableObject) context.newObject(documentScope);
			dialogScope.put("dialog", dialogScope, dialogScope);
			dialogScope.setParentScope(documentScope);
		case ANONYME_SCOPE:
			anonymeScope = (ScriptableObject) context.newObject(dialogScope);
			anonymeScope.setParentScope(dialogScope);
			break;
		}
	}

	private ScriptableObject searchDeclarationScope(String name) {
		ScriptableObject start = anonymeScope;
		String search = name.split("\\.")[0];
		while (start != sharedScope) {
			if (start.has(search, start)) {
				return start;
			}
			start = (ScriptableObject) start.getParentScope();
		}
		return null;
	}

	private void declareNormalizedSessionVariables() throws IOException {
		Context ctxt = new ContextFactory().enterContext();
		if (sessionVariables != null) {
				evaluateScript(sessionVariables, SESSION_SCOPE);
		}

		try {
			Class<?> cudlSessionFile = Class.forName("test.Session");
			String sessionScript = ((CudlSession) cudlSessionFile.newInstance()).getSessionScript();
			ctxt.evaluateString(sessionScope, sessionScript, sessionScript, 1, null);
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (ClassNotFoundException e) {
			String msg = "INFO: You can define a Session class in the test package implementing cudl.utils.CudlSession !!! "
					+ "See https://github.com/multimediabs/cudl/blob/master/VxmlInterpreter/test/test/Session.java";
			System.err.println(msg);
			// throw new RuntimeException(msg);
		}
	}

	private void declarareNormalizedApplicationVariables() {
		Context.enter().evaluateString(applicationScope, APPLICATION_VARIABLES, APPLICATION_VARIABLES, 1, null);
	}

	private ScriptableObject getScope(int scope) {
		switch (scope) {
		case ANONYME_SCOPE:
			return anonymeScope;
		case DIALOG_SCOPE:
			return dialogScope;
		case DOCUMENT_SCOPE:
			return documentScope;
		case APPLICATION_SCOPE:
			return applicationScope;
		case SESSION_SCOPE:
			return sessionScope;
		default:
			throw new IllegalArgumentException("Scope " + scope + " Undefined");
		}
	}

	public void enterScope() {
		System.err.println(peekStack.size() + "size");
		if (currentScope > 50) {
			currentScope -= SCOPE_STEP;
			peekStack.push(tmpStatck.pop());
		}
	}

	public void exitScope() {
		if (currentScope < 90) {
			ScriptableObject pop = peekStack.pop();
			resetScopeBinding(currentScope);
			currentScope += SCOPE_STEP;
			tmpStatck.push(pop);
		}
	}
}
