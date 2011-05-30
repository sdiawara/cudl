package fr.mbs.vxml.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fr.mbs.vxml.utils.RemoteFileAccess;

public final class InterpreterVariableDeclaration {
	private Map<Node, String> dialogItemName;
	private int anonymeNameCount = 0;
	private ScriptEngineManager manager;
	private ScriptEngine engine;
	private InterpreterScriptContext context;
	private String location;

	public InterpreterVariableDeclaration() {
		manager = new ScriptEngineManager();
		engine = manager.getEngineByName("ecmascript");
		context = new DefaultInterpreterScriptContext();
		dialogItemName = new Hashtable<Node, String>();
		addVariableNormalized();
	}

	private void addVariableNormalized() {
		try {
			engine.eval("session = new Object();", context);
			engine.eval("application = new Object();", context);
			engine.eval("document = new Object();", context);
			engine.eval("dialog = new Object();", context);
		} catch (ScriptException e) {
			throw new RuntimeException("Vxml interpreter internal error");
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

		context.getBindings(DefaultInterpreterScriptContext.DOCUMENT_SCOPE)
				.put(nodeName, engine.eval(nodeValue, context));

		engine.eval(getNormalizationScript(nodeName,
				DefaultInterpreterScriptContext.DOCUMENT_SCOPE), context);

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
		// System.err.println(nodeValue);
		context.getBindings(scope).put(nodeName,
				engine.eval(nodeValue, context));
		engine.eval(getNormalizationScript(nodeName, scope), context);
	}

	public void declareVariable(File file, int scope)
			throws FileNotFoundException, ScriptException {
		engine.eval(new FileReader(file), context.getBindings(scope));
	}

	public Object evaluateScript(Node script, int scope) throws DOMException,
			ScriptException, IOException {
		Object val = null;
		NamedNodeMap attributes = script.getAttributes();
		if (script.getNodeName().equals("script")) {
			if (null != attributes && attributes.getNamedItem("src") != null) {
				// System.err.println(attributes.getNamedItem("src")
				// .getTextContent());
				File remoteFile = RemoteFileAccess.getRemoteFile(
						location + "/", attributes.getNamedItem("src")
								.getTextContent());
				val = engine.eval(new FileReader(remoteFile), context
						.getBindings(scope));
				// System.err.println("path " + remoteFile.getCanonicalPath());
			} else {
				val = engine.eval(script.getTextContent(), context
						.getBindings(scope));
			
				//engine.eval(getNormalizationScript(script.getTextContent()+"", scope), context);
			}
		} else if (script.getNodeName().equals("value")) {
			val = engine.eval(attributes.getNamedItem("expr").getTextContent(),
					context);
			// System.err.println("value ("
			// + attributes.getNamedItem("expr").getTextContent() + ")= "
			// + val);
		} else {
			val = engine.eval(attributes.getNamedItem("cond").getTextContent(),
					context)
					+ "";
			// System.err.println("cond ("
			// + attributes.getNamedItem("cond").getTextContent() + ")= "
			// + val);
		}

		return val;
	}

	public void setValue(Node node, String value, int scope)
			throws ScriptException {
		NamedNodeMap attributes = node.getAttributes();
		Node namedItem;
		namedItem = (null == attributes) ? null : attributes
				.getNamedItem("name");

		if (namedItem == null) {
			context.getBindings(scope).put(dialogItemName.get(node),
					engine.eval(value, context));
		} else {
			context.getBindings(scope).put(namedItem.getNodeValue(),
					engine.eval(value, context));
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
		Object eval = engine.eval(name, context);
		// System.err.println("get(" + name + ")= " + eval);
		return (null == eval) ? "undefined" : eval;
	}

	public void resetScopeBinding(int scope) {
		context.getBindings(scope).clear();
	}

	public void setLocation(String substring) {
		this.location = substring;
	}

	private String getNormalizationScript(String name, int scope) {
		String scopeName;
		switch (scope) {
		case DefaultInterpreterScriptContext.APPLICATION_SCOPE:
			scopeName = "application";
			break;
		case DefaultInterpreterScriptContext.DOCUMENT_SCOPE:
			scopeName = "document";
			break;
		case DefaultInterpreterScriptContext.DIALOG_SCOPE:
			scopeName = "dialog";
			break;
		case DefaultInterpreterScriptContext.SESSION_SCOPE:
			scopeName = "session";
			break;
		default:
			scopeName = "";
		}
		
		return "".equals(scopeName) ? "" : scopeName + "." + name + "=" + name;
	}
}
