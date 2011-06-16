package cudl.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.script.ScriptException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cudl.interpreter.InterpreterContext;
import cudl.interpreter.execption.InterpreterException;
import cudl.utils.Utils;


public class InterpreterEventHandler implements InterpreterListener {

	@Override
	public void doEvent(InterpreterEvent interpreterEvent)
			throws ScriptException, IOException {
		InterpreterContext context = (InterpreterContext) interpreterEvent
				.getSource();

		List<Node> catchList = searchEvent(interpreterEvent.type, context.field);
		removeUnlessCond(catchList);
		try {
			// FIXME: take the first with a correct event counter
			// a counter who is ≤ at currentCounter
			context.interpreter.execute(catchList.get(0));
		} catch (InterpreterException e) {
			context.executionHandler(e);
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
