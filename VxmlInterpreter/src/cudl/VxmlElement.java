package cudl;

import static cudl.utils.Utils.getNodeAttributeValue;
import static cudl.utils.VxmlElementType.isFormItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.mozilla.javascript.Undefined;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cudl.script.InterpreterVariableDeclaration;
import cudl.utils.Utils;
import cudl.utils.VxmlElementType;

interface VxmlElement {
	Object interpret(InterpreterContext context) throws InterpreterException, IOException,
			SAXException;
}

class FormTag extends DefaultVxmlElementInterpreter {
	private static final String initChild = "var script";
	private int form_item_generated = 0;
	private String formItems = "block transfer subdialog field";
	private boolean initVar = true;

	public FormTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException,
			SAXException {
		// Phase initialization
		if (initVar) {
			System.err.println("Begin Phase initialization");
			NodeList childs = node.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++) {
				Node child = childs.item(i);
				String nodeName = child.getNodeName();
				if (initChild.contains(nodeName)) {
					TagInterpreterFactory.getTagInterpreter(child, context).interpret(context);
				} else if (isFormItem(child)) {
					String name = getNodeAttributeValue(child, "name");
					String expr = getNodeAttributeValue(child, "expr");
					if (name == null) {
						name = "form_item_generated_name_by_cudl_" + form_item_generated++;
					}
					context.getDeclaration()
							.declareVariable(name, expr == null ? "undefined" : expr, 60);
					// if (isInputItem(child) || "initial".equals(getName(child))) {
					// promptCounter.put(name, 1);
					// }
					context.addFormItemName(child, name);
				}
			}
			System.err.println("End Phase initialization");
		}

		System.err.println("Enter Main loop");
		while (true) {
			Node formItem = selectNextFormItem();
			if (formItem == null) {
				context.setHangup(true);
				break;
			}
			context.setSelectedFormItem(formItem);

			activateGrammar();

			if (formItems.contains(formItem.getNodeName())) {
				try {
					TagInterpreterFactory.getTagInterpreter(formItem, context).interpret(context);
					context.getDeclaration().resetScopeBinding(
							InterpreterVariableDeclaration.ANONYME_SCOPE);
				} catch (ExitException e) {
					context.setHangup(true);
					return null;
				} catch (GotoException e) {
					context.setNextItemToVisit(e.nextItem);
					if (e.next != null) {
						context.getDeclaration().resetScopeBinding(
								InterpreterVariableDeclaration.DIALOG_SCOPE);
						throw new GotoException(e.next, null);
					}
				}
			}
		}
		return null;
	}

	private void activateGrammar() {
		List<Node> grammarActive = new ArrayList<Node>();
		if (VxmlElementType.isInputItem(context.getSelectedFormItem()))
			if (VxmlElementType.isAModalItem(context.getSelectedFormItem())) {
				grammarActive.add(Utils.serachItem(context.getSelectedFormItem(), "grammar"));
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

	Node selectNextFormItem() {
		Node node = null;
		if (context.getNextItemToVisit() != null) {
			node = Utils.searchItemByName(context.getCurrentDialog(), context.getNextItemToVisit());
			context.setNextItemToVisit(null);
		} else {
			for (Iterator<Entry<Node, String>> iterator = context.getFormItemNames().entrySet()
					.iterator(); iterator.hasNext();) {
				Entry<Node, String> next = iterator.next();
				Node nextToVisit = next.getKey();
				String name = next.getValue();
				String cond = Utils.getNodeAttributeValue(nextToVisit, "cond");
				if (cond == null || (Boolean) context.getDeclaration().evaluateScript(cond, 50))
					if (context.getDeclaration().getValue(name).equals(Undefined.instance)) {
						return nextToVisit;
					}
			}
		}
		return node;
	}

	public void setInitVar(boolean initVar) {
		this.initVar = initVar;
	}
}

class MenuTag extends DefaultVxmlElementInterpreter {
	private String execute = "prompt";

	public MenuTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException,
			SAXException {
		context.setSelectedFormItem(node);
		System.err.println("BEGIN MENU interpretation");
		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node item = childs.item(i);
			if (execute.contains(item.getNodeName()))
				TagInterpreterFactory.getTagInterpreter(item, context).interpret(context);
		}
		return null;
	}
}

class VarTag extends DefaultVxmlElementInterpreter {
	public VarTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) {
		String name = Utils.getNodeAttributeValue(node, "name");
		if (!context.getParams().contains(name)) {
			String expr = Utils.getNodeAttributeValue(node, "expr");
			context.getDeclaration().declareVariable(name, expr == null ? "undefined" : expr,
					InterpreterVariableDeclaration.ANONYME_SCOPE);
			System.err.println("Declare variable " + name + " = " + expr);
		}
		return null;
	}
}

class LogTag extends DefaultVxmlElementInterpreter {
	public LogTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException,
			SAXException {
		Log log = new Log();

		String label = getNodeAttributeValue(node, "label");
		System.err.print("LOG:");
		if (label != null) {
			log.label = label;
			System.err.print(" " + label + " ");
		}

		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			log.value += TagInterpreterFactory.getTagInterpreter(child, context).interpret(context);
			System.err.println(log.value);
		}

		context.getLogs().add(log);
		return null;
	}
}

class PromptTag extends DefaultVxmlElementInterpreter {
	String prompt = "audio";

	public PromptTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException,
			SAXException {
		Prompt p = new Prompt();
		String timeout = getNodeAttributeValue(node, "timeout");
		p.timeout = timeout != null ? timeout : "";

		String bargein = getNodeAttributeValue(node, "bargein");
		p.bargein = bargein != null ? bargein : "";

		String bargeinType = getNodeAttributeValue(node, "bargeintype");
		// bargeinType = (String) (bargeinType == null ?
		// context.geta.get("bargeintype")
		// : bargeinType);
		p.bargeinType = bargeinType != null ? bargeinType : "";

		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			if (prompt.contains(child.getNodeName())) {
				Prompt pc = (Prompt) TagInterpreterFactory.getTagInterpreter(child, context).interpret(
						context);
				p.audio += pc.audio + " ";
				p.tts += pc.tts + " ";
			} else {
				p.tts += TagInterpreterFactory.getTagInterpreter(child, context).interpret(context);
				System.err.println(p.tts + " ***");
			}
		}

		p.tts = p.tts.trim();
		p.audio = p.audio.trim();
		context.addPrompt(p);
		return null;
	}
}

class ExitTag extends DefaultVxmlElementInterpreter {
	public ExitTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws ExitException {
		context.setHangup(true);
		throw new ExitException();
	}
}

class AssignTag extends DefaultVxmlElementInterpreter {
	public AssignTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) {
		String name = getNodeAttributeValue(node, "name");
		String expr = getNodeAttributeValue(node, "expr");
		context.getDeclaration().setValue(name, expr, 50);
		return null;
	}
}

class ClearTag extends DefaultVxmlElementInterpreter {
	public ClearTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) {
		String namelist = getNodeAttributeValue(node, "namelist");
		if (namelist != null) {
			StringTokenizer tokenizer = new StringTokenizer(namelist);
			while (tokenizer.hasMoreElements()) {
				String name = (String) tokenizer.nextElement();
				context.getDeclaration().setValue(name, "undefined", 50);
			}
		}
		// else clear set formItem to undefined
		return null;
	}
}

class IfTag extends DefaultVxmlElementInterpreter {
	public IfTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws IOException, InterpreterException,
			SAXException {
		boolean conditionChecked = checkCond(node);
		NodeList childs = node.getChildNodes();
		System.err.println("cond evaluation " + conditionChecked);
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			if (VxmlElementType.isAnExecutableItem(child)) {
				if (conditionChecked) {
					TagInterpreterFactory.getTagInterpreter(child, context).interpret(context);
				}
			} else if (VxmlElementType.isConditionalItem(child)) {
				if (conditionChecked) {
					break;
				} else {
					conditionChecked = this.checkCond(child);
				}
			}
		}
		return null;
	}

	private boolean checkCond(Node node) throws IOException {
		String cond = getNodeAttributeValue(node, "cond");

		return cond == null
				|| ((Boolean) context.getDeclaration().evaluateScript(cond,
						InterpreterVariableDeclaration.ANONYME_SCOPE));
	}
}

class TextTag extends DefaultVxmlElementInterpreter {
	public TextTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) {
		String textContent = node.getTextContent();
		if (VxmlElementType.isFormItem(node.getParentNode()) && !textContent.trim().equals("")) {
			Prompt p = new Prompt();
			p.tts += textContent.trim();
			context.addPrompt(p);
		}
		return textContent;
	}
}

class CommentTag extends DefaultVxmlElementInterpreter {
	public CommentTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) {
		return "";
	}
}

class GotoTag extends DefaultVxmlElementInterpreter {
	public GotoTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException {
		String nextItemAtt = getNodeAttributeValue(node, "nextitem");
		String next = getNodeAttributeValue(node, "next");

		if (nextItemAtt == null)
			nextItemAtt = getNodeAttributeValue(node, "expritem");
		if (next == null)
			next = getNodeAttributeValue(node, "expr");
		throw new GotoException(next, nextItemAtt);
	}
}

class SubmitTag extends DefaultVxmlElementInterpreter {
	public SubmitTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException {

		String next = getNodeAttributeValue(node, "next");

		String nameList = getNodeAttributeValue(node, "namelist");
		if (nameList != null) {
			StringTokenizer tokenizer = new StringTokenizer(nameList);
			String urlSuite = "?";
			while (tokenizer.hasMoreElements()) {
				String data = tokenizer.nextToken();
				urlSuite += data + "=" + context.getDeclaration().getValue(data) + "&";
			}
			next += urlSuite;
		}
		throw new SubmitException(next);
	}
}

class ReturnTag extends DefaultVxmlElementInterpreter {
	public ReturnTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException {
		System.err.println(getNodeAttributeValue(node, "namelist"));
		throw new ReturnException(getNodeAttributeValue(node, "namelist"));
	}
}

class DisconnectTag extends DefaultVxmlElementInterpreter {
	public DisconnectTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException {
		throw new EventException("connection.disconnect.hangup");
	}
}

class ValueTag extends DefaultVxmlElementInterpreter {
	public ValueTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) {
		return context.getDeclaration().getValue(getNodeAttributeValue(node, "expr"));
	}
}

class ThrowTag extends DefaultVxmlElementInterpreter {
	public ThrowTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException {
		System.err.println(getNodeAttributeValue(node, "event") + " throw");
		throw new EventException(getNodeAttributeValue(node, "event"));
	}
}

class FilledTag extends DefaultVxmlElementInterpreter {
	private boolean execute = false;

	public FilledTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException,
			SAXException {
		if (!execute)
			throw new FilledException(node.getParentNode());
		return super.interpret(context);
	}

	public void setExecute(boolean execute) {
		this.execute = execute;
	}
}

class ScriptTag extends DefaultVxmlElementInterpreter {
	public ScriptTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws IOException {
		String src = getNodeAttributeValue(node, "src");
		if (src == null) {
			context.getDeclaration().evaluateScript(node.getTextContent(),
					InterpreterVariableDeclaration.DIALOG_SCOPE);
		} else {
			context.getDeclaration().evaluateFileScript(src,
					InterpreterVariableDeclaration.DIALOG_SCOPE);
		}
		return null;
	}
}

class BlockTag extends DefaultVxmlElementInterpreter {
	public BlockTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException,
			SAXException {
		context.getDeclaration().setValue(context.getFormItemNames().get(node), "true",
				InterpreterVariableDeclaration.DIALOG_SCOPE);
		return super.interpret(context);
	}
}

class SubdialogTag extends DefaultVxmlElementInterpreter {
	public SubdialogTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException,
			SAXException {
		context.getDeclaration().setValue(context.getFormItemNames().get(node), "true",
				InterpreterVariableDeclaration.DIALOG_SCOPE);
		String src = getNodeAttributeValue(node, "src");
		InternalInterpreter internalInterpreter = null;
		if (src != null) {
			InterpreterContext subdialogContext;
			try {
				String location = Utils.tackWeelFormedUrl(context.getLocation(), src);
				subdialogContext = new InterpreterContext(location, new InterpreterVariableDeclaration(
						location));
				internalInterpreter = new InternalInterpreter(subdialogContext);
				declareParams(internalInterpreter, node.getChildNodes());
				internalInterpreter.interpret(subdialogContext);
			} catch (ParserConfigurationException e1) {
				throw new RuntimeException(e1);
			}
			context.getLogs().addAll(internalInterpreter.getContext().getLogs());
			context.getPrompts().addAll(internalInterpreter.getContext().getPrompts());
		}
		context.getDeclaration().evaluateScript(
				context.getFormItemNames().get(node) + "=new Object();",
				InterpreterVariableDeclaration.DIALOG_SCOPE);
		if (internalInterpreter != null) {
			String returnValue = internalInterpreter.getContext().getReturnValue();
			System.err.println("return value " + returnValue);
			StringTokenizer tokenizer = new StringTokenizer(returnValue);
			while (tokenizer.hasMoreElements()) {
				String variable = tokenizer.nextToken();
				InterpreterVariableDeclaration declaration2 = internalInterpreter.getContext()
						.getDeclaration();
				context.getDeclaration().evaluateScript(
						context.getFormItemNames().get(node) + "." + variable + "='"
								+ declaration2.getValue(variable) + "'",
						InterpreterVariableDeclaration.ANONYME_SCOPE);
			}
			Node filled = Utils.serachItem(node, "filled");
			if (filled != null) {
				FilledTag tagInterpreter = (FilledTag) TagInterpreterFactory.getTagInterpreter(filled,
						context);
				tagInterpreter.setExecute(true);
				tagInterpreter.interpret(context);
			}
		}

		return null;
	}

	private void declareParams(InternalInterpreter internalInterpreter, NodeList childNodes) {
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item.getNodeName().equals("param")) {
				String name = getNodeAttributeValue(item, "name");
				String value = getNodeAttributeValue(item, "expr");

				System.err.println("name =" + name + " expr " + value);
				internalInterpreter.getContext().addParam(name);
				System.err.println("Param " + name + "\t"
						+ context.getDeclaration().evaluateScript(value, 50));
				internalInterpreter.getContext().getDeclaration().declareVariable(name,
						"'" + context.getDeclaration().evaluateScript(value, 50) + "'",
						InterpreterVariableDeclaration.ANONYME_SCOPE);
			}
		}
	}
}

class TransferTag extends DefaultVxmlElementInterpreter {
	public TransferTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException {
		String dest = getNodeAttributeValue(node, "dest");
		String destExpr = getNodeAttributeValue(node, "destexpr");
		context.setTransfertDestination((dest != null) ? dest : context.getDeclaration()
				.evaluateScript(destExpr, 50)
				+ "");
		throw new TransferException();
	}
}

class FieldTag extends DefaultVxmlElementInterpreter {
	private String tags = "prompt var assign if goto submit filled";

	public FieldTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException,
			SAXException {
		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			if (tags.contains((child.getNodeName())))
				TagInterpreterFactory.getTagInterpreter(child, context).interpret(context);
		}

		return null;
	}
}

class AudioTag extends DefaultVxmlElementInterpreter {
	public AudioTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException {
		Prompt p = new Prompt();
		String src = getNodeAttributeValue(node, "src");
		p.audio = src == null ? "" : src;
		p.tts = node.getTextContent();
		return p;
	}
}

class NoinputTag extends DefaultVxmlElementInterpreter {
	public NoinputTag(Node node, InterpreterContext context) {
		super(node, context);
	}
}

class CatchTag extends DefaultVxmlElementInterpreter {
	public CatchTag(Node node, InterpreterContext context) {
		super(node, context);
	}
}

class NomatchTag extends DefaultVxmlElementInterpreter {
	public NomatchTag(Node node, InterpreterContext context) {
		super(node, context);
	}
}

class EnumerateTag extends DefaultVxmlElementInterpreter {
	public EnumerateTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException,
			SAXException {
		String enumearation = "";
		NodeList childNodes = context.getSelectedFormItem().getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item.getNodeName().equals("choice")) {
				enumearation += item.getTextContent().trim() + "; ";
			}
		}
		return enumearation;
	}

}

class DefaultVxmlElementInterpreter implements VxmlElement {
	protected final Node node;
	protected final InterpreterContext context;

	DefaultVxmlElementInterpreter(Node node, InterpreterContext context) {
		this.node = node;
		this.context = context;
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException,
			SAXException {
		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			TagInterpreterFactory.getTagInterpreter(childs.item(i), context).interpret(context);
		}
		return null;
	}
}