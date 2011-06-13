package fr.mbs.vxml.interpreter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.script.ScriptException;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.mbs.vxml.interpreter.execption.DisconnectException;
import fr.mbs.vxml.interpreter.execption.EventException;
import fr.mbs.vxml.interpreter.execption.ExitException;
import fr.mbs.vxml.interpreter.execption.FilledException;
import fr.mbs.vxml.interpreter.execption.GotoException;
import fr.mbs.vxml.interpreter.execption.InterpreterException;
import fr.mbs.vxml.interpreter.execption.SubmitException;
import fr.mbs.vxml.script.DefaultInterpreterScriptContext;
import fr.mbs.vxml.script.InterpreterScriptContext;
import fr.mbs.vxml.script.InterpreterVariableDeclaration;
import fr.mbs.vxml.utils.Prompt;
import fr.mbs.vxml.utils.Utils;
import fr.mbs.vxml.utils.VxmlElementType;

public class Interpreter {
	private InterpreterVariableDeclaration declaration;
	public Node selectedItem;
	public List<String> w3cNodeConfSuite = new ArrayList<String>();

	private List<String> traceLog = new ArrayList<String>();
	private List<String> traceStat = new ArrayList<String>();

	private List<Prompt> prompts = new ArrayList<Prompt>();
	private Properties currentDialogProperties = new Properties();

	private boolean nextItemSelectGuard = false;

	private Hashtable<String, NodeExecutor> nodeExecution = new Hashtable<String, NodeExecutor>() {
		{
			// TODO: implement interpretation of: reprompt,
			// return,

			// Just for W3C test
			put("conf:pass", new NodeExecutor() {
				public void execute(Node node) throws ExitException {
					w3cNodeConfSuite.add(node.toString());
					throw new ExitException();
				}
			});
			// Just for W3C test
			put("conf:fail", new NodeExecutor() {
				public void execute(Node node) {
					w3cNodeConfSuite.add(node.toString());
					NamedNodeMap attributes = node.getAttributes();
					if (attributes == null)
						return;
					if (node.getAttributes().getNamedItem("reason") != null)
						System.err.println(node.getAttributes().getNamedItem(
								"reason").getTextContent());
				}
			});
			put("filled", new NodeExecutor() {
				public void execute(Node node) throws FilledException {
					throw new FilledException();
				}
			});
			put("prompt", new NodeExecutor() {
				public void execute(Node node) throws ScriptException,
						IOException {
					collectPrompt(node);

				}
			});
			put("#text", new NodeExecutor() {
				public void execute(Node node) {
					collectSimpleSpeechPrompt(node);
				}
			});
			put("reprompt", new NodeExecutor() {
				public void execute(Node node) {
					System.err.println("TODO: implemente reprompt");
				}
			});
			put("return", new NodeExecutor() {
				public void execute(Node node) {
					// System.err.println("TODO: implement return interpretation");
					// throw
				}
			});
			put("disconnect", new NodeExecutor() {
				public void execute(Node node) throws DisconnectException {
					throw new DisconnectException();
				}
			});
			put("script", new NodeExecutor() {
				public void execute(Node node) throws ScriptException,
						IOException {
					declaration.evaluateScript(node,
							DefaultInterpreterScriptContext.ANONYME_SCOPE);
				}
			});
			put("var", new NodeExecutor() {
				public void execute(Node node) throws ScriptException {
					declaration.declareVariable(node,
							DefaultInterpreterScriptContext.ANONYME_SCOPE);
				}
			});
			put("assign", new NodeExecutor() {
				public void execute(Node node) throws ScriptException {
					assignVariableValue(node);
				}
			});
			put("clear", new NodeExecutor() {
				public void execute(Node node) {
					clearVariable(node);
				}
			});
			put("if", new NodeExecutor() {
				public void execute(Node node) throws InterpreterException,
						ScriptException, IOException {
					checkConditionAndExecute(node);
				}
			});
			put("goto", new NodeExecutor() {
				public void execute(Node node) throws GotoException,
						ScriptException {
					// FIXME: do same for expritem (script interpretation value
					// )
					// add assert163.txml file from to test

					if (node.getAttributes().getNamedItem("nextitem") == null) {
						throw new GotoException(node.getAttributes()
								.getNamedItem("next").getNodeValue());
					}
					selectedItem = selectDialogNextItemTovisit(node);
					nextItemSelectGuard = true;
				}

			});
			put("submit", new NodeExecutor() {
				public void execute(Node node) throws GotoException,
						SubmitException, ScriptException {
					throw new SubmitException(node, declaration);
				}

			});
			put("log", new NodeExecutor() {
				public void execute(Node node) throws ScriptException {
					collectTrace(node);
				}
			});
			put("exit", new NodeExecutor() {
				public void execute(Node node) throws ExitException {
					w3cNodeConfSuite.add("Just for exit");
					throw new ExitException();
				}
			});

			put("throw", new NodeExecutor() {
				public void execute(Node node) throws EventException,
						DOMException {
					throw new EventException(node.getAttributes().getNamedItem(
							"event").getNodeValue());
				}
			});
		}
	};
	public String transfertDestination;

	public Interpreter() throws IOException, ScriptException {
		declaration = new InterpreterVariableDeclaration();
	}

	public void interpretDialog(Node dialog) throws InterpreterException,
			ScriptException, IOException {

		// PHASE INITIALIZATION
		NodeList nodeList = dialog.getChildNodes();
		declareVariable(nodeList, DefaultInterpreterScriptContext.DIALOG_SCOPE);
		collectDialogProperty(nodeList);

		// TODO: Si l'utlisateur sysest rentré an prononçant une phrase de la
		// grammaire dans un autre formulaire , aller directement à la phrase de
		// traitement

		// Boucle principale

		mainLoop(dialog);
	}

	private void mainLoop(Node dialog) throws ScriptException, IOException,
			InterpreterException, TransferException {
		while (true) {
			// PHASE SELECTION
			if ((selectedItem = phaseSelect(dialog)) == null) {
				break;
			}

			// Exécuter l'élément de formulaire.
			String nodeName = selectedItem.getNodeName();
			if (nodeName.equals("field")) {
				System.err.println("WAIT FOR USER INPUT");
				execute(selectedItem);
			} else if (nodeName.equals("record")) {
			} else if (nodeName.equals("object")) {
			} else if (nodeName.equals("subdialog")) {
				execute(Utils.searchDialogByName(selectedItem.getParentNode()
						.getParentNode().getChildNodes(), selectedItem
						.getAttributes().getNamedItem("src").getNodeValue()
						.replace("#", "")));
				declaration.setValue(selectedItem, "new Object()",
						DefaultInterpreterScriptContext.DOCUMENT_SCOPE);
			} else if (nodeName.equals("transfer")) {
				w3cNodeConfSuite.add("transfer "
						+ (isBlindTransfer(selectedItem) ? "blind" : "bridge"));
				Node namedItem = selectedItem.getAttributes().getNamedItem(
						"dest");
				transfertDestination = namedItem == null ? selectedItem
						.getAttributes().getNamedItem("destexpr")
						.getNodeValue() : namedItem.getNodeValue();
				throw new TransferException();
			} else if (nodeName.equals("initial")) {
			} else if (nodeName.equals("block")) {
				declaration.setValue(selectedItem, "new Object()",
						DefaultInterpreterScriptContext.DOCUMENT_SCOPE);
				execute(selectedItem);
				declaration
						.resetScopeBinding(InterpreterScriptContext.ANONYME_SCOPE);
			}
		}
	}

	public void blindTransferSuccess() throws ScriptException, IOException,
			InterpreterException {
		throw new EventException("connection.disconnect.transfer");
	}

	public void destinationHangup() throws ScriptException,
			InterpreterException, IOException {
		declaration.setValue(selectedItem, "'far_end_disconnect'",
				DefaultInterpreterScriptContext.DOCUMENT_SCOPE);
		mainLoop(selectedItem.getParentNode());
	}

	public void callerHangDestination() throws ScriptException, IOException,
			InterpreterException {
		setTransferResultAndExecute("'near_end_disconnect'");
	}

	private void setTransferResultAndExecute(String transferResult)
			throws ScriptException, InterpreterException, IOException {
		declaration.setValue(selectedItem, transferResult,
				DefaultInterpreterScriptContext.DOCUMENT_SCOPE);
		try {
			execute(selectedItem);
		} catch (FilledException e) {
			execute(Utils.serachItem(selectedItem, "filled"));
		}
	}

	public void callerHangup() throws EventException {
		throw new EventException("connection.disconnect.hangup");
	}

	public void noAnswer() throws ScriptException, InterpreterException,
			IOException {
		setTransferResultAndExecute("'noanswer'");
	}

	private boolean isBlindTransfer(Node selectedItem2) {
		Node namedItem = selectedItem2.getAttributes().getNamedItem("bridge");
		return namedItem == null || namedItem.getNodeValue().equals("false");
	}

	public void declareVariable(NodeList nodeList, int scope)
			throws ScriptException, IOException {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeName().equals("var"))
				declaration.declareVariable(node, scope);
			else if (VxmlElementType.isFormItem(node)) {
				declaration.declareDialogItem(node);
			} else if (node.getNodeName().equals("script")) {
				declaration.evaluateScript(node, scope);
			}
		}
	}

	public void execute(Node node) throws InterpreterException,
			ScriptException, IOException {
		NodeList child = node.getChildNodes();
		for (int i = 0; i < child.getLength(); i++) {
			Node node1 = child.item(i);
			NodeExecutor executor = nodeExecution.get(node1.getNodeName());

			if (null != executor) {
				executor.execute(node1);
			}

			if (nextItemSelectGuard) {
				nextItemSelectGuard = false;
				return;
			}
		}
	}

	private void collectPrompt(Node node) throws ScriptException, IOException {
		Prompt p = new Prompt();
		if (node.getNodeName().equals("prompt")) {
			NamedNodeMap attributes = node.getAttributes();
			if (attributes.getLength() > 0) {
				Node timeout = attributes.getNamedItem("timeout");
				if (timeout != null) {
					p.timeout = timeout.getNodeValue();
				}
				Node bargein = attributes.getNamedItem("bargein");
				if (bargein != null) {
					p.bargein = bargein.getNodeValue();
				}

				Node bargeinType = attributes.getNamedItem("bargeintype");
				if (bargeinType != null) {
					p.bargeinType = bargeinType.getNodeValue();
				} else if (getCurrentDialogProperties().get("bargeintype") != null) {
					p.bargeinType = getCurrentDialogProperties().getProperty(
							"bargeintype");
				}
			}

			NodeList childs = node.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++) {
				Node child = childs.item(i);

				if (child.getNodeName().equals("audio")) {

					NamedNodeMap attributesAudio = child.getAttributes();
					Node src = attributesAudio.getNamedItem("src");
					if (src != null) {
						p.audio += src.getNodeValue() + " ";
					}
					if (child.getChildNodes().getLength() > 0)
						p.tts += child.getChildNodes().item(0).getNodeValue()
								+ " ";
				} else if (child.getNodeName().equals("#text")) {
					addPromtTextToSpeech(child, p);
				} else if (child.getNodeName().equals("value")) {
					addPromptWithValue(child, p);
				}
			}
		}
		p.tts = p.tts.trim();
		prompts.add(p);
	}

	private void collectSimpleSpeechPrompt(Node node) {
		Prompt p = new Prompt();
		addPromtTextToSpeech(node, p);
		if (p.tts != "") {
			p.tts = p.tts.trim();
			prompts.add(p);
		}
	}

	private void addPromtTextToSpeech(Node node, Prompt p) {
		String nodeValue = node.getNodeValue().trim();
		if (nodeValue != null && !nodeValue.equals(" ")
				&& nodeValue.length() > 0) {
			p.tts += nodeValue + " ";
		}
	}

	private void addPromptWithValue(Node value, Prompt p)
			throws ScriptException, IOException {
		p.tts += declaration.evaluateScript(value,
				DefaultInterpreterScriptContext.ANONYME_SCOPE);
	}

	private void clearVariable(Node node1) {

		String[] nameList = node1.getAttributes().getNamedItem("namelist")
				.getNodeValue().split(" ");

		for (int i = 0; i < nameList.length; i++) {
		}
	}

	private void assignVariableValue(Node node1) throws ScriptException {
		Node expr = node1.getAttributes().getNamedItem("expr");
		declaration.setValue(node1, expr.getNodeValue(),
				DefaultInterpreterScriptContext.ANONYME_SCOPE);
	}

	private Node selectDialogNextItemTovisit(Node node) {
		Node nextItem = node.getAttributes().getNamedItem(
				node.getNodeName().equals("goto") ? "nextitem" : "next");
		return (nextItem != null) ? searchItemByName(node.getParentNode()
				.getParentNode(), nextItem.getNodeValue()) : null;
	}

	private Node searchItemByName(Node dialog, String id) {
		NodeList childs = dialog.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			NamedNodeMap attributes = child.getAttributes();

			if (attributes != null && attributes.getLength() > 0) {
				Node namedItem = attributes.getNamedItem("name");
				if (namedItem != null && namedItem.getNodeValue().equals(id)) {
					return child;
				}
			}
		}
		return null;
	}

	public List<String> getTraceLog() {
		return traceLog;
	}

	public List<String> getTraceStat() {
		return traceStat;
	}

	private void collectTrace(Node node) throws ScriptException {
		String value = getNodeValue(node);
		traceLog.add(value);
		System.err.println("LOG :" + value);
		for (int j = 0; j < node.getAttributes().getLength(); j++) {
			traceStat.add("[" + node.getAttributes().item(j).getNodeName()
					+ ":" + node.getAttributes().item(j).getNodeValue() + "] "
					+ node.getTextContent());
		}
	}

	private String getNodeValue(Node node) throws ScriptException {
		NodeList childs = node.getChildNodes();
		String value = "";

		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			if ("value".equals(child.getNodeName())) {
				value += declaration.getValue(child.getAttributes()
						.getNamedItem("expr").getTextContent());
			} else
				value += child.getNodeValue();
		}
		return value;
	}

	private void checkConditionAndExecute(Node node)
			throws InterpreterException, ScriptException, IOException {
		boolean conditionChecked = this.checkCond(node);
		NodeList childs = node.getChildNodes();

		for (int i = 0; i < childs.getLength(); i++) {
			Node item = childs.item(i);
			if (isAnExecutableItem(item)) {
				// FIXME: Déplacer dans VxmlElement la fonction
				// isAnExecutableItem
				if (conditionChecked) {
					nodeExecution.get(item.getNodeName()).execute(item);
				}
			} else if (VxmlElementType.isConditionalItem(item)) {
				if (conditionChecked) {
					break;
				} else {
					conditionChecked = this.checkCond(item);
				}
			}
		}
	}

	public List<Prompt> getPrompts() {
		return prompts;
	}

	public void resetDocumentScope() {
		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.ANONYME_SCOPE);
		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.DIALOG_SCOPE);
		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.DOCUMENT_SCOPE);
	}

	public void resetDialogScope() {
		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.ANONYME_SCOPE);

		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.DIALOG_SCOPE);

	}

	private boolean isAnExecutableItem(Node item) {
		return nodeExecution.get(item.getNodeName()) != null;
	}

	private boolean checkCond(Node node) throws ScriptException, IOException {
		NamedNodeMap attribute = node.getAttributes();
		Node cond = (attribute.getLength() == 0) ? null : attribute
				.getNamedItem("cond");

		return cond == null
				|| declaration.evaluateScript(node,
						DefaultInterpreterScriptContext.ANONYME_SCOPE).equals(
						"true");

	}

	private void collectDialogProperty(NodeList nodeList) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeName().equals("property")) {
				collectProperty(node.getAttributes());
			}
		}
	}

	private void collectProperty(NamedNodeMap attributes) {
		getCurrentDialogProperties().put(
				attributes.getNamedItem("name").getNodeValue(),
				attributes.getNamedItem("value").getNodeValue());
	}

	private Node phaseSelect(Node dialog) throws ScriptException, IOException {
		return (selectedItem != null && declaration.getValue(selectedItem)
				.equals("undefined")) ? selectedItem
				: unsatisfiedGuardCondition(dialog.getChildNodes());
	}

	private Node unsatisfiedGuardCondition(NodeList nodeList)
			throws ScriptException, IOException {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);

			if (VxmlElementType.isFormItem(node)) {
				if (declaration.getValue(node).equals("undefined"))
					if (this.checkCond(node)) {
						return node;
					}
			}
		}
		return null;
	}

	public void resetApplicationScope() {
		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.APPLICATION_SCOPE);
		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.DOCUMENT_SCOPE);
		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.DIALOG_SCOPE);
		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.ANONYME_SCOPE);

	}

	public void utterance(String string, String string2) throws ScriptException {
		declaration.evaluateScript("lastresult$[0].utterance =" + string,
				InterpreterScriptContext.APPLICATION_SCOPE);
		declaration.evaluateScript("lastresult$[0].inputmode =" + string2,
				InterpreterScriptContext.APPLICATION_SCOPE);
	}

	public void setCurrentDialogProperties(Properties currentDialogProperties) {
		this.currentDialogProperties = currentDialogProperties;
	}

	public Properties getCurrentDialogProperties() {
		return currentDialogProperties;
	}

	public void maxTimeDisconnect() throws ScriptException,
			InterpreterException, IOException {
		setTransferResultAndExecute("'maxtime_disconnect'");
	}

	public void destinationBusy() throws ScriptException, InterpreterException,
			IOException {
		setTransferResultAndExecute("'busy'");
	}

	public void networkBusy() throws ScriptException, InterpreterException,
			IOException {
		setTransferResultAndExecute("'network_busy'");
	}
}