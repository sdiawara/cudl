package fr.mbs.vxml.interpreter.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.script.ScriptException;

import org.omg.CORBA.CTX_RESTRICT_SCOPE;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.mbs.vxml.interpreter.InterpreterContext;
import fr.mbs.vxml.interpreter.execption.InterpreterException;
import fr.mbs.vxml.utils.Utils;

public class InterpreterEventHandler implements InterpreterListener {

	@Override
	public void doEvent(InterpreterEvent interpreterEvent)
			throws ScriptException, IOException {
		InterpreterContext context = (InterpreterContext) interpreterEvent
				.getSource();

		List<Node> catchList = searchEvent(interpreterEvent.type, context.field);
		// addRootHandler(interpreterEvent.type, catchList, context.field);
		removeUnlessCond(catchList);
		try {
			// FIXME: take the first with a correct event counter
			// a counter who is ≤ at currentCounter
			context.interpreter.execute(catchList.get(0));
		} catch (InterpreterException e) {
			context.executionHandler(e);
		}
	}

	private void addRootHandler(String event, List<Node> catchList, Node node) {
		Node item = node.getOwnerDocument().getElementsByTagName("vxml")
				.item(0);
		NamedNodeMap attributes = item.getAttributes();
		Node namedItem = null;
		if (null != attributes) {
			namedItem = attributes.getNamedItem("application");
		}
		if (null != namedItem) {
			System.err.println("---->" + searchEvent(event, item) + " " + item
					+ " " + event);
			catchList.addAll(searchEvent(event, node));
		}
	}

	private void removeUnlessCond(List<Node> catchList) {
		for (Iterator<Node> iterator = catchList.iterator(); iterator.hasNext();) {
			Node node = iterator.next();
			if (!Utils.checkCond(node)) {
				catchList.remove(node);
			}
		}
	}

	private List<Node> searchEvent(String eventName, Node parent) {
		ArrayList<Node> eventList = new ArrayList<Node>();
		while (parent != null) {
			NodeList nodeList = parent.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeName().equals(eventName)
						|| isCatchItemAndContainEvent(node, eventName)) {
					eventList.add(node);
				}
			}
			parent = parent.getParentNode();
		}

		return eventList;
	}

	private boolean isCatchItemAndContainEvent(Node node, String eventName) {
		return (node.getNodeName().equals("catch")
				&& (node.getAttributes().getLength() > 0) && (node
				.getAttributes().getNamedItem("event").getNodeValue()
				.contains(eventName)));
	}
}
