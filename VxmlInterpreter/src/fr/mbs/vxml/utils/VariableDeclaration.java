package fr.mbs.vxml.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class VariableDeclaration {
	private static final int ANONYME_SCOPE = 0;
	private static final int DIALOG_SCOPE = 1;
	private static final int DOCUMENT_SCOPE = 2;
	private static final int APPLICATION_SCOPE = 3;
	// private static final int SESSION_SCOPE = 4;
	private static final int NUMBER_OF_SCOPE = 4;
	private int anonymeNameCount = 0;

	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine = manager.getEngineByName("js");
	private ScriptContext anonyme = new SimpleScriptContext();
	private ScriptContext dialog = new SimpleScriptContext();
	private ScriptContext document = new SimpleScriptContext();
	private ScriptContext application = new SimpleScriptContext();

	public String declareVariable(Node node, int scope) throws DOMException {

		NamedNodeMap attributes = node.getAttributes();
		Node name = attributes.getNamedItem("name");
		String nodeDeclarationName = (name == null) ? node.getNodeName() + "_"
				+ anonymeNameCount++ : name.getNodeValue();

		Node expr = attributes.getNamedItem("expr");

		ScriptContext context = getContext(scope);
		Object value = null;
		int tmp = scope;
		do {
			try {

				value = (null == expr) ? "undefined"
						: (null == context) ? engine.eval(expr.getNodeValue())
								: engine.eval(expr.getNodeValue(), context);
				if (value != null) {
					System.err.println("name="+nodeDeclarationName+ " expr="+value);
					break;
				}
			} catch (ScriptException e) {
			}
		} while (++tmp < NUMBER_OF_SCOPE && (context = getContext(tmp)) != null);

		if (null == context) {
			engine.put(nodeDeclarationName, value);
		} else {
			context.getBindings(ScriptContext.ENGINE_SCOPE).put(
					nodeDeclarationName, value );
		}

		return nodeDeclarationName;
	}

	public String evaluateScript(Node node, int scope)
			throws FileNotFoundException, DOMException, ScriptException {

		ScriptContext context = getContext(scope);
		String value = "";
		if (node.getAttributes() != null
				&& node.getAttributes().getLength() > 0) {
			value = evaluateScriptWithAttribute(node, scope);
		} else {
			if (context == null) {
				value = engine.eval(node.getTextContent()) + "";
			} else {
				int tmp = scope;
				do {
					try {
						value = engine.eval(node.getTextContent(), context)
								+ "";
						if (value != "")
							break;
					} catch (ScriptException e) {
					}
				} while (++tmp < NUMBER_OF_SCOPE
						&& (context = getContext(tmp)) != null);
			}
		}

		return value;
	}

	private String evaluateScriptWithAttribute(Node node, int scope)
			throws ScriptException, FileNotFoundException {
		ScriptContext context = getContext(scope);
		Node namedItem = node.getAttributes().getNamedItem("src");
		String value = "";
		if (null != namedItem) {
			if (context == null) {
				value = engine.eval(new FileReader(namedItem.getNodeValue()))
						.toString();
			} else {

				value = engine.eval(new FileReader(namedItem.getNodeValue()),
						context).toString();
			}
		} else {
			namedItem = node.getAttributes().getNamedItem("expr");
			if (context == null) {
				value = engine.eval(namedItem.getNodeValue()).toString();
			} else {
				int tmp = scope;
				do {
					try {
						value = engine.eval(namedItem.getNodeValue(), context)
								.toString();
						if (value != "") {
							break;
						}
					} catch (ScriptException e) {
					}
				} while (++tmp < NUMBER_OF_SCOPE
						&& (context = getContext(tmp)) != null);
				if ("".equals(value))
					throw new IllegalArgumentException();
			}

		}

		return value;
	}

	public String getValue(String declareVariable, int scope) {
		ScriptContext context;
		int tmp = scope - 1;
		while (++tmp < NUMBER_OF_SCOPE && (context = getContext(tmp)) != null) {
			Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
			if (bindings.containsKey(declareVariable)) {
				return bindings.get(declareVariable) + "";
			}
		}

		if (engine.getBindings(ScriptContext.ENGINE_SCOPE).containsKey(
				declareVariable))
			return engine.get(declareVariable) + "";

		throw new IllegalArgumentException(declareVariable
				+ " is not derclared in this scope " + tmp);
	}

	public void resetScope(int scope) {
		getContext(scope).setBindings(new SimpleBindings(),
				ScriptContext.ENGINE_SCOPE);
	}

	public void setValue(String declareVariable, String expr, int scope)
			throws ScriptException {
		int tmp = scope - 1;
		ScriptContext context;
		while (++tmp < NUMBER_OF_SCOPE && (context = getContext(tmp)) != null) {
			Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
			if (bindings.containsKey(declareVariable)) {
				bindings.put(declareVariable, engine.eval(expr));
				return;
			}
		}
		engine.put(declareVariable, engine.eval(expr));
	}

	private ScriptContext getContext(int scope) {
		switch (scope) {
		case ANONYME_SCOPE:
			return anonyme;
		case DIALOG_SCOPE:
			return dialog;
		case DOCUMENT_SCOPE:
			return document;
		case APPLICATION_SCOPE:
			return application;
		default:
			return null;
		}
	}
}
