package cudl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cudl.event.InterpreterEvent;

class InterpreterEventHandler implements InterpreterListener {
	private Map<String, Integer> eventCounter = new Hashtable<String, Integer>();
	private final InterpreterContext context;

	InterpreterEventHandler(InterpreterContext context) {
		this.context = context;
	}

	@Override
	public void doEvent(InterpreterEvent interpreterEvent, Executor executor) throws IOException,
			SAXException, InterpreterException {

		String type = interpreterEvent.type;
		eventCounter.put(type, (eventCounter.get(type) == null) ? 1 : eventCounter.get(type) + 1);

		List<Node> catchList = searchEvent(type, context.getSelectedFormItem());
		if (catchList.size() == 0) {
			if (context.getRootDocument() != null)
				catchList = searchEvent(type, context.getRootDocument().getElementsByTagName("vxml")
						.item(0));
		}

		System.err.println("catchLinst =  " + catchList);
		// FIXME: take the first with a correct event counter
		// a counter who is ≤ at currentCounter
		// System.err.println(catchList);
		executor.execute(catchList.get(0));
	}

	private List<Node> searchEvent(String eventName, Node parent) {
		ArrayList<Node> eventList = new ArrayList<Node>();
		while (parent != null) {
			NodeList nodeList = parent.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeName().equals(eventName) || isCatchItemAndContainEvent(node, eventName)) {
					eventList.add(node);
				}
			}
			parent = parent.getParentNode();
		}

		return eventList;
	}

	private boolean isCatchItemAndContainEvent(Node node, String eventName) {
		NamedNodeMap attributes = node.getAttributes();
		return (node.getNodeName().equals("catch") && (attributes.getLength() > 0) && (attributes
				.getNamedItem("event").getNodeValue().contains(eventName)));
	}
}
