package cudl;

import static cudl.utils.Utils.getNodeAttributeValue;
import static cudl.utils.Utils.serachItem;

import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cudl.script.InterpreterVariableDeclaration;

class InternalInterpreter {
	public static final int START = 1;
	public static final int NOINPUT = 2;
	public static final int NOMATCH = 3;
	public static final int EVENT = 4;
	public static final int BLIND_TRANSFER_SUCCESSSS = 5;
	public static final int CONNECTION_DISCONNECT_HANGUP = 6;
	public static final int NOANSWER = 7;
	public static final int CALLER_HUNGUP_DURING_TRANSFER = 8;
	public static final int NETWORK_BUSY = 9;
	public static final int DESTINATION_BUSY = 10;
	public static final int MAX_TIME_DISCONNECT = 11;
	public static final int DESTINATION_HANGUP = 12;
	public static final int TALK = 13;
	public static final int DTMF = 14;

	private final InterpreterContext context;
	private InterpreterEventHandler ieh;
	private Properties properties = new Properties();
	private boolean test;
	private String eventType;

	InternalInterpreter(InterpreterContext context) throws IOException, SAXException {
		this.context = context;
		ieh = new InterpreterEventHandler(context);
	}

	public void interpret(int action, String arg) throws IOException, SAXException,
			ParserConfigurationException {
		Node node = context.getCurrentDialog();
		try {
			VxmlTag dialog;
			switch (action) {
			case START:
				System.err.println(node);
				dialog = TagInterpreterFactory.getTagInterpreter(node);
				if (test) {
					((FormTag) dialog).setInitVar(false);
				} else
					context.getFormItemNames().clear();
				dialog.interpret(context);
				break;
			// FIXME : remove duplicate code
			case NOINPUT:
				ieh.doEvent("noinput");
				break;
			case NOMATCH:
				ieh.doEvent("nomatch");
			case EVENT:
				ieh.doEvent(eventType);
				break;
			case BLIND_TRANSFER_SUCCESSSS:
				context.getDeclaration().evaluateScript(
						"connection.protocol.isdnvn6.transferresult= '0'",
						InterpreterVariableDeclaration.SESSION_SCOPE);
				ieh.doEvent("connection.disconnect.transfer");
				break;
			case CONNECTION_DISCONNECT_HANGUP:
				ieh.doEvent("connection.disconnect.hangup");
				break;
			case NOANSWER:
				context.getDeclaration().evaluateScript(
						"connection.protocol.isdnvn6.transferresult= '2'",
						InterpreterVariableDeclaration.SESSION_SCOPE);
				setTransferResultAndExecute("'noanswer'");
				break;

			case CALLER_HUNGUP_DURING_TRANSFER:
				setTransferResultAndExecute("'near_end_disconnect'");
				break;
			case NETWORK_BUSY:
				context.getDeclaration().evaluateScript(
						"connection.protocol.isdnvn6.transferresult= '5'",
						InterpreterVariableDeclaration.SESSION_SCOPE);
				setTransferResultAndExecute("'network_busy'");
				break;
			case DESTINATION_BUSY:
				setTransferResultAndExecute("'busy'");
				break;
			case MAX_TIME_DISCONNECT:
				setTransferResultAndExecute("'maxtime_disconnect'");
				break;
			case DESTINATION_HANGUP:
				context.getDeclaration().setValue(
						context.getFormItemNames().get(context.getSelectedFormItem()),
						"'far_end_disconnect'", InterpreterVariableDeclaration.DIALOG_SCOPE);
				test = true;
				interpret(1, null);
				break;
			case TALK:
				utterance(arg, "'voice'");
				break;
			case DTMF:
				utterance(arg, "'dtmf'");
				break;
			}
		} catch (GotoException e) {
			ieh.resetEventCounter();
			context.buildDocument(e.next);
			node = context.getCurrentDialog();
			interpret(1, null);
		} catch (SubmitException e) {
			ieh.resetEventCounter();
			context.buildDocument(e.next);
			node = context.getCurrentDialog();
			interpret(1, null);
		} catch (FilledException e) {
		} catch (EventException e) {
			eventType = e.type;
			interpret(EVENT, null);
		} catch (TransferException e) {
			ieh.resetEventCounter();
		} catch (ReturnException e) {
			context.setReturnValue(e.namelist);
		} catch (InterpreterException e) {
			System.err.println(e);
		}
	}

	private void setTransferResultAndExecute(String transferResult) throws InterpreterException,
			IOException, SAXException, ParserConfigurationException {
		context.getDeclaration().setValue(
				context.getFormItemNames().get(context.getSelectedFormItem()), transferResult,
				InterpreterVariableDeclaration.DIALOG_SCOPE);
		FilledTag filled = (FilledTag) TagInterpreterFactory.getTagInterpreter(serachItem(context
				.getSelectedFormItem(), "filled"));
		filled.setExecute(true);
		filled.interpret(context); // FIXME raise disconnect event instead to be
		// closer to OMS buggy interpretation.
	}

	private void executionHandler(InterpreterException e) throws IOException, SAXException,
			ParserConfigurationException {

		if (e instanceof GotoException) {
			context.buildDocument(((GotoException) e).next);
			interpret(1, null);
		} else if (e instanceof SubmitException) {
			context.buildDocument(((SubmitException) e).next);
			interpret(1, null);
		}
		if (e instanceof EventException) {
			try {
				ieh.doEvent(((EventException) e).type);
			} catch (InterpreterException e1) {
				executionHandler(e1);
			}
		}
	}

	private void collectDialogProperty(NodeList nodeList) {
		properties.clear();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeName().equals("property")) {
				collectProperty(node);
			}
		}
	}

	private void collectProperty(Node node) {
		properties.put(getNodeAttributeValue(node, "name"), getNodeAttributeValue(node, "value"));
	}

	void utterance(String utterance, String utteranceType) throws IOException, SAXException,
			ParserConfigurationException, InterpreterException {
		Node currentDialog = context.getCurrentDialog();
		if (currentDialog.getNodeName().equals("form")) {
			context.getDeclaration().evaluateScript(
					"application.lastresult$[0].utterance =" + utterance,
					InterpreterVariableDeclaration.APPLICATION_SCOPE);
			context.getDeclaration().evaluateScript(
					"application.lastresult$[0].inputmode =" + utteranceType,
					InterpreterVariableDeclaration.APPLICATION_SCOPE);
			context.getDeclaration().setValue(
					context.getFormItemNames().get(context.getSelectedFormItem()), utterance, 60);
			FilledTag filled = (FilledTag) TagInterpreterFactory.getTagInterpreter(serachItem(context
					.getSelectedFormItem(), "filled"));
			filled.setExecute(true);
			filled.interpret(context);
		} else {
			NodeList childNodes = currentDialog.getChildNodes();
			int choiceCount = 0;
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node child = childNodes.item(i);
				if (child.getNodeName().equals("choice")) {
					choiceCount++;
					try {
						if (choiceCount == Integer.parseInt(utterance.replaceAll("'", ""))) {
							throw new GotoException(getNodeAttributeValue(child, "next"), null);
						}
					} catch (NumberFormatException e) {
						System.err.println("toto");
						if (child.getTextContent().contains(utterance.replaceAll("'", ""))) {
							throw new GotoException(getNodeAttributeValue(child, "next"), null);
						}
					}
				}
			}
		}
	}

	Properties getCurrentDialogProperties() {
		collectDialogProperty(context.getSelectedFormItem().getParentNode().getChildNodes());
		System.err.println(properties);
		return properties;
	}

	public InterpreterContext getContext() {
		return context;
	}

}
