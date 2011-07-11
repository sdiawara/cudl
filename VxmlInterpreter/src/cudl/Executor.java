package cudl;

import static cudl.utils.Utils.getNodeAttributeValue;
import static cudl.utils.Utils.serachItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.script.ScriptException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cudl.script.DefaultInterpreterScriptContext;
import cudl.script.InterpreterScriptContext;
import cudl.script.InterpreterVariableDeclaration;
import cudl.utils.VxmlElementType;

class Executor {
	private final WIPContext context;
	private final InterpreterVariableDeclaration declaration;

	// This contains what would hear by users
	List<Prompt> prompts = new ArrayList<Prompt>();
	List<Log> logs = new ArrayList<Log>();

	// this list contains all executable node
	Map<String, NodeExecutor> nodeExecutors = new HashMap<String, NodeExecutor>() {
		{
			put("conf:pass", new NodeExecutor() {
				public void execute(Node node) throws ExitException {
					Prompt prompt = new Prompt();
					prompt.tts = "pass";
					prompts.add(prompt);
					System.err.println("loll");
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
					declaration.evaluateScript("lastresult$[0].utterance ='"
							+ nodeValue + "'",
							InterpreterScriptContext.APPLICATION_SCOPE);
					declaration.evaluateScript("lastresult$[0].inputmode ="
							+ "'voice'",
							InterpreterScriptContext.APPLICATION_SCOPE);

					Executor.this.execute(serachItem(context
							.getSelectedFormItem(), "filled"));
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

			put("var", new NodeExecutor() {
				@Override
				public void execute(Node node) throws InterpreterException,
						ScriptException, IOException {
					String name = getNodeAttributeValue(node, "name");
					String expr = getNodeAttributeValue(node, "expr");
					declaration.setValue(name, expr == null ? "undefined"
							: expr, InterpreterScriptContext.ANONYME_SCOPE);
				}
			});
			put("assign", new NodeExecutor() {
				@Override
				public void execute(Node node) throws InterpreterException,
						ScriptException, IOException {
					String name = getNodeAttributeValue(node, "name");
					String expr = getNodeAttributeValue(node, "expr");
					declaration.evaluateScript(name + "=" + expr, 50);
				}
			});
			put("clear", new NodeExecutor() {
				public void execute(Node node) throws ScriptException {
					clearVariable(node);
				}
			});

			put("if", new NodeExecutor() {
				public void execute(Node node) throws InterpreterException,
						ScriptException, IOException {
					checkConditionAndExecute(node);
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
					// TODO: implemement
					throw new RuntimeException("REPROMPT non implementer");
				}
			});
			put("goto", new NodeExecutor() {
				public void execute(Node node) throws GotoException,
						ScriptException {
					// FIXME: do same for expritem (script interpretation value
					// )
					// add assert163.txml file from to test
					String nextItemAtt = getNodeAttributeValue(node, "nextitem");
					String next = getNodeAttributeValue(node, "next");

					if (nextItemAtt == null)
						nextItemAtt = getNodeAttributeValue(node, "expritem");
					if (next == null)
						next = getNodeAttributeValue(node, "expr");
					throw new GotoException(next, nextItemAtt);
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
			put("exit", new NodeExecutor() {
				@Override
				public void execute(Node node) throws InterpreterException,
						ScriptException, IOException {
					context.setHangup(true);
					throw new ExitException();
				}
			});
			put("return", new NodeExecutor() {
				@Override
				public void execute(Node node) throws InterpreterException,
						ScriptException, IOException {
					System.err.println("return");
				//	throw new ReturnException();
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
			put("log", new NodeExecutor() {
				@Override
				public void execute(Node node) throws InterpreterException,
						ScriptException, IOException {
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
			});
			put("throw", new NodeExecutor() {
				public void execute(Node node) throws EventException,
						DOMException {
					throw new EventException(getNodeAttributeValue(node,
							"event"));
				}
			});

			put("filled", new NodeExecutor() {
				public void execute(Node node) throws DOMException,
						FilledException {
					throw new FilledException(context.getSelectedFormItem());
				}
			});
		}
	};

	Executor(WIPContext context, InterpreterVariableDeclaration declaration) {
		this.context = context;
		this.declaration = declaration;
	}

	void execute(Node node) throws InterpreterException, ScriptException,
			IOException {
		NodeList child = node.getChildNodes();
		for (int i = 0; i < child.getLength(); i++) {
			Node node1 = child.item(i);
			NodeExecutor executor = nodeExecutors.get(node1.getNodeName());
			if (null != executor) {
				executor.execute(node1);
			}
		}
		declaration.resetScopeBinding(InterpreterScriptContext.ANONYME_SCOPE);
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

	private void clearVariable(Node node) throws ScriptException {

		String namelist = getNodeAttributeValue(node, "namelist");

		if (namelist != null) {
			StringTokenizer tokenizer = new StringTokenizer(namelist);
			while (tokenizer.hasMoreElements()) {
				String name = (String) tokenizer.nextElement();
				declaration.setValue(name, "undefined", 50);
			}
		} else {
			// TODO: reinit all formItem
		}
	}

	private void checkConditionAndExecute(Node node)
			throws InterpreterException, ScriptException, IOException {
		boolean conditionChecked = this.checkCond(node);
		NodeList childs = node.getChildNodes();

		for (int i = 0; i < childs.getLength(); i++) {
			Node item = childs.item(i);
			if (VxmlElementType.isAnExecutableItem(item)
					|| item.getNodeName().contains("conf")) {
				if (conditionChecked) {
					NodeExecutor nodeExecutor = nodeExecutors.get(item
							.getNodeName());
					if (nodeExecutor != null)
						nodeExecutor.execute(item);
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

	private boolean checkCond(Node node) throws ScriptException, IOException {
		String cond = getNodeAttributeValue(node, "cond");

		return cond == null
				|| declaration.evaluateScript(cond,
						DefaultInterpreterScriptContext.ANONYME_SCOPE).equals(
						"true");
	}

	private void collectPrompt(Node node) throws ScriptException, IOException {
		Prompt p = new Prompt();
		if (node.getNodeName().equals("prompt")) {
			String timeout = getNodeAttributeValue(node, "timeout");
			p.timeout = timeout != null ? timeout : "";

			String bargein = getNodeAttributeValue(node, "bargein");
			p.bargein = bargein != null ? bargein : "";

			String bargeinType = getNodeAttributeValue(node, "bargeintype");
			// bargeinType = (String) (bargeinType == null ? dialogProperties
			// .get("bargeintype") : bargeinType);
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

	private void collectSimpleSpeechPrompt(Node node) {
		Prompt p = new Prompt();
		addPromtTextToSpeech(node, p);
		if (p.tts != "") {
			p.tts = p.tts.trim();
			prompts.add(p);
		}
	}
}