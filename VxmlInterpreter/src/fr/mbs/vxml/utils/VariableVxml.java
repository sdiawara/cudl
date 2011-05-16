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

public final class VariableVxml {
	private static final int ANONYME_SCOPE = 0;
	private static final int DIALOG_SCOPE = 1;
	private static final int DOCUMENT_SCOPE = 2;
	private static final int APPLICATION_SCOPE = 3;
	private static final int SESSION_SCOPE = 4;
	private static final int NUMBER_OF_SCOPE = 3;

	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine = manager.getEngineByName("js");

	private int anonymeNameCount = 0;
	private ScriptContext anonyme = new SimpleScriptContext();
	private ScriptContext dialog = new SimpleScriptContext();
	private ScriptContext document = new SimpleScriptContext();

	public String declareVariable(Node node, int scope) throws DOMException,
			ScriptException {

		String name = null;
		NamedNodeMap attributes = node.getAttributes();
		Node namedItem = attributes.getNamedItem("name");
		name = (namedItem == null) ? node.getNodeName() + "_"
				+ anonymeNameCount++ : namedItem.getNodeValue();
		Node expr = attributes.getNamedItem("expr");
		Bindings scopeBindings;
		switch (scope) {
		case ANONYME_SCOPE:
			scopeBindings = anonyme.getBindings(ScriptContext.ENGINE_SCOPE);
			scopeBindings.put(name, (expr == null) ? "undefined" : engine
					.eval(expr.getNodeValue()));
			break;
		case DIALOG_SCOPE:
			scopeBindings = dialog.getBindings(ScriptContext.ENGINE_SCOPE);
			scopeBindings.put(name, (expr == null) ? "undefined" : engine
					.eval(expr.getNodeValue()));
			break;
		case DOCUMENT_SCOPE:
			scopeBindings = document.getBindings(ScriptContext.ENGINE_SCOPE);
			scopeBindings.put(name, (expr == null) ? "undefined" : engine
					.eval(expr.getNodeValue()));
			break;
		case APPLICATION_SCOPE:
		case SESSION_SCOPE:
		default:
			engine.put(name, (expr == null) ? "undefined" : engine.eval(expr
					.getNodeValue()));
			break;
		}

		return name;
	}

	public String evaluateScript(Node node) throws FileNotFoundException,
			DOMException, ScriptException {
		assert (node.getNodeName().equals("script"));
		String value = "";
		if (node.getAttributes() != null
				&& node.getAttributes().getLength() > 0) {

			if (node.getAttributes().getNamedItem("src") != null) {
				value = engine.eval(
						new FileReader(node.getAttributes().getNamedItem("src")
								.getNodeValue())).toString();
			}
		} else {
			value = engine.eval(node.getTextContent()) + "";
		}
		return value;
	}

	public String getValue(String declareVariable, int scope) {
		ScriptContext context;
		int tmp = scope - 1;
		while (++tmp < NUMBER_OF_SCOPE && (context = getContext(tmp)) != null) {
			if (context.getBindings(ScriptContext.ENGINE_SCOPE).containsKey(
					declareVariable)) {
				return context.getBindings(ScriptContext.ENGINE_SCOPE).get(
						declareVariable)
						+ "";
			}
		}

		if (engine.getBindings(ScriptContext.ENGINE_SCOPE).containsKey(
				declareVariable))
			return engine.get(declareVariable) + "";
		throw new IllegalArgumentException(declareVariable
				+ " is not derclared in this scope " + scope);
	}

	private ScriptContext getContext(int scope) {
		switch (scope) {
		case ANONYME_SCOPE:
			return anonyme;
		case DIALOG_SCOPE:
			return dialog;
		case DOCUMENT_SCOPE:
			return document;
		default:
			return null;
		}
	}

	public void resetScope(int scope) {
		switch (scope) {
		case ANONYME_SCOPE:
			anonyme.setBindings(new SimpleBindings(),
					ScriptContext.ENGINE_SCOPE);
			break;
		case DIALOG_SCOPE:
			dialog
					.setBindings(new SimpleBindings(),
							ScriptContext.ENGINE_SCOPE);
			break;
		case DOCUMENT_SCOPE:
			document.setBindings(new SimpleBindings(),
					ScriptContext.ENGINE_SCOPE);
			break;
		default:

		}
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
}
