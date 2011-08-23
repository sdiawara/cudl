package cudl;

import static cudl.utils.Utils.getNodeAttributeValue;
import static cudl.utils.Utils.serachItem;

import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cudl.event.InterpreterEvent;
import cudl.script.InterpreterVariableDeclaration;

public class InternalInterpreter implements VxmlElement {
	private final InterpreterContext context;
	private InterpreterEventHandler ieh;
	private Properties properties = new Properties();
	private boolean test;

	public InternalInterpreter(InterpreterContext context) throws IOException, SAXException {
		this.context = context;
		ieh = new InterpreterEventHandler(context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws IOException, SAXException {
		Node node = context.getCurrentDialog();
		while (node != null) {
			try {
				VxmlElement dialog = TagInterpreterFactory.getTagInterpreter(node, context);
				if (test) {
					((FormTag) dialog).setInitVar(false);
				} else
					context.getFormItemNames().clear();
				dialog.interpret(context);
				break;
			} catch (GotoException e) {
				context.buildDocument(e.next);
				node = context.getCurrentDialog();
			} catch (SubmitException e) {
				context.buildDocument(e.next);
				node = context.getCurrentDialog();
			} catch (FilledException e) {
				break;
			} catch (EventException e) {
				try {
					ieh.doEvent(new InterpreterEvent(this, e.type));
				} catch (InterpreterException e1) {
					break;
				}
			} catch (TransferException e) {
				break;
			} catch (ReturnException e) {
				context.setReturnValue(e.namelist);
				break;
			} catch (InterpreterException e) {
			}
		}

		return null;
	}

	void blindTransferSuccess() throws IOException, SAXException, ParserConfigurationException {
		try {
			ieh.doEvent(new InterpreterEvent(this, "connection.disconnect.transfer"));
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	void destinationHangup() throws IOException, SAXException, ParserConfigurationException {
		context.getDeclaration().setValue(
				context.getFormItemNames().get(context.getSelectedFormItem()), "'far_end_disconnect'",
				InterpreterVariableDeclaration.DIALOG_SCOPE);
		System.err.println("Verif");
		test = true;
		interpret(context);
	}

	void callerHangDestination() throws IOException, SAXException, ParserConfigurationException {
		try {
			setTransferResultAndExecute("'near_end_disconnect'");
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	private void setTransferResultAndExecute(String transferResult) throws InterpreterException,
			IOException, SAXException {
		context.getDeclaration().setValue(
				context.getFormItemNames().get(context.getSelectedFormItem()), transferResult,
				InterpreterVariableDeclaration.DIALOG_SCOPE);
		FilledTag filled = (FilledTag) TagInterpreterFactory.getTagInterpreter(serachItem(context
				.getSelectedFormItem(), "filled"), context);
		filled.setExecute(true);
		filled.interpret(context);
	}

	void event(String eventType) throws IOException, SAXException, ParserConfigurationException {
		try {
			ieh.doEvent(new InterpreterEvent(this, eventType));
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	private void executionHandler(InterpreterException e) throws IOException, SAXException,
			ParserConfigurationException {

		if (e instanceof GotoException) {
			context.buildDocument(((GotoException) e).next);
			System.err.println("GOTO" + ((GotoException) e).next);
			interpret(context);
		} else if (e instanceof SubmitException) {
			context.buildDocument(((SubmitException) e).next);
			interpret(context);
		}
		if (e instanceof EventException) {
			try {
				ieh.doEvent(new InterpreterEvent(this, ((EventException) e).type));
			} catch (InterpreterException e1) {
				System.out.println("--------" + e.getClass().getSimpleName());
			}
		}
	}

	void callerHangup(int i) throws IOException, SAXException, ParserConfigurationException {
		context.getDeclaration().evaluateScript(
				"connection.protocol.isdnvn6.transferresult= '" + i + "'",
				InterpreterVariableDeclaration.SESSION_SCOPE);
		try {
			ieh.doEvent(new InterpreterEvent(this, "connection.disconnect.hangup"));
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	void noAnswer() throws IOException, SAXException, ParserConfigurationException {
		context.getDeclaration().evaluateScript("connection.protocol.isdnvn6.transferresult= '2'",
				InterpreterVariableDeclaration.SESSION_SCOPE);
		try {
			setTransferResultAndExecute("'noanswer'");
		} catch (InterpreterException e) {
			executionHandler(e);
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
			ParserConfigurationException {
		context.getDeclaration().evaluateScript("application.lastresult$[0].utterance =" + utterance,
				InterpreterVariableDeclaration.APPLICATION_SCOPE);
		context.getDeclaration().evaluateScript(
				"application.lastresult$[0].inputmode =" + utteranceType,
				InterpreterVariableDeclaration.APPLICATION_SCOPE);
		context.getDeclaration().setValue(
				context.getFormItemNames().get(context.getSelectedFormItem()), utterance, 60);
		try {
			FilledTag filled = (FilledTag) TagInterpreterFactory.getTagInterpreter(serachItem(context
					.getSelectedFormItem(), "filled"), context);
			filled.setExecute(true);
			filled.interpret(context);
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	void setCurrentDialogProperties(Properties currentDialogProperties) {
		this.properties = currentDialogProperties;
	}

	Properties getCurrentDialogProperties() {
		collectDialogProperty(context.getSelectedFormItem().getParentNode().getChildNodes());
		System.err.println(properties);
		return properties;
	}

	void maxTimeDisconnect() throws IOException, SAXException, ParserConfigurationException {
		try {
			setTransferResultAndExecute("'maxtime_disconnect'");
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	void destinationBusy() throws IOException, SAXException, ParserConfigurationException {
		try {
			setTransferResultAndExecute("'busy'");
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	void networkBusy() throws SAXException, IOException, ParserConfigurationException {
		try {
			setTransferResultAndExecute("'network_busy'");
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	public InterpreterContext getContext() {
		return context;
	}
}
