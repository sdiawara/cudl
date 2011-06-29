package cudl.script;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import cudl.utils.RemoteFileAccess;
import cudl.utils.SessionFileCreator;

public class InterpreterVariableDeclaration {
	private Map<Node, String> dialogItemName;
	private int anonymeNameCount = 0;
	private ScriptEngineManager manager;
	private ScriptEngine engine;
	private InterpreterScriptContext context;
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
		context = new DefaultInterpreterScriptContext();
		dialogItemName = new Hashtable<Node, String>();
		addVariableNormalized();
		location = scriptLocation;
	}

	private void addVariableNormalized() throws IOException, ScriptException {
		try {
			declarareScope(InterpreterScriptContext.SESSION_SCOPE);
			File sessionFile = new SessionFileCreator().get3900DefaultSession();
			if (null != sessionFile) {
				engine.eval(new FileReader(sessionFile),
						getBindings(InterpreterScriptContext.SESSION_SCOPE));
				sessionFile.delete();
			}

			declarareScope(InterpreterScriptContext.APPLICATION_SCOPE);

			declarareNormalizeApplication();

			declarareScope(InterpreterScriptContext.DOCUMENT_SCOPE);
			declarareScope(InterpreterScriptContext.DIALOG_SCOPE);
		} catch (ScriptException e) {
			throw new ScriptException("Vxml interpreter internal error "
					+ e.toString());
		}
	}

	private void declarareNormalizeApplication() throws ScriptException {
		for (Iterator<String> appliVar = normalizedApplicationVariable
				.iterator(); appliVar.hasNext();) {
			String script = (String) appliVar.next();
			engine.eval(script,
					getBindings(InterpreterScriptContext.APPLICATION_SCOPE));
		}
	}

	public void declareDialogItem(Node formItem) throws ScriptException {
		NamedNodeMap attributes = formItem.getAttributes();
		Node name = null;
		Node value = null;
		if (attributes.getLength() > 0) {
			name = attributes.getNamedItem("name");
			value = attributes.getNamedItem("expr");
		}
		String nodeName = (null == name) ? formItem.getNodeName() + "_"
				+ anonymeNameCount++ : name.getNodeValue();
		String nodeValue = (null == value) ? "undefined" : value
				.getTextContent();

		Bindings bindings = getBindings(InterpreterScriptContext.DOCUMENT_SCOPE);
		bindings.put(nodeName, engine.eval(nodeValue, bindings));

		dialogItemName.put(formItem, nodeName);
	}

	public void declareVariable(Node node, int scope) throws ScriptException {
		if (!node.getNodeName().equals("var"))
			throw new IllegalArgumentException(
					"This node should be an vxml var node");
		NamedNodeMap attributes = node.getAttributes();
		String nodeName = attributes.getNamedItem("name").getTextContent();
		Node value = attributes.getNamedItem("expr");
		String nodeValue = (null == value) ? "undefined" : value
				.getTextContent();

		System.err.println(nodeName + "   =" + nodeValue + scope);
		getBindings(scope).put(nodeName,
				engine.eval(getReplaceBindingName(nodeValue), context));
	}

	public Object evaluateScript(Node script, int scope) throws DOMException,
			ScriptException, IOException {
		Object val = null;
		NamedNodeMap attributes = script.getAttributes();
		if (script.getNodeName().equals("script")) {
			if (null != attributes && attributes.getNamedItem("src") != null) {
				File remoteFile = RemoteFileAccess.getRemoteFile(location,
						attributes.getNamedItem("src").getTextContent());
				val = engine.eval(new FileReader(remoteFile), context);
			} else {
				// FIXME: evaluate in scope
				val = engine
						.eval(getReplaceBindingName(script.getTextContent()),
								context);
			}
		} else if (script.getNodeName().equals("value")) {
			val = engine.eval(getReplaceBindingName(attributes.getNamedItem(
					"expr").getTextContent()), context);
		} 

		return val;
	}

	public Object evaluateScript(String script, int scope)
			throws ScriptException {
		return engine.eval(getReplaceBindingName(script), context)+"";
	}

	public void setValue(Node node, String value, int scope)
			throws ScriptException {

		NamedNodeMap attributes = node.getAttributes();
		Node namedItem;
		namedItem = (null == attributes) ? null : attributes
				.getNamedItem("name");

		if (namedItem == null) {
			getBindings(scope).put(dialogItemName.get(node),
					engine.eval(value, context));
		} else {
			engine.eval(getReplaceBindingName(namedItem.getNodeValue() + " = "
					+ value), context);
		}
	}

	public Object getValue(Node selectedItem) throws DOMException,
			ScriptException {
		NamedNodeMap attributes = selectedItem.getAttributes();
		Node namedItem = (null == attributes) ? null : attributes
				.getNamedItem("name");
		String name = (null == namedItem) ? dialogItemName.get(selectedItem)
				: namedItem.getNodeValue();

		return getValue(name);
	}

	public Object getValue(String name) throws ScriptException {
		Object eval = engine.eval(getReplaceBindingName(name), context);
		return (null == eval) ? "undefined" : eval;
	}

	public void resetScopeBinding(int scope) {
		System.err.println("clear scope " + getScopeName(scope) + " scope="
				+ scope);
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

	public void declareVariable(String name, String value, int scope)
			throws ScriptException {
		getBindings(scope).put(name,
				engine.eval(getReplaceBindingName(value), context));
	}

	public Object evaluateFileScript(String fileName, int scope)
			throws ScriptException, IOException {
		File remoteFile = RemoteFileAccess.getRemoteFile(fileName);
		return engine.eval(new FileReader(remoteFile), context);
	}

	public void setValue(String name, String value, int scope) {
		getBindings(scope).put(name, value);
	}
}
