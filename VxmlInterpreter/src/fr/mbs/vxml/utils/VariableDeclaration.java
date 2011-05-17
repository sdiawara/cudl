package fr.mbs.vxml.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;

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
	private static final int SESSION_SCOPE = 4;
	private static final int NUMBER_OF_SCOPE = 3;
	private int anonymeNameCount = 0;

	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine = manager.getEngineByName("js");
	private ScriptContext anonyme = new SimpleScriptContext();
	private ScriptContext dialog = new SimpleScriptContext();
	private ScriptContext document = new SimpleScriptContext();
	private ScriptContext application = new SimpleScriptContext();

	public String declareVariable(Node node, int scope) throws DOMException,
			ScriptException {

		NamedNodeMap attributes = node.getAttributes();
		Node name = attributes.getNamedItem("name");
		String nodeDeclarationName = (name == null) ? node.getNodeName() + "_"
				+ anonymeNameCount++ : name.getNodeValue();

		Node expr = attributes.getNamedItem("expr");
		Object value = (null == expr) ? "undefined" : engine.eval(expr
				.getNodeValue());

		ScriptContext context = getContext(scope);
		System.err.println(scope + " " + name + " " + context);
		if (null == context) {
			engine.put(nodeDeclarationName, value);
			System.err.println(engine.getBindings(ScriptContext.ENGINE_SCOPE)
					.containsKey(nodeDeclarationName));
		} else {
			context.getBindings(ScriptContext.ENGINE_SCOPE).put(
					nodeDeclarationName, value + "");
		}

		return nodeDeclarationName;
	}

	public String evaluateScript(Node node, int scope)
			throws FileNotFoundException, DOMException, ScriptException {

		// ScriptContext context = getContext(scope);
		//
		// Object value = "";
		// NamedNodeMap attributes = node.getAttributes();
		// Node src;
		// if (null != attributes && attributes.getLength() > 0
		// && (null != (src = attributes.getNamedItem("src")))) {
		// value = engine.eval(new FileReader(src.getNodeValue()));
		// } else {
		// value = engine.eval(node.getTextContent()) + "";
		// }
		//		 
		// return value+"";

		ScriptContext context = getContext(scope);
		System.err.println("scope " + scope + " " + context);
		String value = "";
		if (node.getAttributes() != null
				&& node.getAttributes().getLength() > 0) {

			if (node.getAttributes().getNamedItem("src") != null) {
				if (context == null) {
					value = engine.eval(
							new FileReader(node.getAttributes().getNamedItem(
									"src").getNodeValue())).toString();
				} else {

					value = engine.eval(
							new FileReader(node.getAttributes().getNamedItem(
									"src").getNodeValue()), context).toString();
				}
			}
		} else {
			if (context == null) {
				value = engine.eval(node.getTextContent()) + "";
			} else
				value = engine.eval(node.getTextContent(), context) + "";
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
				+ " is not derclared in this scope " + scope);
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
