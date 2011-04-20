package vxml.interpreter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import vxml.interpreter.execption.DisconnectException;
import vxml.interpreter.execption.ExitException;
import vxml.interpreter.execption.GotoException;
import vxml.interpreter.execption.InterpreterExecption;
import vxml.interpreter.execption.SubmitException;

public class Interpreter {
	public List<VariableVxml> currentNodeVariables = new ArrayList<VariableVxml>();
	private List<String> traceLog = new ArrayList<String>();
	private List<String> traceStat = new ArrayList<String>();
	private List<Prompt> prompts = new ArrayList<Prompt>();
	private Map<Node, Integer> promptCounter;
	public Map<Node, VariableVxml> variables = new Hashtable<Node, VariableVxml>();
	private int countFormItemVariable = 0;
	public Node selectedItem;

	private Hashtable<String, NodeExecutor> nodeExecution = new Hashtable<String, NodeExecutor>() {
		{
			// TODO: implement interpretation of: reprompt,
			// return,
			// script

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
					System.err
							.println("TODO: implement reprompt interpretation");
				}
			});
			put("return", new NodeExecutor() {
				public void execute(Node node) {
					// FIXME: replace assertion by error.semantic
					assert (node.getParentNode().getNodeName()
							.equals("subdialog"));
					System.err.println("TODO: implement return interpretation");
				}
			});
			put("disconnect", new NodeExecutor() {
				public void execute(Node node) throws DisconnectException {
					throw new DisconnectException();
				}
			});
			put("script", new NodeExecutor() {
				public void execute(Node node) {
					System.err.println("TODO: implement script interpretation");
				}
			});
			put("var", new NodeExecutor() {
				public void execute(Node node) {
					declareVariable(node);
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
				public void execute(Node node) throws InterpreterExecption {
					checkConditionAndExecute(node);
				}
			});
			put("goto", new NodeExecutor() {
				public void execute(Node node) {
					selectedItem = selectDialogNextItemTovisit(node);
				}

			});
			put("submit", new NodeExecutor() {
				public void execute(Node node) throws GotoException,
						SubmitException {

					throw new SubmitException();
				}

			});
			put("log", new NodeExecutor() {
				public void execute(Node node) {
					collectTrace(node);
				}
			});
			put("exit", new NodeExecutor() {
				public void execute(Node node) throws ExitException {
					throw new ExitException();
				}
			});
		}
	};

	public void interpretDialog(Node dialog) throws InterpreterExecption {
		// PHASE INITIALIZATION
		NodeList nodeList = dialog.getChildNodes();
		initialization(nodeList);

		// TODO: Si l'utlisateur est rentré an prononçant une phrase de la
		// grammaire dans un autre formulaire , aller directement à la phase de
		// traitement

		// Boucle principale
		while (true) {
			// PHASE SELECTION
			if ((selectedItem = phaseSelect(dialog)) == null) {
				break;
			}

			// TODO: Mettre en file d'attente les invites de l'élément de
			// formulaire.
			// ---> Activer les grammaires de l'élément de formulaire.

			// Exécuter l'élément de formulaire.

			// TODO: Mettre des nodes executor et selector
			String nodeName = selectedItem.getNodeName();
			if (nodeName.equals("field")) {
				variables.get(selectedItem).value = "defined";
			} else if (nodeName.equals("record")) {
			} else if (nodeName.equals("object")) {
			} else if (nodeName.equals("subdialog")) {
			} else if (nodeName.equals("transfer")) {
			} else if (nodeName.equals("initial")) {
			} else if (nodeName.equals("block")) {
				variables.get(selectedItem).value = "defined";
				execute(selectedItem);
			}

			// TODO: PHASE PROCESS
		}
	}

	private void initialization(NodeList nodeList) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeName().equals("var")) {
				declareVariable(node);
			} else if (node.getNodeName().equals("script")) {
				// TODO : Interpreter les scripts
			} else if (VxmlElement.isFormItem(node)) {
				declareVariable(node);
				NodeList child = node.getChildNodes();
				for (int j = 0; j < child.getLength(); j++) {
					Node item = child.item(j);
					if (VxmlElement.isInputItem(item)
							|| item.getNodeName().equals("initial")) {
						promptCounter.put(item, 1);
					}
				}
			}
		}
	}

	private Node phaseSelect(Node dialog) {
		return (selectedItem != null && variables.get(selectedItem).value
				.equals("undefined")) ? selectedItem
				: unsatisfiedGuardCondition(dialog.getChildNodes());
	}

	private Node unsatisfiedGuardCondition(NodeList nodeList) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (VxmlElement.isFormItem(node)) {
				if (variables.get(node) != null
						&& variables.get(node).value.equals("undefined")) {
					return node;
				}
			}
		}
		return null;
	}

	public void execute(Node node) throws InterpreterExecption {
		NodeList child = node.getChildNodes();
		for (int i = 0; i < child.getLength(); i++) {
			Node node1 = child.item(i);
			nodeExecution.get(node1.getNodeName()).execute(node1);
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
			}

			NodeList childs = node.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++) {
				Node child = childs.item(i);

				if (child.getNodeName().equals("audio")) {
					NamedNodeMap attributesAudio = child.getAttributes();
					Node src = attributesAudio.getNamedItem("src");
					if (src != null) {
						p.audio += src.getNodeValue()+" ";
					}
					if (child.getChildNodes().getLength() > 0)
						p.tts += child.getChildNodes().item(0).getNodeValue()+" ";
				} else if (child.getNodeName().equals("#text")) {
					setPromtTextToSpeech(child, p);
				}
			}
		}

		prompts.add(p);
	}

	private void collectSimpleSpeechPrompt(Node node) {
		Prompt p = new Prompt();
		setPromtTextToSpeech(node, p);
		if (p.tts != "")
			prompts.add(p);

	}

	private void setPromtTextToSpeech(Node node, Prompt p) {
		String nodeValue = node.getNodeValue().trim();
		if (nodeValue != null && !nodeValue.equals(" ")
				&& nodeValue.length() > 0) {
			p.tts += nodeValue.replaceAll("@", " ")+" ";
		}
	}

	private void clearVariable(Node node1) {
		String[] nameList = node1.getAttributes().getNamedItem("namelist")
				.getNodeValue().split(" ");

		for (int i = 0; i < nameList.length; i++) {
			VariableVxml variableVxml = searchVariableWithName(nameList[i]);
			if (variableVxml != null) {
				variableVxml.value = VariableVxml.DEFAULT_VARIABLE_VALUE;
			}
		}
	}

	private void assignVariableValue(Node node1) {
		VariableVxml variableVxml = searchVariableWithName(node1
				.getAttributes().getNamedItem("name").getNodeValue());
		if (variableVxml != null) {
			variableVxml.value = node1.getAttributes().getNamedItem("expr")
					.getNodeValue().replace("'", "");
		}
	}

	private VariableVxml searchVariableWithName(String nodeValue) {
		for (Iterator<VariableVxml> varIterator = currentNodeVariables
				.iterator(); varIterator.hasNext();) {
			VariableVxml variableVxml = (VariableVxml) varIterator.next();
			if (variableVxml.name.equals(nodeValue))
				return variableVxml;
		}
		return null;
	}

	private void declareVariable(Node node) {
		NamedNodeMap attributes = node.getAttributes();

		String name = null;
		if (attributes.getLength() > 0) {
			name = attributes.getNamedItem("name").getNodeValue();
		}
		if (name == null) {
			name = node.getNodeName() + "_" + countFormItemVariable++;
		}

		Node value = attributes.getNamedItem("expr");
		// FIXME: Variables dupliqués a deux endroit differents
		currentNodeVariables.add(new VariableVxml(name,
				value == null ? "undefined" : value.getNodeValue().replaceAll(
						"'(.*)'", "$1")));
		variables.put(node, new VariableVxml(name, value == null ? "undefined"
				: value.getNodeValue().replaceAll("'(.*)'", "$1")));
	}

	public Node selectDialogNextItemTovisit(Node node) {
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

	private void collectTrace(Node node) {
		traceLog.add(node.getTextContent());
		for (int j = 0; j < node.getAttributes().getLength(); j++) {
			traceStat.add("[" + node.getAttributes().item(j).getNodeName()
					+ ":" + node.getAttributes().item(j).getNodeValue() + "] "
					+ node.getTextContent());
		}
	}

	private void checkConditionAndExecute(Node node)
			throws InterpreterExecption {
		boolean conditionChecked = checkCond(node);
		NodeList childs = node.getChildNodes();

		for (int i = 0; i < childs.getLength(); i++) {
			Node item = childs.item(i);
			if (isAnExecutableItem(item)) { // FIXME: Déplacer dans VxmlElement
				// la fonction isAnExecutableItem
				if (conditionChecked) {
					nodeExecution.get(item.getNodeName()).execute(item);
				}
			} else if (VxmlElement.isConditionalItem(item)) {
				if (conditionChecked) {
					break;
				} else {
					conditionChecked = checkCond(item);
				}
			}
		}
	}

	private boolean checkCond(Node node) {
		NamedNodeMap attribute = node.getAttributes();
		Node cond = (attribute.getLength() == 0) ? null : attribute
				.getNamedItem("cond");
		return cond == null || cond.getNodeValue().equals("true");
	}

	private boolean isAnExecutableItem(Node item) {
		return nodeExecution.get(item.getNodeName()) != null;
	}

	public List<String> getTraceLog() {
		return traceLog;
	}

	public List<String> getTraceStat() {
		return traceStat;
	}

	public List<Prompt> getPrompts() {
		return prompts;
	}
}
