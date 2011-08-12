package cudl.script;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import cudl.utils.RemoteFileAccess;
import cudl.utils.SessionFileCreator;

public class InterpreterVariableDeclaration {
	public static final int SESSION_SCOPE = 90;
	public static final int APPLICATION_SCOPE = 80;
	public static final int DOCUMENT_SCOPE = 70;
	public static final int DIALOG_SCOPE = 60;
	public static final int ANONYME_SCOPE = 50;

	private ScriptableObject sharedScope;
	private ScriptableObject anonymeScope;
	private ScriptableObject dialogScope;
	private ScriptableObject documentScope;
	private ScriptableObject applicationScope;
	private ScriptableObject sessionScope;

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

	public InterpreterVariableDeclaration(String scriptLocation) throws IOException {
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

	private void addVariableNormalized() throws IOException {
		File sessionFile = new SessionFileCreator().get3900DefaultSession();
		if (null != sessionFile) {
			Context ctxt = Context.enter();
			ctxt.evaluateReader(sessionScope, new FileReader(sessionFile), sessionFile.getName(), 1,
					null);
			sessionFile.delete();
		}

		declarareNormalizeApplication();
	}

	private void declarareNormalizeApplication() {
		for (Iterator<String> appliVar = normalizedApplicationVariable.iterator(); appliVar.hasNext();) {
			String script = (String) appliVar.next();
			Context.enter().evaluateString(applicationScope, script, script, 1, null);
		}
	}

	public void declareVariable(String name, String value, int scope) {
		getScope(scope).put(name, getScope(scope), evaluateScript(value, scope));
	}

	public Object evaluateScript(String script, int scope) {
		Context ctxt = Context.enter();
		return ctxt.evaluateString(getScope(scope), script, script + scope, 1, null);
	}

	public void setValue(String name, String value, int scope)  {
		Context ctxt = Context.enter();
		ctxt.evaluateString(getScope(scope), name + "=" + value, name + "=" + value, 1, null);

		ScriptableObject start = getScope(scope);
		while (start != null) {
			if (start.has(name, start))
				start.put(name, start, evaluateScript(value, scope));
			start = (ScriptableObject) start.getPrototype();
		}
	}

	public Object getValue(String name) {
		return Context.enter().evaluateString(anonymeScope, name, "<Eval>", 1, null);
	}

	public void resetScopeBinding(int scope) {
		Context context = Context.enter();
		switch (scope) {
		case APPLICATION_SCOPE:
			applicationScope = (ScriptableObject) context.newObject(sessionScope);
			applicationScope.put("application", applicationScope, applicationScope);
			applicationScope.setPrototype(sessionScope);
			declarareNormalizeApplication();
		case DOCUMENT_SCOPE:
			documentScope = (ScriptableObject) context.newObject(applicationScope);
			documentScope.put("document", documentScope, documentScope);
			documentScope.setPrototype(applicationScope);
		case DIALOG_SCOPE:
			dialogScope = (ScriptableObject) context.newObject(documentScope);
			dialogScope.put("dialog", dialogScope, dialogScope);
			dialogScope.setPrototype(documentScope);
		case ANONYME_SCOPE:
			anonymeScope = (ScriptableObject) context.newObject(dialogScope);
			anonymeScope.setPrototype(dialogScope);
			break;

		default:
			break;
		}
	}

	public Object evaluateFileScript(String fileName, int scope) throws IOException {
		File remoteFile = RemoteFileAccess.getRemoteFile(fileName);
		Context ctxt = Context.enter();
		return ctxt.evaluateReader(getScope(scope), new FileReader(remoteFile), remoteFile.getName(),
				1, null);
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
}
