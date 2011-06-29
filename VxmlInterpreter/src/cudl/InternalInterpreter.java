package cudl;

import static cudl.utils.Utils.getNodeAttributeValue;
import static cudl.utils.Utils.searchDialogByName;
import static cudl.utils.Utils.serachItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.script.ScriptException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cudl.script.DefaultInterpreterScriptContext;
import cudl.script.InterpreterScriptContext;
import cudl.script.InterpreterVariableDeclaration;
import cudl.utils.VxmlElementType;

public class InternalInterpreter {
	private InterpreterVariableDeclaration declaration;
	Node selectedItem;
	private List<Log> logs = new ArrayList<Log>();

	private List<Prompt> prompts = new ArrayList<Prompt>();
	private Properties dialogProperties = new Properties();

	private boolean nextItemSelectGuard = false;
	private boolean hangup;
	List<Node> grammarActive = new ArrayList<Node>();

	private Hashtable<String, NodeExecutor> nodeExecution = new Hashtable<String, NodeExecutor>() {
		{
			// TODO: implement interpretation of: reprompt,
			// return,

			// Just for W3C test
			put("conf:pass", new NodeExecutor() {
				public void execute(Node node) throws ExitException {
					Prompt prompt = new Prompt();
					prompt.tts = "pass";
					prompts.add(prompt);
					throw new ExitException();
				}
			});

			put("conf:noinput", new NodeExecutor() {
				public void execute(Node node) throws ExitException,
						InterpreterException {
					throw new EventException("noinput");
				}
			});

			put("conf:speech", new NodeExecutor() {
				public void execute(Node node) throws ScriptException,
						InterpreterException, IOException {
					String nodeValue = getNodeAttributeValue(node, "value");
					utterance("'" + nodeValue + "'", "'voice'");
				}
			});
			// Just for W3C test
			put("conf:fail", new NodeExecutor() {
				public void execute(Node node) throws DOMException,
						ScriptException {
					// w3cNodeConfSuite.add(node.toString());

					String reason = getNodeAttributeValue(node, "reason");
					if (reason != null)
						System.err.println("reason ->" + reason);
					else {
						String expr = getNodeAttributeValue(node, "expr");
						if (expr != null)
							System.err.println("reason ->"
									+ declaration.evaluateScript(expr, 50));
					}
				}
			});
			put("filled", new NodeExecutor() {
				public void execute(Node node) throws InterpreterException,
						ScriptException, IOException {
					if (!selectedItem.getNodeName().equals("subdialog"))
						throw new FilledException();
					else
						InternalInterpreter.this.execute(node);
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
				public void execute(Node node) throws InterpreterException {
					throw new EventException("connection.disconnect.hangup");
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

					String nextItemAtt = getNodeAttributeValue(node, "nextitem");
					if (nextItemAtt == null) {
						throw new GotoException(getNodeAttributeValue(node,
								"next"));
					}
					selectedItem = selectDialogNextItemTovisit(node);
					nextItemSelectGuard = true;
				}

			});
			put("submit", new NodeExecutor() {
				public void execute(Node node) throws GotoException,
						SubmitException, ScriptException {
					String next = getNodeAttributeValue(node, "next");

					String nameList = getNodeAttributeValue(node, "namelist");
					if (nameList != null) {
						StringTokenizer tokenizer = new StringTokenizer(
								nameList);
						String urlSuite = "?";
						while (tokenizer.hasMoreElements()) {
							String data = tokenizer.nextToken();
							urlSuite += data + "=" + declaration.getValue(data)
									+ "&";
						}
						next += urlSuite;
					}
					throw new SubmitException(next);
				}
			});

			put("log", new NodeExecutor() {
				public void execute(Node node) throws ScriptException {
					collectTrace(node);
				}
			});
			put("exit", new NodeExecutor() {

				public void execute(Node node) throws ExitException {
					// w3cNodeConfSuite.add("Just for exit");
					Prompt prompt = new Prompt();
					prompt.tts="pass";
					prompts.add(prompt);
					hangup = true;
					System.err.println("raccrocher par un exit");
					throw new ExitException();
				}
			});

			put("throw", new NodeExecutor() {
				public void execute(Node node) throws EventException,
						DOMException {
					throw new EventException(getNodeAttributeValue(node,
							"event"));
				}
			});

			put("property", new NodeExecutor() {
				public void execute(Node node) throws EventException,
						DOMException {
					collectProperty(node);
				}
			});

		}
	};
	public String transfertDestination;

	InternalInterpreter(String location) throws IOException, ScriptException {
		declaration = new InterpreterVariableDeclaration(location);
	}

	void interpretDialog(Node dialog) throws InterpreterException,
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
			InterpreterException {
		while (true) {
			// PHASE SELECTION
			if ((selectedItem = phaseSelect(dialog)) == null) {
				hangup = true;
				break;
			}

			// Grammar activation
			grammarActive.clear();
			activateGrammar();

			String nodeName = selectedItem.getNodeName();
			if (nodeName.equals("field")) {
				System.err.println("WAIT FOR USER INPUT");
				execute(selectedItem);
			} else if (nodeName.equals("record")) {
			} else if (nodeName.equals("object")) {
			} else if (nodeName.equals("subdialog")) {
				String src = getNodeAttributeValue(selectedItem, "src");
				execute(searchDialogByName(selectedItem.getParentNode()
						.getParentNode().getChildNodes(), src.replace("#", "")));
				execute(selectedItem);
				declaration.setValue(selectedItem, "new Object()",
						DefaultInterpreterScriptContext.DOCUMENT_SCOPE);
			} else if (nodeName.equals("transfer")) {
				String dest = getNodeAttributeValue(selectedItem, "dest");
				String destExpr = getNodeAttributeValue(selectedItem,
						"destexpr");
				transfertDestination = (dest != null) ? dest : declaration
						.evaluateScript(destExpr, 50)
						+ "";
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

	private void activateGrammar() {
		if (VxmlElementType.isInputItem(selectedItem))
			if (VxmlElementType.isAModalItem(selectedItem)) {
				grammarActive.add(serachItem(selectedItem, "grammar"));
				System.err.println("modal" + grammarActive.size());
			} else {
				Node parent = selectedItem;
				while (null != parent) {
					Node serachItem = serachItem(parent, "grammar");
					if (null != serachItem) {
						grammarActive.add(serachItem);
					}
					parent = parent.getParentNode();
				}
			}
	}

	void blindTransferSuccess() throws ScriptException, IOException,
			InterpreterException {
		throw new EventException("connection.disconnect.transfer");
	}

	void destinationHangup() throws ScriptException, InterpreterException,
			IOException {
		declaration.setValue(selectedItem, "'far_end_disconnect'",
				DefaultInterpreterScriptContext.DOCUMENT_SCOPE);
		mainLoop(selectedItem.getParentNode());
	}

	void callerHangDestination() throws ScriptException, IOException,
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
			execute(serachItem(selectedItem, "filled"));
		}
	}

	void callerHangup(int i) throws EventException, ScriptException {
		declaration.evaluateScript(
				"connection.protocol.isdnvn6.transferresult= '" + i + "'",
				DefaultInterpreterScriptContext.SESSION_SCOPE);
		throw new EventException("connection.disconnect.hangup");
	}

	void noAnswer() throws ScriptException, InterpreterException, IOException {
		declaration.evaluateScript(
				"connection.protocol.isdnvn6.transferresult= '2'",
				DefaultInterpreterScriptContext.SESSION_SCOPE);
		setTransferResultAndExecute("'noanswer'");
	}

//	private boolean isBlindTransfer(Node node) {
//		return !Boolean.parseBoolean(getNodeAttributeValue(node, "bridge"));
//	}

	void declareVariable(NodeList nodeList, int scope) throws ScriptException,
			IOException {
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
			String timeout = getNodeAttributeValue(node, "timeout");
			p.timeout = timeout != null ? timeout : "";

			String bargein = getNodeAttributeValue(node, "bargein");
			p.bargein = bargein != null ? bargein : "";

			String bargeinType = getNodeAttributeValue(node, "bargeintype");
			bargeinType = (String) (bargeinType == null ? dialogProperties
					.get("bargeintype") : bargeinType);
			p.bargeinType = bargeinType != null ? bargeinType : "";

			NodeList childs = node.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++) {
				Node child = childs.item(i);

				if (child.getNodeName().equals("audio")) {
					String audioSrc = getNodeAttributeValue(child, "src");
					if (audioSrc != null)
						p.audio += audioSrc + " ";

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

	private void clearVariable(Node node) {

		String namelist = getNodeAttributeValue(node, "namelist");

		if (namelist != null) {
			StringTokenizer tokenizer = new StringTokenizer(namelist);
			while (tokenizer.hasMoreElements()) {
				String name = (String) tokenizer.nextElement();
				declaration.setValue(name, "undefined", 50);
			}
		}

	}

	private void assignVariableValue(Node node1) throws ScriptException {
		// String expr = getNodeAttributeValue(node1, "expr");
		// String name = getNodeAttributeValue(node1, "name");
		// declaration.setValue(name, expr,
		// DefaultInterpreterScriptContext.ANONYME_SCOPE);

		// FIXME: remove node to setValue parameter, add string name generate
		Node expr = node1.getAttributes().getNamedItem("expr");
		declaration.setValue(node1, expr.getNodeValue(),
				DefaultInterpreterScriptContext.ANONYME_SCOPE);
	}

	private Node selectDialogNextItemTovisit(Node node) {
		String nextItem = getNodeAttributeValue(node, node.getNodeName()
				.equals("goto") ? "nextitem" : "next");
		return searchItemByName(node.getParentNode().getParentNode(), nextItem);
	}

	private Node searchItemByName(Node dialog, String id) {
		NodeList childs = dialog.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			String name = getNodeAttributeValue(child, "name");
			if (name != null && name.equals(id)) {
				return child;
			}
		}
		return null;
	}

	List<String> getTraceLog() {
		List<String> labeledLog = new ArrayList<String>();
		for (Iterator<Log> iterator = logs.iterator(); iterator.hasNext();) {
			labeledLog.add(((Log) iterator.next()).value);
		}

		return labeledLog;
	}

	List<String> getTracetWithLabel(String... label) {
		List<String> labeledLog = new ArrayList<String>();
		for (Iterator<Log> iterator = logs.iterator(); iterator.hasNext();) {
			Log log = (Log) iterator.next();
			for (int i = 0; i < label.length; i++) {
				if (log.label.equals(label[i])) {
					labeledLog.add(log.value);
				}
			}
		}
		return labeledLog;
	}

	private void collectTrace(Node node) throws ScriptException {
		Log log = new Log();

		String label = getNodeAttributeValue(node, "label");

		System.err.print("LOG:");
		if (label != null) {
			log.label = label;
			System.err.print(" " + label);
		}
		log.value = getNodeValue(node);
		System.err.println(" " + log.value);
		logs.add(log);
	}

	private String getNodeValue(Node node) throws ScriptException {
		NodeList childs = node.getChildNodes();
		String value = "";
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			if ("value".equals(child.getNodeName())) {
				value += declaration.getValue(getNodeAttributeValue(child,
						"expr"));
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

	List<Prompt> getPrompts() {
		return prompts;
	}

	void resetDocumentScope() {
		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.ANONYME_SCOPE);
		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.DIALOG_SCOPE);
		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.DOCUMENT_SCOPE);
	}

	void resetDialogScope() {
		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.ANONYME_SCOPE);
		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.DIALOG_SCOPE);

	}

	private boolean isAnExecutableItem(Node item) {
		return nodeExecution.get(item.getNodeName()) != null;
	}

	private boolean checkCond(Node node) throws ScriptException, IOException {
		String cond = getNodeAttributeValue(node, "cond");

		return cond == null
				|| declaration.evaluateScript(cond,
						DefaultInterpreterScriptContext.ANONYME_SCOPE).equals(
						"true");
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

	void resetApplicationScope() {
		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.APPLICATION_SCOPE);
		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.DOCUMENT_SCOPE);
		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.DIALOG_SCOPE);
		declaration
				.resetScopeBinding(DefaultInterpreterScriptContext.ANONYME_SCOPE);

	}

	void utterance(String string, String string2) throws ScriptException,
			InterpreterException, IOException {
		declaration.evaluateScript("lastresult$[0].utterance =" + string,
				InterpreterScriptContext.APPLICATION_SCOPE);
		declaration.evaluateScript("lastresult$[0].inputmode =" + string2,
				InterpreterScriptContext.APPLICATION_SCOPE);
		execute(serachItem(selectedItem, "filled"));
	}

	void setCurrentDialogProperties(Properties currentDialogProperties) {
		this.dialogProperties = currentDialogProperties;
	}

	// FIXME: add well management
	Properties getCurrentDialogProperties() {
		collectDialogProperty(selectedItem.getParentNode().getChildNodes());
		System.err.println(dialogProperties);
		return dialogProperties;
	}

	void maxTimeDisconnect() throws ScriptException, InterpreterException,
			IOException {
		setTransferResultAndExecute("'maxtime_disconnect'");
	}

	void destinationBusy() throws ScriptException, InterpreterException,
			IOException {
		setTransferResultAndExecute("'busy'");
	}

	void networkBusy() throws ScriptException, InterpreterException,
			IOException {
		setTransferResultAndExecute("'network_busy'");
	}

	boolean raccrochage() {
		return hangup;
	}
}