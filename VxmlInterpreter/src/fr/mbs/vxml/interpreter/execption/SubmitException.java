package fr.mbs.vxml.interpreter.execption;

import javax.script.ScriptException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fr.mbs.vxml.script.InterpreterVariableDeclaration;

public class SubmitException extends InterpreterException {
	public String next;

	public SubmitException(Node node, InterpreterVariableDeclaration declaration)
			throws ScriptException {
		NamedNodeMap attributes = node.getAttributes();
		this.next = attributes.getNamedItem("next").getNodeValue();

		Node namedItem = attributes.getNamedItem("namelist");
		String[] namelist = namedItem != null ? namedItem.getNodeValue().split(
				" ") : new String[0];
		String urlSuite = "?";
		for (int i = 0; i < namelist.length; i++) {
			String declareVariable = namelist[i];

			urlSuite += declareVariable + "="
					+ declaration.getValue(declareVariable) + "&";

		}
		this.next += urlSuite;
	}
}
