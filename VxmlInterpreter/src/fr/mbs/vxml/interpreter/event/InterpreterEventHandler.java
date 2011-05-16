package fr.mbs.vxml.interpreter.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.mbs.vxml.interpreter.InterpreterContext;
import fr.mbs.vxml.interpreter.execption.InterpreterException;
import fr.mbs.vxml.utils.Utils;


public class InterpreterEventHandler implements InterpreterListener {

	public void noInput(InterpreterEvent interpreterEvent) {
		handle(interpreterEvent, "noinput");
	}

	@Override
	public void NoMatch(InterpreterEvent interpreterEvent) {
		handle(interpreterEvent, "nomatch");
	}

	@Override
	public void error(InterpreterEvent interpreterEvent) {
		handle(interpreterEvent, "error");
	}

	@Override
	public void help(InterpreterEvent interpreterEvent) {
		handle(interpreterEvent, "help");
	}

	private void handle(EventObject interpreterEvent, String eventName) {
		InterpreterContext context = (InterpreterContext) interpreterEvent
				.getSource();

		List<Node> catchList = searchEvent(eventName,
				context.interpreter.selectedItem);
		removeUnlessCond(catchList);
		try {
			// FIXME: take the first with a correct event counter
			// a counter who is ≤ at currentCounter
			context.interpreter.execute(catchList.get(0));
		} catch (InterpreterException e) {

			try {
				context.executionHandler(e);
			} catch (SAXException e1) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e1);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e1);
			}
		}
	}

	private void removeUnlessCond(List<Node> catchList) {
		for (Iterator<Node> iterator = catchList.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
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
