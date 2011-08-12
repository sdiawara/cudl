package cudl;

import static cudl.utils.Utils.getNodeAttributeValue;
import static cudl.utils.Utils.serachItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.script.ScriptException;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cudl.event.InterpreterEvent;
import cudl.script.InterpreterVariableDeclaration;

class InternalInterpreter {
	private InterpreterVariableDeclaration declaration;
	private Properties dialogProperties = new Properties();
	private FormInterpreationAlgorithm fia;
	private InterpreterContext context;
	private InterpreterEventHandler interpreterListener;

	InternalInterpreter(String location) throws IOException, 
			ParserConfigurationException, SAXException {
		declaration = new InterpreterVariableDeclaration(location);
		context = new InterpreterContext(location, declaration);
		interpreterListener = new InterpreterEventHandler(context);
	}

	void interpretDialog() throws  IOException, SAXException {
		fia = new FormInterpreationAlgorithm(context, declaration);
		fia.initializeDialog(context.getCurrentDialog());
	}

	void mainLoop() throws  IOException, SAXException, ParserConfigurationException {
		try {
			fia.mainLoop();
		} catch (GotoException e) {
			context.buildDocument(e.next);
			fia.initializeDialog(context.getCurrentDialog());
			mainLoop();
		} catch (SubmitException e) {
			System.err.println("submmit " + e.next);
			context.buildDocument(e.next);
			fia.initializeDialog(context.getCurrentDialog());
			mainLoop();
		} catch (EventException e) {
			try {
				interpreterListener.doEvent(new InterpreterEvent(this, e.type), fia.executor);
			} catch (InterpreterException e1) {
				executionHandler(e1);
			}
		} catch (InterpreterException e) {
			System.err.println(e.getClass() + "INGNORE");
		}
	}

	void blindTransferSuccess() throws  IOException, SAXException,
			ParserConfigurationException {
		try {
			interpreterListener.doEvent(new InterpreterEvent(this, "connection.disconnect.transfer"),
					fia.executor);
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	void destinationHangup() throws  IOException, SAXException,
			ParserConfigurationException {
		declaration.setValue(fia.getFormItemName(context.getSelectedFormItem()),
				"'far_end_disconnect'", InterpreterVariableDeclaration.DIALOG_SCOPE);
		mainLoop();
	}

	void callerHangDestination() throws  IOException, SAXException,
			ParserConfigurationException {
		try {
			setTransferResultAndExecute("'near_end_disconnect'");
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	private void setTransferResultAndExecute(String transferResult) throws 
			InterpreterException, IOException {
		declaration.setValue(fia.getFormItemName(context.getSelectedFormItem()), transferResult,
				InterpreterVariableDeclaration.DIALOG_SCOPE);
		fia.executor.execute(serachItem(context.getSelectedFormItem(), "filled"));
	}

	void event(String eventType) throws  IOException, SAXException,
			ParserConfigurationException {
		try {
			interpreterListener.doEvent(new InterpreterEvent(this, eventType), fia.executor);
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	private void executionHandler(InterpreterException e) throws  IOException,
			SAXException, ParserConfigurationException {

		if (e instanceof GotoException) {
			declaration.resetScopeBinding(InterpreterVariableDeclaration.ANONYME_SCOPE);
			context.buildDocument(((GotoException) e).next);
			System.err.println("" + ((GotoException) e).next);
			fia.initializeDialog(context.getCurrentDialog());
			mainLoop();
		} else if (e instanceof SubmitException) {
			context.buildDocument(((SubmitException) e).next);
			fia.initializeDialog(context.getCurrentDialog());
			mainLoop();
		}
	}

	void callerHangup(int i) throws  IOException, SAXException,
			ParserConfigurationException {
		declaration.evaluateScript("connection.protocol.isdnvn6.transferresult= '" + i + "'",
				InterpreterVariableDeclaration.SESSION_SCOPE);
		try {
			interpreterListener.doEvent(new InterpreterEvent(this, "connection.disconnect.hangup"),
					fia.executor);
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	void noAnswer() throws  IOException, SAXException, ParserConfigurationException {
		declaration.evaluateScript("connection.protocol.isdnvn6.transferresult= '2'",
				InterpreterVariableDeclaration.SESSION_SCOPE);
		try {
			setTransferResultAndExecute("'noanswer'");
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	List<String> getTraceLog() {
		List<String> labeledLog = new ArrayList<String>();
		for (Iterator<Log> iterator = fia.getLogs().iterator(); iterator.hasNext();) {
			labeledLog.add(((Log) iterator.next()).value);
		}

		return labeledLog;
	}

	List<String> getTracetWithLabel(String... label) {
		List<String> labeledLog = new ArrayList<String>();
		for (Iterator<Log> iterator = fia.getLogs().iterator(); iterator.hasNext();) {
			Log log = (Log) iterator.next();
			for (int i = 0; i < label.length; i++) {
				if (log.label.equals(label[i])) {
					labeledLog.add(log.value);
				}
			}
		}
		return labeledLog;
	}

	List<Prompt> getPrompts() {
		return fia.getPrompts();
	}

	private void collectDialogProperty(NodeList nodeList) {
		dialogProperties.clear();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeName().equals("property")) {
				collectProperty(node);
			}
		}
	}

	private void collectProperty(Node node) {
		dialogProperties.put(getNodeAttributeValue(node, "name"),
				getNodeAttributeValue(node, "value"));
	}

	void resetApplicationScope() throws ScriptException {
		declaration.resetScopeBinding(InterpreterVariableDeclaration.APPLICATION_SCOPE);
	}

	void utterance(String utterance, String utteranceType) throws  IOException,
			SAXException, ParserConfigurationException {
		declaration.evaluateScript("application.lastresult$[0].utterance =" + utterance,
				InterpreterVariableDeclaration.APPLICATION_SCOPE);
		declaration.evaluateScript("application.lastresult$[0].inputmode =" + utteranceType,
				InterpreterVariableDeclaration.APPLICATION_SCOPE);
		declaration.setValue(fia.getFormItemName(context.getSelectedFormItem()), utterance, 60);
		try {
			fia.executor.execute(serachItem(context.getSelectedFormItem(), "filled"));
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	void setCurrentDialogProperties(Properties currentDialogProperties) {
		this.dialogProperties = currentDialogProperties;
	}

	Properties getCurrentDialogProperties() {
		collectDialogProperty(context.getSelectedFormItem().getParentNode().getChildNodes());
		System.err.println(dialogProperties);
		return dialogProperties;
	}

	void maxTimeDisconnect() throws  IOException, SAXException,
			ParserConfigurationException {
		try {
			setTransferResultAndExecute("'maxtime_disconnect'");
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	void destinationBusy() throws  IOException, SAXException,
			ParserConfigurationException {
		try {
			setTransferResultAndExecute("'busy'");
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	void networkBusy() throws  SAXException, IOException,
			ParserConfigurationException {
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