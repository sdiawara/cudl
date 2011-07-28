package cudl;

import static cudl.utils.Utils.getNodeAttributeValue;
import static cudl.utils.VxmlElementType.isFormItem;
import static cudl.utils.VxmlElementType.isInputItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.script.ScriptException;
import javax.xml.parsers.ParserConfigurationException;

import org.mozilla.javascript.Undefined;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cudl.script.InterpreterScriptContext;
import cudl.script.InterpreterVariableDeclaration;
import cudl.utils.Utils;
import cudl.utils.VxmlElementType;

class FormInterpreationAlgorithm /* TODO: make it observer */{
	private final WIPContext context; // make it observable
	private final InterpreterVariableDeclaration declaration;
	private int form_item_generated;
	private Map<String, Integer> promptCounter = new Hashtable<String, Integer>();
	private String gotoNextItem;
	private boolean lastIteraionEndWithGotoNextItem;
	private Map<Node, String> formItemNames = new LinkedHashMap<Node, String>();

	Executor executor;

	FormInterpreationAlgorithm(WIPContext context,
			InterpreterVariableDeclaration declaration) {
		this.context = context;
		this.declaration = declaration;
		this.executor = new Executor(context, declaration);
	}

	void initializeDialog(Node dialog) throws ScriptException, IOException {
		form_item_generated = 0;
		lastIteraionEndWithGotoNextItem = false;
		formItemNames.clear();

		NodeList childNodes = dialog.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			String name = getNodeAttributeValue(child, "name");
			String expr = getNodeAttributeValue(child, "expr");
			if ("var".equals(getName(child))) {
				declaration.declareVariable(name, expr == null ? "undefined"
						: expr, InterpreterScriptContext.DIALOG_SCOPE);
			} else if ("script".equals(getName(child))) {
				String src = getNodeAttributeValue(child, "src");
				// FIXME: server uri
				if (src == null) {
					declaration.evaluateScript(child.getTextContent(),
							InterpreterScriptContext.DIALOG_SCOPE);
				} else {
					declaration.evaluateFileScript(src,
							InterpreterScriptContext.DIALOG_SCOPE);
				}
			} else if (isFormItem(child)) {
				if (name == null) {
					name = "form_item_generated_name_by_cudl_"
							+ form_item_generated++;
				}
				declaration.declareVariable(name, expr == null ? "undefined"
						: expr, InterpreterScriptContext.DIALOG_SCOPE);

				if (isInputItem(child) || "initial".equals(getName(child))) {
					promptCounter.put(name, 1);
				}
				formItemNames.put(child, name);
			}
		}
	}

	void mainLoop() throws ScriptException, IOException, InterpreterException,
			ParserConfigurationException, SAXException {
		while (true) {
			// PHASE SELECT
			Node formItem = selectNextFormItem();
			if (formItem == null) {
				context.setHangup(true);
				break;
			}
			context.setSelectedFormItem(formItem);

			// Mettre en file d'attente les invites de l'élément de formulaire.
			// List<Node> promptsQueue = new ArrayList<Node>();
			// NodeList childNodes = formItem.getChildNodes();
			// if (isInputItem(formItem) || "initial".equals(getName(formItem)))
			// {
			// if (reprompt && !context.getCurrentChange()) {
			// // NodeList childNodes = childNodes;
			// promptsQueue = selectedPrompt(formItem, childNodes);
			// promptCounter.put(formItemNames.get(formItem),
			// (promptCounter.get(formItem) + 1));
			// }
			// }

			// Activer les grammaires de l'élément de formulaire.
			activateGrammar();

			// Exécuter l'élément de formulaire.
			if ("subdialog".equals(getName(formItem))) {
				String src = getNodeAttributeValue(formItem, "src");
				InternalInterpreter internalInterpreter = null;
				if (src != null) {
					internalInterpreter = new InternalInterpreter(context
							.getCurrentFileName());
					setSubdialogRequierement(src, internalInterpreter);
					NodeList childNodes = formItem.getChildNodes();
					declareParams(internalInterpreter, childNodes);
					internalInterpreter.interpretDialog();
					internalInterpreter.mainLoop();
					// FIXME: add log
					System.err.println(executor.prompts);
					executor.prompts.addAll(internalInterpreter.getPrompts());
				}
				declaration.evaluateScript(formItemNames.get(formItem)
						+ "=new Object();",
						InterpreterScriptContext.DIALOG_SCOPE);
				if (internalInterpreter != null) {
					String returnValue = internalInterpreter.getContext()
							.getReturnValue();
					System.err.println("return value" + returnValue);
					StringTokenizer tokenizer = new StringTokenizer(returnValue);
					while (tokenizer.hasMoreElements()) {
						String variable = tokenizer.nextToken();
						InterpreterVariableDeclaration declaration2 = internalInterpreter
								.getContext().getDeclaration();
						declaration.evaluateScript(formItemNames.get(formItem)
								+ "." + variable + "='"
								+ declaration2.getValue(variable) + "'",
								InterpreterScriptContext.ANONYME_SCOPE);
					}
				}

				Node filled = Utils.serachItem(formItem, "filled");
				if (filled != null)
					executor.execute(filled);
			} else if ("transfer".equals(getName(formItem))) {
				String dest = getNodeAttributeValue(formItem, "dest");
				String destExpr = getNodeAttributeValue(formItem, "destexpr");
				context.setTransfertDestination((dest != null) ? dest
						: declaration.evaluateScript(destExpr, 50) + "");
				throw new TransferException();
			} else if ("field".equals(getName(formItem))) {
				executor.execute(formItem);
			} else if ("block".equals(getName(formItem))) {
				declaration.setValue(formItemNames.get(formItem), "true",
						InterpreterScriptContext.DIALOG_SCOPE);
				try {
					executor.execute(formItem);
				} catch (GotoException e) {
					if (e.nextItem != null) {
						gotoNextItem = e.nextItem;
						lastIteraionEndWithGotoNextItem = true;
					} else {
						declaration
								.resetScopeBinding(InterpreterScriptContext.DIALOG_SCOPE);
						declaration
								.resetScopeBinding(InterpreterScriptContext.ANONYME_SCOPE);
						int indexOf = e.next.indexOf("#");
						if (indexOf >= 0)
							context.setNexted(e.next.substring(indexOf + 1));
						throw new GotoException(e.next, e.nextItem);
					}
				} catch (SubmitException e) {
					throw new SubmitException(e.next);
				} catch (ExitException e) {
					return;
				}
			} else {
				throw new RuntimeException(formItem.getNodeName()
						+ " non traité");
			}
		}
	}

	private void declareParams(InternalInterpreter internalInterpreter,
			NodeList childNodes) throws ScriptException {
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item.getNodeName().equals("param")) {
				String name = getNodeAttributeValue(item, "name");
				String value = getNodeAttributeValue(item, "expr");
				internalInterpreter.getContext().addParam(name);
				System.err.println("Param " + name + "\t"
						+ declaration.evaluateScript(value, 50));
				internalInterpreter.getContext().getDeclaration()
						.declareVariable(
								name,
								"'" + declaration.evaluateScript(value, 50)
										+ "'",
								InterpreterScriptContext.ANONYME_SCOPE);
			}
		}
	}

	private void setSubdialogRequierement(String src,
			InternalInterpreter internalInterpreter) throws ScriptException,
			IOException, SAXException {
		String[] split = src.split("#");

		if (split.length == 2)
			internalInterpreter.getContext().setCurrentDialog(
					Utils.searchDialogByName(context.getCurrentDialog()
							.getParentNode().getChildNodes(), split[1]));
		if (!split[0].equals(""))
			internalInterpreter.getContext().buildDocument(split[0]);
	}

	// private void playPrompts(NodeList childNodes) throws
	// InterpreterException,
	// ScriptException, IOException {
	// for (int i = 0; i < childNodes.getLength(); i++) {
	// Node child = childNodes.item(i);
	// String nodeName = child.getNodeName();
	// if ("prompt".equals(nodeName) || "#text".equals(nodeName)) {
	// executor.nodeExecutors.get(nodeName).execute(child);
	// }
	// }
	// }

	Node selectNextFormItem() throws ScriptException {
		Node node = null;
		if (lastIteraionEndWithGotoNextItem) {
			node = Utils.searchItemByName(context.getCurrentDialog(),
					gotoNextItem);
			gotoNextItem = null;
			lastIteraionEndWithGotoNextItem = false;
		} else {
			for (Iterator<Entry<Node, String>> iterator = formItemNames
					.entrySet().iterator(); iterator.hasNext();) {
				Entry<Node, String> next = iterator.next();
				Node nextToVisit = next.getKey();
				String name = next.getValue();
				String cond = Utils.getNodeAttributeValue(nextToVisit, "cond");
				if (cond == null
						|| (Boolean) declaration.evaluateScript(cond, 50))
					if (declaration.getValue(name).equals(Undefined.instance)) {
						return nextToVisit;
					}
			}
		}

		return node;
	}

	List<Log> getLogs() {
		return executor.logs;
	}

	List<Prompt> getPrompts() {
		return executor.prompts;
	}

	// private List<Node> selectedPrompt(Node formItem, NodeList childNodes) {
	// List<Node> promptsQueue = new ArrayList<Node>();
	// for (int i = 0; i < childNodes.getLength(); i++) {
	// Node item = childNodes.item(i);
	// if ("prompt".equals(getName(item))) {
	// String countAtt = getNodeAttributeValue(item, "count");
	// if (countAtt != null
	// && Integer.parseInt(countAtt) <= promptCounter
	// .get(formItemNames.get(formItem))) {
	// promptsQueue.add(item);
	// } else if (countAtt == null) {
	// promptsQueue.add(item);
	// }
	// }
	// }
	// return promptsQueue;
	// }

	private void activateGrammar() {
		List<Node> grammarActive = new ArrayList<Node>();
		if (VxmlElementType.isInputItem(context.getSelectedFormItem()))
			if (VxmlElementType.isAModalItem(context.getSelectedFormItem())) {
				grammarActive.add(Utils.serachItem(context
						.getSelectedFormItem(), "grammar"));
			} else {
				Node parent = context.getSelectedFormItem();
				while (null != parent) {
					Node serachItem = Utils.serachItem(parent, "grammar");
					if (null != serachItem) {
						grammarActive.add(serachItem);
					}
					parent = parent.getParentNode();
				}
			}
		context.setGrammarActive(grammarActive);
	}

	private String getName(Node child) {
		return child.getNodeName();
	}

	String getFormItemName(Node selectedFormItem) {
		return formItemNames.get(selectedFormItem);
	}
}