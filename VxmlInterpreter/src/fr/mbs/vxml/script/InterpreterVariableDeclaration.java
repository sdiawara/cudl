package fr.mbs.vxml.script;

import java.util.Hashtable;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fr.mbs.vxml.utils.VxmlElementType;

public final class InterpreterVariableDeclaration {
	private Map<Node, String> formItemName;
	private int anonymeNameCount = 0;
	private ScriptEngineManager manager;
	private ScriptEngine engine;

	public InterpreterVariableDeclaration() {
		manager = new ScriptEngineManager();
		engine = manager.getEngineByName("ecmascript");
		engine.setContext(new DefaultInterpreterScriptContext());
		formItemName = new Hashtable<Node, String>();
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

		engine.getContext().getBindings(getNodeScope(formItem)).put(nodeName,
				engine.eval(nodeValue));

		formItemName.put(formItem, nodeName);
	}

	public void declareVariable(Node node) throws ScriptException {
		if (!node.getNodeName().equals("var"))
			throw new IllegalArgumentException(
					"This node should be an vxml var node");
		NamedNodeMap attributes = node.getAttributes();
		String nodeName = attributes.getNamedItem("name").getTextContent();
		Node value = attributes.getNamedItem("expr");
		String nodeValue = (null == value) ? "undefined" : value
				.getTextContent();

		engine.getContext().getBindings(getNodeScope(node)).put(nodeName,
				engine.eval(nodeValue));
	}

	public Object evaluateScript(Node script) throws ScriptException {
		Object val = null;
		NamedNodeMap attributes = script.getAttributes();
		if (script.getNodeName().equals("script")) {
			if (null != attributes && attributes.getNamedItem("src") != null) {
				val = engine.eval(attributes.getNamedItem("src")
						.getTextContent(), engine.getContext().getBindings(
						getNodeScope(script)))
						+ "";
			} else {
				// System.err.println("=======>" + (script.getTextContent()));
				val = engine.eval(script.getTextContent()) + "";
			}
		} else if (script.getNodeName().equals("value")) {
			val = engine.eval(attributes.getNamedItem("expr").getTextContent())
					+ "";
		} else
			val = engine.eval(script.getTextContent()) + "";
		System.err.println("node "+script+ "val ="+val);
		return val;
	}
	
	private int getNodeScope(Node node) {
		if (VxmlElementType.isFormItem(node))
			return ScriptContext.ENGINE_SCOPE;
		
		Node tmp = VxmlElementType.isConditionalItem(node) ? node
				.getParentNode() : node;
		tmp = null == tmp ? node : tmp;

		if (isAnAnonymeContext(tmp))
			return InterpreterScriptContext.ANONYME_SCOPE;
		else if (isAnDialogContext(tmp))
			return InterpreterScriptContext.DIALOG_SCOPE;
		else if (node.getParentNode().getNodeName().equals("vxml"))
			return InterpreterScriptContext.DOCUMENT_SCOPE;

		return InterpreterScriptContext.APPLICATION_SCOPE;
	}

	private boolean isAnAnonymeContext(Node node) {
		return VxmlElementType.isFormItem(node.getParentNode());
	}

	private boolean isAnDialogContext(Node node) {
		return VxmlElementType.isADialog(node.getParentNode());
	}

	public void setValue(Node node, String value) throws ScriptException {
		NamedNodeMap attributes = node.getAttributes();

		// System.err.println("setNODE " + node);
		Node namedItem;
		namedItem = (null == attributes) ? null : attributes
				.getNamedItem("name");

		if (namedItem == null) {

			engine.eval(formItemName.get(node) + " = " + value + ";");
		} else {
			// System.err.println(" " + namedItem.getNodeValue() + " = " +
			// value);
			engine.eval(namedItem.getNodeValue() + " = " + value + ";");
		}
	}

	public Object getValue(Node selectedItem) throws DOMException,
			ScriptException {
		NamedNodeMap attributes = selectedItem.getAttributes();
		Node namedItem = (null == attributes) ? null : attributes
				.getNamedItem("name");
		String name = (null == namedItem) ? formItemName.get(selectedItem)
				: namedItem.getNodeValue();

		return getValue(name);
	}

	public Object getValue(String name) throws ScriptException {
		Object eval = engine.eval(name);
		return (null == eval) ? "undefined" : eval;
	}

	public void resetScopeBinding(int scope) {
		engine.getContext().getBindings(scope).clear();
	}
}
