package fr.mbs.vxml.interpreter;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.print.attribute.standard.MediaSize.Engineering;
import javax.script.ScriptException;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.mbs.vxml.interpreter.execption.DisconnectException;
import fr.mbs.vxml.interpreter.execption.ExitException;
import fr.mbs.vxml.interpreter.execption.FilledException;
import fr.mbs.vxml.interpreter.execption.GotoException;
import fr.mbs.vxml.interpreter.execption.InterpreterException;
import fr.mbs.vxml.interpreter.execption.SubmitException;
import fr.mbs.vxml.utils.Prompt;
import fr.mbs.vxml.utils.VariableDeclaration;
import fr.mbs.vxml.utils.VxmlElementType;

public class Interpreter {
	public VariableDeclaration variableVxml = new VariableDeclaration();
	public Node selectedItem;
	public List<String> w3cNodeConfSuite = new ArrayList<String>();

	private Map<Node, String> nodeItemVariablesName = new Hashtable<Node, String>();
	private List<String> traceLog = new ArrayList<String>();
	private List<String> traceStat = new ArrayList<String>();
	private List<Prompt> prompts = new ArrayList<Prompt>();

	private boolean nextItemSelectGuard = false;
	private Properties currentDialogProperties = new Properties();

	private Hashtable<String, NodeExecutor> nodeExecution = new Hashtable<String, NodeExecutor>() {
		{
			// TODO: implement interpretation of: reprompt,
			// return,
			// script

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
					w3cNodeConfSuite.add(node.getNodeName()+" reason ="+node.getAttributes().getNamedItem(
					 "reason").getNodeValue());
					 
				}
			});

			put("filled", new NodeExecutor() {
				public void execute(Node node) throws FilledException {
					throw new FilledException();
				}
			});
			put("prompt", new NodeExecutor() {
				public void execute(Node node) {
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
					System.out.println("TODO: implemente reprompt");
				}
			});
			put("return", new NodeExecutor() {
				public void execute(Node node) {
					System.err.println("TODO: implement return interpretation");
				}
			});
			put("disconnect", new NodeExecutor() {
				public void execute(Node node) throws DisconnectException {
					throw new DisconnectException();
				}
			});
			put("script", new NodeExecutor() {
				public void execute(Node node) throws ScriptException {
					System.err.println("scripppppppppppppppptinnnnnnnng oki");
					try {
						variableVxml.evaluateScript(node, getNodeScope(node));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (DOMException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			put("var", new NodeExecutor() {
				public void execute(Node node) throws DOMException,
						ScriptException {
					variableVxml.declareVariable(node, getNodeScope(node));
				}
			});
			put("assign", new NodeExecutor() {
				public void execute(Node node) {
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
						ScriptException {
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

						variableVxml.resetScope(1);
						throw new GotoException(node.getAttributes()
								.getNamedItem("next").getNodeValue());
					}
					selectedItem = selectDialogNextItemTovisit(node);
					nextItemSelectGuard = true;
				}

			});
			put("submit", new NodeExecutor() {
				public void execute(Node node) throws GotoException,
						SubmitException {
					throw new SubmitException(node, variableVxml);
				}

			});
			put("log", new NodeExecutor() {
				public void execute(Node node) {
					collectTrace(node);
				}
			});
			put("exit", new NodeExecutor() {
				public void execute(Node node) throws ExitException {
					w3cNodeConfSuite.add("Just for exit");
					throw new ExitException();
				}
			});
		}
	};

	public void interpretDialog(Node dialog) throws InterpreterException,
			ScriptException, FileNotFoundException, DOMException {

		// PHASE INITIALIZATION
		NodeList nodeList = dialog.getChildNodes();
		declareVariable(nodeList);
		collectDialogProperty(nodeList);

		// TODO: Si l'utlisateur sysest rentré an prononçant une phrase de la
		// grammaire dans un autre formulaire , aller directement à la phrase de
		// traitement

		// Boucle principale

		while (true) {
			// PHASE SELECTION
			if ((selectedItem = phaseSelect(dialog)) == null) {
				break;
			}
			// Exécuter l'élément de formulaire.

			String nodeName = selectedItem.getNodeName();
			if (nodeName.equals("field")) {
				execute(selectedItem);
				return;
			} else if (nodeName.equals("record")) {
			} else if (nodeName.equals("object")) {
			} else if (nodeName.equals("subdialog")) {
				variableVxml.setValue(nodeItemVariablesName.get(selectedItem),
						"'defined'", getNodeScope(selectedItem));
			} else if (nodeName.equals("transfer")) {
			} else if (nodeName.equals("initial")) {
			} else if (nodeName.equals("block")) {

				variableVxml.setValue(nodeItemVariablesName.get(selectedItem),
						"'defined'", getNodeScope(selectedItem));
				execute(selectedItem);

				variableVxml.resetScope(0);
			}
			// FIXME: remove 0

			// System.out.println("After selection ["+
			// variables.get(selectedItem)+"]"+" n°"
			// +i+++" "+currentNodeVariables);
			// TODO: PHASE PROCESS
		}
	}

	public void declareVariable(NodeList nodeList)
			throws FileNotFoundException, DOMException, ScriptException {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeName().equals("var")
					|| VxmlElementType.isFormItem(node)) {
				String declareVariable = variableVxml.declareVariable(node,
						VxmlElementType.isFormItem(node) ? 5
								: getNodeScope(node));
				if (VxmlElementType.isFormItem(node)) {
					nodeItemVariablesName.put(node, declareVariable);
				}
			} else if (node.getNodeName().equals("script")) {
				variableVxml.evaluateScript(node, getNodeScope(node));
			}
		}
	}

	public void declareVariable(NodeList nodeList, int scope)
			throws FileNotFoundException, DOMException, ScriptException {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeName().equals("var")
					|| VxmlElementType.isFormItem(node)) {
				String declareVariable = variableVxml.declareVariable(node,
						scope);
				if (VxmlElementType.isFormItem(node)) {
					nodeItemVariablesName.put(node, declareVariable);
				}
			} else if (node.getNodeName().equals("script")) {
				variableVxml.evaluateScript(node, scope);
			}
		}
	}

	public void execute(Node node) throws InterpreterException, ScriptException {
		NodeList child = node.getChildNodes();
		for (int i = 0; i < child.getLength(); i++) {
			Node node1 = child.item(i);
			NodeExecutor executor = nodeExecution.get(node1.getNodeName());

			if (node1.getNodeName().equals("filled")) {
				return;
			}

			if (null != executor) {
				executor.execute(node1);
			}

			if (nextItemSelectGuard) {
				nextItemSelectGuard = false;
				return;
			}
		}
	}

	private void collectPrompt(Node node) {
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
				} else if (currentDialogProperties.get("bargeintype") != null) {
					p.bargeinType = currentDialogProperties
							.getProperty("bargeintype");
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
					try {
						addPromptWithValue(child, p);
					} catch (DOMException e) {
						e.printStackTrace();
					} catch (ScriptException e) {
						e.printStackTrace();
					}
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

	private void addPromptWithValue(Node value, Prompt p) throws DOMException,
			ScriptException {
		assert (value.getNodeName().endsWith("value"));
		p.tts += variableVxml.getValue(value.getAttributes().item(0)
				.getNodeValue(), 0);
	}

	private void clearVariable(Node node1) {
		String[] nameList = node1.getAttributes().getNamedItem("namelist")
				.getNodeValue().split(" ");

		for (int i = 0; i < nameList.length; i++) {
			try {
				variableVxml.setValue(nameList[i], "'undefined'",
						getNodeScope(node1));
			} catch (ScriptException e) {
				e.printStackTrace();
			}
		}
	}

	private void assignVariableValue(Node node1) {

		try {
			String nodeName = node1.getAttributes().getNamedItem("name")
					.getNodeValue();
			Node expr = node1.getAttributes().getNamedItem("expr");

			variableVxml.setValue(nodeName, expr.getNodeValue(),
					getNodeScope(node1));
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			e.printStackTrace();
		}
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

	private void collectTrace(Node node) {
		String value = getNodeValue(node);
		traceLog.add(value);
		for (int j = 0; j < node.getAttributes().getLength(); j++) {
			traceStat.add("[" + node.getAttributes().item(j).getNodeName()
					+ ":" + node.getAttributes().item(j).getNodeValue() + "] "
					+ node.getTextContent());
		}
	}

	private String getNodeValue(Node node) {
		NodeList childs = node.getChildNodes();
		String value = "";

		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			if ("value".equals(child.getNodeName()))
				value += variableVxml.getValue(child.getAttributes()
						.getNamedItem("expr").getNodeValue(),
						getNodeScope(child));
			else
				value += child.getNodeValue();
		}
		return value;
	}

	private void checkConditionAndExecute(Node node)
			throws InterpreterException, ScriptException {
		boolean conditionChecked = this.checkCond(node);
		NodeList childs = node.getChildNodes();

		for (int i = 0; i < childs.getLength(); i++) {
			Node item = childs.item(i);
			if (isAnExecutableItem(item)) { // FIXME: Déplacer dans VxmlElement
				// la fonction isAnExecutableItem
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

	private boolean isAnExecutableItem(Node item) {
		return nodeExecution.get(item.getNodeName()) != null;
	}

	private boolean isAnAnonymeContext(Node node) {
		return VxmlElementType.isFormItem(node.getParentNode());
	}

	private boolean isAnDialogContext(Node node) {
		return VxmlElementType.isADialog(node.getParentNode());
	}

	private int getNodeScope(Node node) {
		Node tmp =VxmlElementType.isConditionalItem(node)?node.getParentNode():node;
		if (isAnAnonymeContext(tmp))
			return 0;
		else if (isAnDialogContext(tmp))
			return 1;
		else if (node.getParentNode().getNodeName().equals("vxml"))
			return 2;
		return 5;
	}

	private boolean checkCond(Node node) {
		NamedNodeMap attribute = node.getAttributes();
		Node cond = (attribute.getLength() == 0) ? null : attribute
				.getNamedItem("cond");
		try {
			return cond == null
					|| variableVxml.evaluateScript(cond, getNodeScope(node))
							.equals("true");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
		return false;
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
		currentDialogProperties.put(attributes.getNamedItem("name")
				.getNodeValue(), attributes.getNamedItem("value")
				.getNodeValue());
	}

	private Node phaseSelect(Node dialog) {
		return (selectedItem != null && variableVxml.getValue(
				nodeItemVariablesName.get(selectedItem),
				getNodeScope(selectedItem)).equals("undefined")) ? selectedItem
				: unsatisfiedGuardCondition(dialog.getChildNodes());
	}

	private Node unsatisfiedGuardCondition(NodeList nodeList) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);

			if (VxmlElementType.isFormItem(node)) {
				if (nodeItemVariablesName.get(node) != null
						&& variableVxml.getValue(
								nodeItemVariablesName.get(node), 1).equals(
								"undefined") && this.checkCond(node)) {
					return node;
				}
			}
		}
		return null;
	}
}