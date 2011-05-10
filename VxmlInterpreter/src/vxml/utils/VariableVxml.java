package vxml.utils;

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
	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine = manager.getEngineByName("js");

	private int anonymeNameCount = 0;
	private ScriptContext context = new SimpleScriptContext();

	public String declareVariable(Node node) throws DOMException,
			ScriptException {

		String name = null;

		NamedNodeMap attributes = node.getAttributes();
		Node namedItem = attributes.getNamedItem("name");
		name = (namedItem == null) ? node.getNodeName() + "_"
				+ anonymeNameCount++ : namedItem.getNodeValue();
		Node expr = attributes.getNamedItem("expr");

		if (hasAnAnonymeContext(node)) {
			Bindings anonymeScope = context
					.getBindings(ScriptContext.ENGINE_SCOPE);
			anonymeScope.put(name, (expr == null) ? "undefined" : engine
					.eval(expr.getNodeValue()));
		} else {
			engine.put(name, (expr == null) ? "undefined" : engine.eval(expr
					.getNodeValue()));
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
			value = engine.eval(node.getTextContent()).toString();
		}
		return value;
	}

	public void resetScope(Node node) {
		context.setBindings(new SimpleBindings(), ScriptContext.ENGINE_SCOPE);
	}

	public String getValue(String declareVariable) {
		if (context.getBindings(ScriptContext.ENGINE_SCOPE).containsKey(
				declareVariable)) {
			return context.getBindings(ScriptContext.ENGINE_SCOPE).get(
					declareVariable)
					+ "";
		} else if (engine.getBindings(ScriptContext.ENGINE_SCOPE).containsKey(
				declareVariable))
			return engine.get(declareVariable) + "";

		throw new IllegalArgumentException(declareVariable
				+ " is not derclared in this scope");
	}

	public void setValue(String declareVariable, String expr)
			throws ScriptException {
		engine.put(declareVariable, engine.eval(expr));
	}

	private boolean hasAnAnonymeContext(Node node) {
		return VxmlElementType.isFormItem(node.getParentNode());
	}
}
