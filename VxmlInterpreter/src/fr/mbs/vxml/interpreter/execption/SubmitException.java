package fr.mbs.vxml.interpreter.execption;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fr.mbs.vxml.utils.VariableVxml;


public class SubmitException extends InterpreterException {
	public String next;
	
	public SubmitException(Node node, VariableVxml variableVxml) {
		NamedNodeMap attributes = node.getAttributes();
		this.next = attributes.getNamedItem("next").getNodeValue();
		System.err.println("->>"+next);
		Node namedItem = attributes.getNamedItem("namelist");
		String[] namelist = namedItem != null ? namedItem.getNodeValue().split(
				" ") : new String[0];
		String urlSuite = "?";
		for (int i = 0; i < namelist.length; i++) {
			String declareVariable = namelist[i];

			urlSuite += declareVariable + "="
					+ variableVxml.getValue(declareVariable) + "&";
			
		}
		
		this.next += urlSuite;
		System.err.println(next);
	}
}
