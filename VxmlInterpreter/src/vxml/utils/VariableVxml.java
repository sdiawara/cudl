package vxml.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class VariableVxml {
	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine = manager.getEngineByName("js");
	private int anonymeNameCount = 0;

	public String declareVariable(Node node) throws DOMException,
			ScriptException {
		NamedNodeMap attributes = node.getAttributes();
		Node namedItem = attributes.getNamedItem("name");
		String name = (namedItem == null) ? node.getNodeName() + "_"
				+ anonymeNameCount++ : (namedItem == null) ? node
				.getParentNode().getNodeName()
				+ anonymeNameCount : namedItem.getNodeValue();
		Node expr = attributes.getNamedItem("expr");

		engine.put(name, (expr == null) ? "undefined" : engine.eval(expr
				.getNodeValue()));

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

	public String getValue(String declareVariable) {
		return engine.get(declareVariable).toString();
	}

	public void setValue(String declareVariable, String expr)
			throws ScriptException {
		engine.put(declareVariable, engine.eval(expr));
	}
}
