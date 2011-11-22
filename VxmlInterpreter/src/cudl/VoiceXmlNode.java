package cudl;

import static cudl.utils.Utils.getNodeAttributeValue;
import static cudl.utils.VxmlElementType.isFormItem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;

import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Undefined;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cudl.script.InterpreterVariableDeclaration;
import cudl.utils.Utils;
import cudl.utils.VxmlElementType;

abstract class VoiceXmlNode {
	protected final Node node;
	protected List<VoiceXmlNode> childs;

	// protected VoiceXmlNode parent;

	VoiceXmlNode(Node node) {
		this.node = node;
		createChilds(node.getChildNodes());
	}

	public String getAttribute(String att) {
		NamedNodeMap attributes = node.getAttributes();
		if (attributes == null)
			return null;

		Node attValue = attributes.getNamedItem(att);
		if (attValue == null)
			return null;

		return attValue.getNodeValue();
	}

	protected boolean checkCond(Node node, InterpreterContext context) throws IOException {
		String cond = getNodeAttributeValue(node, "cond");
		return cond == null
				|| Boolean.valueOf(context.getDeclaration().evaluateScript(cond, InterpreterVariableDeclaration.ANONYME_SCOPE)
						+ "");
	}

	public void addChild(VoiceXmlNode childNode) {
		if (canContainsChild(childNode)) {
			childs.add(childNode);
		}
	}

	public boolean canContainsChild(VoiceXmlNode child) {
		return false;
	}

	public boolean canContainsChild(String childName) {
		return false;
	}

	public String getNodeName() {
		return node.getNodeName();
	}

	private void createChilds(NodeList nodelist) {
		childs = new ArrayList<VoiceXmlNode>();
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node childNode = nodelist.item(i);
			if (canContainsChild(childNode.getNodeName()))
				childs.add(TagInterpreterFactory.getTagInterpreter(childNode));
		}
	}

	abstract Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException,
			ParserConfigurationException;
}

class FormTag extends VoiceXmlNode {
	private static final String ATT_ID = "id";
	private static final String ATT_SCOPE = "scope";

	private static final List<String> CHILDS;
	private static final String initChild = "var script";
	private int form_item_generated = 0;
	private String formItems = "block transfer subdialog field";
	private boolean initVar = true;

	static {
		CHILDS = new ArrayList<String>();
		CHILDS.add("block");
		CHILDS.add("catch");
		CHILDS.add("error");
		CHILDS.add("field");
		CHILDS.add("filled");
		CHILDS.add("grammar");
		CHILDS.add("help");
		CHILDS.add("initial");
		CHILDS.add("link");
		CHILDS.add("noinput");
		CHILDS.add("nomatch");
		CHILDS.add("property");
		CHILDS.add("record");
		CHILDS.add("script");
		CHILDS.add("subdialog");
		CHILDS.add("transfer");
		CHILDS.add("var");
	}

	public FormTag(Node node) {
		super(node);
	}

	public String getId() {
		return getAttribute(ATT_ID);
	}

	public String getScope() {
		return getAttribute(ATT_SCOPE);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException,
			ParserConfigurationException {
		// Phase initialization
		if (initVar) {
			NodeList childs = node.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++) {
				Node child = childs.item(i);
				String nodeName = child.getNodeName();
				if (initChild.contains(nodeName)) {
					TagInterpreterFactory.getTagInterpreter(child).interpret(context);
				} else if (isFormItem(child)) {
					String name = getNodeAttributeValue(child, "name");
					String expr = getNodeAttributeValue(child, "expr");
					if (name == null) {
						name = "form_item_generated_name_by_cudl_" + form_item_generated++;
					}
					context.getDeclaration().declareVariable(name, expr == null ? "undefined" : expr, 60);
					if ("field".equals(child.getNodeName()) || "initial".equals(child.getNodeName())) {
						context.setPromptCounter(child, 1);
					}
					context.addFormItemName(child, name);
				}
			}
		}

		while (true) {
			Node formItem = selectNextFormItem(context);
			if (formItem == null) {
				context.setHangup(true);
				System.err.println("SORTIE");
				break;
			}
			context.setSelectedFormItem(formItem);
			activateGrammar(context);

			if (formItems.contains(formItem.getNodeName())) {
				try {
					TagInterpreterFactory.getTagInterpreter(formItem).interpret(context);
					context.getDeclaration().resetScopeBinding(InterpreterVariableDeclaration.ANONYME_SCOPE);
				} catch (ExitException e) {
					context.setHangup(true);
					return null;
				} catch (GotoException e) {
					context.setNextItemToVisit(e.nextItem);
					if (e.next != null) {
						context.getDeclaration().resetScopeBinding(InterpreterVariableDeclaration.DIALOG_SCOPE);
						throw new GotoException(e.next, null);
					}
				}
			}
		}
		return null;
	}

	private void activateGrammar(InterpreterContext context) {
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

	Node selectNextFormItem(InterpreterContext context) {
		Node node = null;
		if (context.getNextItemToVisit() != null) {
			node = Utils.searchItemByName(context.getCurrentDialog(), context.getNextItemToVisit());
			context.setNextItemToVisit(null);
		} else {
			for (Iterator<Entry<Node, String>> iterator = context.getFormItemNames().entrySet().iterator(); iterator.hasNext();) {
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

	@Override
	public boolean canContainsChild(VoiceXmlNode child) {
		return CHILDS.contains(child.getNodeName());
	}
}

class MenuTag extends VoiceXmlNode {
	private static final String ATT_ID = "id";
	private static final String ATT_SCOPE = "scope";
	private static final String ATT_DTMF = "dtmf";
	private static final String ATT_ACCEPT = "accept";

	private static final List<String> CHILDS;

	private String execute = "prompt";

	static {
		CHILDS = new ArrayList<String>();
		CHILDS.add("audio");
		CHILDS.add("catch");
		CHILDS.add("choice");
		CHILDS.add("enumerate");
		CHILDS.add("error");
		CHILDS.add("help");
		CHILDS.add("noinput");
		CHILDS.add("nomatch");
		CHILDS.add("prompt");
		CHILDS.add("property");
		CHILDS.add("script");
		CHILDS.add("value");
	}

	public MenuTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException,
			ParserConfigurationException {
		context.setSelectedFormItem(node);
		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node item = childs.item(i);
			if (execute.contains(item.getNodeName())) // FIXME: remove
				TagInterpreterFactory.getTagInterpreter(item).interpret(context);
		}
		return null;
	}

	public String getId() {
		return getAttribute(ATT_ID);
	}

	public String getScope() {
		return getAttribute(ATT_SCOPE);
	}

	public String getDtmf() {
		return getAttribute(ATT_DTMF);
	}

	public String getAttAccept() {
		return getAttribute(ATT_ACCEPT);
	}

	@Override
	public boolean canContainsChild(VoiceXmlNode child) {
		return CHILDS.contains(child.getNodeName());
	}
}

class VarTag extends VoiceXmlNode {
	private static final String ATT_NAME = "name";
	private static final String ATT_EXPR = "expr";

	public VarTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws EventException {
		String name = getName();
		String[] split = name.split("\\.");
		if (Utils.scopeNames().contains(split[0]) && split.length > 1) {
			throw new EventException("error.semantic",
					"Aucune declaration possible avec les noms de porté : [session application document dialog]");
		}
		if (!context.getParams().contains(name)) {
			String expr = getExpr();
			context.getDeclaration().declareVariable(name, expr == null ? "undefined" : expr,
					node.getParentNode().getNodeName().equals("form") ? 60 : 50);
		}
		return null;
	}

	public String getName() {
		return getAttribute(ATT_NAME);
	}

	public String getExpr() {
		return getAttribute(ATT_EXPR);
	}
}

class AssignTag extends VoiceXmlNode {
	private static final String ATT_NAME = "name";
	private static final String ATT_EXPR = "expr";

	public AssignTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws EventException {
		String name = getName();
		String expr = getExpr();
		try {
			context.getDeclaration().getValue(name); // Dont'n remove this, he
			// check variable declaration
			context.getDeclaration().setValue(name, expr);
		} catch (EcmaError error) {
			throw new EventException("error.semantic", "La variable " + name + " n'est pas declaré");
		}
		return null;
	}

	public String getName() {
		return getAttribute(ATT_NAME);
	}

	public String getExpr() {
		return getAttribute(ATT_EXPR);
	}
}

class LogTag extends VoiceXmlNode {
	private static final String ATT_LABEL = "label";
	private static final String ATT_EXPR = "expr";

	private static final List<String> CHILDS;

	static {
		CHILDS = new ArrayList<String>();
		CHILDS.add("value");
	}

	public LogTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException,
			ParserConfigurationException {
		Log log = new Log();

		String label = getLabel();
		String expr = getExpr();

		System.err.print("LOG:");
		if (label != null) {
			log.label = label;
			System.err.print(" " + label + " ");
		}

		log.value += expr == null ? "" : context.getDeclaration().evaluateScript(expr, 50) + " ";

		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			log.value += TagInterpreterFactory.getTagInterpreter(child).interpret(context);
		}
		log.value = log.value.trim();
		System.err.println(log.value);

		context.getLogs().add(log);
		return null;
	}

	public String getLabel() {
		return getAttribute(ATT_LABEL);
	}

	public String getExpr() {
		return getAttribute(ATT_EXPR);
	}

	@Override
	public boolean canContainsChild(String childName) {
		return CHILDS.contains(childName);
	}
}

class PromptTag extends VoiceXmlNode {
	private static final String ATT_TIMEOUT = "timeout";
	private static final String ATT_BARGEIN = "bargein";
	private static final String ATT_BARGEINTYPE = "bargeintype";
	private static final String ATT_COND = "cond";
	private static final String ATT_XML_LANG = "xml:lang";
	private static final List<String> CHILDS;
	static {
		CHILDS = new ArrayList<String>();
		CHILDS.add("audio");
		CHILDS.add("value");
		// CHILDS.add("break");
		// CHILDS.add("emphasis");
		// CHILDS.add("enumerate");
		// CHILDS.add("mark");
		// CHILDS.add("paragraph");
		// CHILDS.add("phoneme");
		// CHILDS.add("prosody");
		// CHILDS.add("say-as");
		// CHILDS.add("sentence");
	}

	String prompt = "audio";

	public PromptTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException,
			ParserConfigurationException {
		if (!checkCond(node, context)) {
			return null;
		}

		Prompt p = new Prompt();

		String timeout = getNodeAttributeValue(node, "timeout");
		p.timeout = timeout != null ? timeout : "";

		String bargein = getNodeAttributeValue(node, "bargein");
		p.bargein = bargein != null ? bargein : "";

		String bargeinType = getNodeAttributeValue(node, "bargeintype");
		p.bargeinType = bargeinType != null ? bargeinType : "";

		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			if (prompt.contains(child.getNodeName())) {
				Prompt pc = (Prompt) TagInterpreterFactory.getTagInterpreter(child).interpret(context);
				p.audio += pc.audio + " ";
				p.tts += pc.tts + " ";
			} else {
				p.tts += TagInterpreterFactory.getTagInterpreter(child).interpret(context);
			}
		}

		p.tts = p.tts.trim();
		p.audio = p.audio.trim();
		context.addPrompt(p);

		return null;
	}

	public String getBargein() {
		return getAttribute(ATT_BARGEIN);
	}

	public String getBargeinType() {
		return getAttribute(ATT_BARGEINTYPE);
	}

	public String getCond() {
		return getAttribute(ATT_COND);
	}

	public String getTimeout() {
		return getAttribute(ATT_TIMEOUT);
	}

	public String getXmlLang() {
		return getAttribute(ATT_XML_LANG);
	}

	@Override
	public boolean canContainsChild(String childName) {
		return CHILDS.contains(childName);
	}
}

class ExitTag extends VoiceXmlNode {
	private static final String ATT_NAME_LIST = "namelist";
	private static final String ATT_EXPR = "expr";

	public ExitTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws ExitException, EventException {
		if (node.getAttributes().getLength() > 1)
			throw new EventException("error.badfetch", "La balise exit doit avoir exactement un attribut.");
		context.setHangup(true);
		throw new ExitException();
	}

	public String getNameList() {
		return getAttribute(ATT_NAME_LIST);
	}

	public String getExpr() {
		return getAttribute(ATT_EXPR);
	}
}

class ClearTag extends VoiceXmlNode {
	private static final String ATT_NAMELIST = "namelist";

	public ClearTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws EventException {
		String namelist = getNameList();
		if (namelist != null) {
			StringTokenizer tokenizer = new StringTokenizer(namelist);
			while (tokenizer.hasMoreElements()) {
				String name = (String) tokenizer.nextElement();
				try {
					context.getDeclaration().getValue(name);
				} catch (Exception e) {
					throw new EventException("error.semantic", "La variable " + name + " n'est pas declaré");
				}
				context.getDeclaration().setValue(name, "undefined");
			}
		} else {
			for (Iterator<Map.Entry<Node, String>> iterator = context.getFormItemNames().entrySet().iterator(); iterator
					.hasNext();) {
				Map.Entry<Node, String> FormItem = (Map.Entry<Node, String>) iterator.next();
				context.getDeclaration().setValue(FormItem.getValue(), "undefined");
			}
		}

		return null;
	}

	public String getNameList() {
		return getAttribute(ATT_NAMELIST);
	}
}

class IfTag extends VoiceXmlNode {
	private static final String ATT_COND = "cond";
	private static final List<String> CHILDS;

	static {
		CHILDS = new ArrayList<String>();
		CHILDS.add("assign");
		CHILDS.add("audio");
		CHILDS.add("clear");
		CHILDS.add("data");
		CHILDS.add("disconnect");
		CHILDS.add("else");
		CHILDS.add("elseif");
		CHILDS.add("enumerate");
		CHILDS.add("exit");
		CHILDS.add("goto");
		CHILDS.add("if");
		CHILDS.add("log");
		CHILDS.add("prompt");
		CHILDS.add("reprompt");
		CHILDS.add("return");
		CHILDS.add("script");
		CHILDS.add("submit");
		CHILDS.add("throw");
		CHILDS.add("value");
		CHILDS.add("var");
	}

	public IfTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws IOException, InterpreterException, SAXException,
			ParserConfigurationException {
		boolean conditionChecked = checkCond(node, context);
		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			if (VxmlElementType.isAnExecutableItem(child)) {
				if (conditionChecked) {
					TagInterpreterFactory.getTagInterpreter(child).interpret(context);
				}
			} else if (VxmlElementType.isConditionalItem(child)) {
				if (conditionChecked) {
					break;
				} else {
					conditionChecked = this.checkCond(child, context);
				}
			}
		}
		return null;
	}

	public String getCond() {
		return getAttribute(ATT_COND);
	}
}

class TextTag extends VoiceXmlNode {
	public TextTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) {
		String textContent = node.getTextContent().replaceAll("[\\s][\\s]+", " ");
		if (VxmlElementType.isFormItem(node.getParentNode()) && !textContent.trim().equals("")) {
			Prompt p = new Prompt();
			p.tts += textContent.trim();
			context.addPrompt(p);
		}
		return textContent;
	}
}

class CommentTag extends VoiceXmlNode {
	public CommentTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) {
		// TODO: modifie prompt and log to return null
		return "";
	}
}

class GotoTag extends VoiceXmlNode {
	private static final String ATT_EXPR = "expr";
	private static final String ATT_EXPRITEM = "expritem";
	private static final String ATT_FETCH_AUDIO = "fetchaudio";
	private static final String ATT_FETCH_INT = "fetchint";
	private static final String ATT_FETCH_TIMEOUT = "fetchtimeout";
	private static final String ATT_MAX_AGE = "maxage";
	private static final String ATT_MAX_STALE = "maxstale";
	private static final String ATT_NEXT = "next";
	private static final String ATT_NEXT_ITEM = "nextitem";

	public GotoTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException {
		String nextItemTmp = getNodeAttributeValue(node, "nextitem");
		String exprItemTmp = getNodeAttributeValue(node, "expritem");

		String nextItem;
		if (nextItemTmp != null && exprItemTmp != null) {
			throw new EventException("error.badfetch", "La balise goto doit un seul attribut parmi : nextitem et expritem");
		} else {
			nextItem = nextItemTmp != null ? nextItemTmp : exprItemTmp != null ? context.getDeclaration().getValue(exprItemTmp)
					+ "" : null;
		}

		if (nextItem != null) {
			throw new GotoException(null, nextItem);
		}

		String nextTmp = getNodeAttributeValue(node, "next");
		String exprTmp = getNodeAttributeValue(node, "expr");

		String next;
		if (nextTmp != null && exprTmp != null) {
			throw new EventException("error.badfetch", "La balise goto doit un seul attribut parmi : next et expr");
		} else {
			next = nextTmp != null ? nextTmp : context.getDeclaration().getValue(exprTmp) + "";
		}
		throw new GotoException(next, null);
	}

	public String getExpr() {
		return getAttribute(ATT_EXPR);
	}

	public String getExpritem() {
		return getAttribute(ATT_EXPRITEM);
	}

	public String getFetchAudio() {
		return getAttribute(ATT_FETCH_AUDIO);
	}

	public String getFetchInt() {
		return getAttribute(ATT_FETCH_INT);
	}

	public String getFetchTimeout() {
		return getAttribute(ATT_FETCH_TIMEOUT);
	}

	public String getMaxAge() {
		return getAttribute(ATT_MAX_AGE);
	}

	public String getMaxStale() {
		return getAttribute(ATT_MAX_STALE);
	}

	public String getNext() {
		return getAttribute(ATT_NEXT);
	}

	public String getNextItem() {
		return getAttribute(ATT_NEXT_ITEM);
	}
}

class SubmitTag extends VoiceXmlNode {
	private static final String ATT_ENCTYPE = "enctype";
	private static final String ATT_EXPR = "expr";
	private static final String ATT_FETCH_AUDIO = "fetchaudio";
	private static final String ATT_FETCH_INT = "fetchint";
	private static final String ATT_FETCH_TIMEOUT = "fetchtimeout";
	private static final String ATT_METHOD = "method";
	private static final String ATT_NAME_LIST = "namelist";
	private static final String ATT_NEXT = "next";

	public SubmitTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException {
		String next = getNext();
		String expr = getExpr();
		if (next != null && expr != null) {
			throw new EventException("error.badfetch",
					"La balise submit doit avoir exactement une des deux attributs: next ou expr");
		}

		next = next != null ? next : context.getDeclaration().getValue(expr) + "";

		String nameList = getNameList();
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

	public String getExpr() {
		return getAttribute(ATT_EXPR);
	}

	public String getFetchAudio() {
		return getAttribute(ATT_FETCH_AUDIO);
	}

	public String getFetchInt() {
		return getAttribute(ATT_FETCH_INT);
	}

	public String getFetchTimeout() {
		return getAttribute(ATT_FETCH_TIMEOUT);
	}

	public String getMethod() {
		return getAttribute(ATT_METHOD);
	}

	public String getNext() {
		return getAttribute(ATT_NEXT);
	}

	public String getNameList() {
		return getAttribute(ATT_NAME_LIST);
	}

	public String getEnctype() {
		return getAttribute(ATT_ENCTYPE);
	}
}

class ReturnTag extends VoiceXmlNode {
	private static final String ATT_EVENT = "event";
	private static final String ATT_EVENT_EXPR = "eventexpr";
	private static final String ATT_MESSAGE_EXPR = "messageexpr";
	private static final String ATT_MESSAGE = "message";
	private static final String ATT_NAME_LIST = "namelist";

	public ReturnTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException {
		String namelist = getNameList();
		String event = getEvent();
		String eventexpr = getEventExpr();
		if ((namelist != null && (event != null || eventexpr != null)) || (event != null && eventexpr != null))
			throw new EventException("error.badfetch",
					"La balise submit doit avoir exactement une des attributs: namelist, event ou eventexpr");
		// TODO: check parent is an subdialog ==> throw semantic error
		throw new ReturnException(event, eventexpr, namelist);
	}

	public String getEvent() {
		return getAttribute(ATT_EVENT);
	}

	public String getEventExpr() {
		return getAttribute(ATT_EVENT_EXPR);
	}

	public String getMessageExpr() {
		return getAttribute(ATT_MESSAGE_EXPR);
	}

	public String getMessage() {
		return getAttribute(ATT_MESSAGE);
	}

	public String getNameList() {
		return getAttribute(ATT_NAME_LIST);
	}
}

class DisconnectTag extends VoiceXmlNode {
	public DisconnectTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException {
		throw new EventException("connection.disconnect.hangup");
	}
}

class ValueTag extends VoiceXmlNode {
	String prompt = "block catch";

	public ValueTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) {
		Object value = context.getDeclaration().getValue(getNodeAttributeValue(node, "expr"));
		if (prompt.contains(node.getParentNode().getNodeName())) {
			Prompt p = new Prompt();
			p.tts = value + "";
			context.getPrompts().add(p);
		}
		return value;
	}
}

class ThrowTag extends VoiceXmlNode {
	public ThrowTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException {
		NamedNodeMap attributes = node.getAttributes();
		if (attributes.getNamedItem("event") != null && attributes.getNamedItem("eventexpr") != null) {
			throw new EventException("error.badfetch",
					"La balise Throw doit avoir exactement une des deux attributs: event ou event expr");
		}
		throw new EventException(getNodeAttributeValue(node, "event"));
	}
}

class ScriptTag extends VoiceXmlNode {
	public ScriptTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws IOException, InterpreterException {
		String src = getNodeAttributeValue(node, "src");
		if (src != null && node.hasChildNodes())
			throw new EventException("error.badfetch",
					"La balise script ne peut avoir de contenue lorsque il definit un attribut src");

		if (src == null) {
			context.getDeclaration().evaluateScript(node.getTextContent(), InterpreterVariableDeclaration.DIALOG_SCOPE);
		} else {
			try {
				context.getDeclaration().evaluateFileScript(src, InterpreterVariableDeclaration.DIALOG_SCOPE);
			} catch (FileNotFoundException e) {
				throw new EventException("error.badfetch", "aucun fichier de ce type :" + src);
			}

		}
		return null;
	}
}

class SubdialogTag extends VoiceXmlNode {
	public SubdialogTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException,
			ParserConfigurationException {
		InterpreterVariableDeclaration declaration = context.getDeclaration();
		String formItemName = context.getFormItemNames().get(node);
		declaration.setValue(formItemName, "true");
		String src = getNodeAttributeValue(node, "src");
		String srcexpr = getNodeAttributeValue(node, "srcexpr");

		if (src != null && srcexpr != null) {
			throw new EventException("error.badfetch",
					"La balise subdialog doit avoir exactement une des deux attributs: src et srcexpr");
		}
		if (src == null && srcexpr != null) {
			src = context.getDeclaration().evaluateScript(srcexpr, 50) + "";
		}
		InternalInterpreter internalInterpreter = null;
		if (src != null) {
			String url = Utils.tackWeelFormedUrl(context.getCurrentFileName(), src);

			// if remote url, put namelist values in GET parameters
			String nameList = getNodeAttributeValue(node, "namelist");
			if (nameList != null) {
				StringTokenizer tokenizer = new StringTokenizer(nameList);
				String urlSuite = "?";
				while (tokenizer.hasMoreElements()) {
					String data = tokenizer.nextToken();
					urlSuite += data + "=" + context.getDeclaration().getValue(data) + "&";
				}
				url += urlSuite;
			}
			System.err.println("subdialog url" + url);
			// TODO put namelist values in child context

			// FIXME this is not a viable way to handle subdialogs ;(
			InterpreterContext subContext = new InterpreterContext(url, context.cookies);
			subContext.enterSubdialog();
			internalInterpreter = new InternalInterpreter(subContext);
			context.cookies = subContext.cookies;

			declareParams(internalInterpreter, node.getChildNodes(), context);

			context.setInternalInterpreter(internalInterpreter);
			internalInterpreter.interpret(1, null);
			context.getLogs().addAll(internalInterpreter.getContext().getLogs());
			context.getPrompts().addAll(internalInterpreter.getContext().getPrompts());
		}
		context.getDeclaration().evaluateScript(formItemName + "=new Object();", InterpreterVariableDeclaration.DIALOG_SCOPE);

		setReturnVariable(context, formItemName, internalInterpreter);

		return null;
	}

	void setReturnVariable(InterpreterContext context, String formItemName, InternalInterpreter internalInterpreter)
			throws EventException, InterpreterException, IOException, SAXException, ParserConfigurationException {
		if (internalInterpreter != null) {
			context.getDeclaration().evaluateScript(formItemName + "=new Object();", InterpreterVariableDeclaration.DIALOG_SCOPE);
			String[] returnValue = internalInterpreter.getContext().getReturnValue();

			String namelist = returnValue[2];
			if (namelist != null) {
				StringTokenizer tokenizer = new StringTokenizer(namelist);
				InterpreterVariableDeclaration declaration2 = internalInterpreter.getContext().getDeclaration();
				while (tokenizer.hasMoreElements()) {
					String variable = tokenizer.nextToken();

					String script = formItemName + "." + variable + "="
							+ cudl.script.Utils.scriptableObjectToString(declaration2.getValue(variable)) + "";
					System.err.println(script);
					context.getDeclaration().evaluateScript(script, InterpreterVariableDeclaration.ANONYME_SCOPE);
				}
			} else if (returnValue[0] != null) {
				throw new EventException(returnValue[0]);
			}

			Node filled = Utils.serachItem(node, "filled");
			if (filled != null) {
				FilledTag tagInterpreter = (FilledTag) TagInterpreterFactory.getTagInterpreter(filled);
				tagInterpreter.setExecute(true);
				tagInterpreter.interpret(context);
			}
		}
	}

	private void declareParams(InternalInterpreter internalInterpreter, NodeList childNodes, InterpreterContext context) {
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item.getNodeName().equals("param")) {
				String name = getNodeAttributeValue(item, "name");
				String value = getNodeAttributeValue(item, "expr");
				internalInterpreter.getContext().addParam(name);
				Object evaluateScript = context.getDeclaration().evaluateScript(value, 50);
				internalInterpreter.getContext().getDeclaration().declareVariable(name, "'" + evaluateScript + "'", 50);
			}
		}
	}
}

class TransferTag extends VoiceXmlNode {
	public TransferTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException {
		String dest = getNodeAttributeValue(node, "dest");
		String destExpr = getNodeAttributeValue(node, "destexpr");
		context.setTransfertDestination((dest != null) ? dest : context.getDeclaration().evaluateScript(destExpr, 50) + "");
		throw new TransferException();
	}
}

class FieldTag extends VoiceXmlNode {
	private final String tags = "prompt var assign if goto submit filled";

	public FieldTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException,
			ParserConfigurationException {
		for (VoiceXmlNode tag : childs) {
			if (tag instanceof PromptTag) {
				String count = getNodeAttributeValue(tag.node, "count");
				if (!(count == null || Integer.valueOf(count) == context.getPromptCounter(context.getSelectedFormItem()))) {
					continue;
				}
			}
			tag.interpret(context);
		}
		return null;
	}

	@Override
	public boolean canContainsChild(String childName) {
		return tags.contains(childName);
	}
}

class AudioTag extends VoiceXmlNode {
	public AudioTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException {
		Prompt p = new Prompt();
		String src = getNodeAttributeValue(node, "src");
		String expr = getNodeAttributeValue(node, "expr");

		if ((src == null && expr == null) || (src != null && expr != null)) {
			throw new EventException("error.badfetch",
					"La balise audio doit avoir exactement une des deux attributs: src ou expr");
		}

		p.audio = (src == null) ? context.getDeclaration().evaluateScript(expr, 50) + "" : src;
		p.tts = node.getTextContent();

		if (node.getParentNode().getNodeName().equals("block")) {
			context.addPrompt(p);
		}
		return p;
	}
}

class EnumerateTag extends VoiceXmlNode {
	public EnumerateTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException {
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

class RepromptTag extends VoiceXmlNode {

	public RepromptTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException,
			ParserConfigurationException {
		context.incrementPromptCounter(context.getSelectedFormItem());
		return null;
	}
}

class ProceduralsTag extends VoiceXmlNode {

	ProceduralsTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException,
			ParserConfigurationException {
		for (VoiceXmlNode tag : childs) {
			tag.interpret(context);
		}
		return null;
	}

	@Override
	public boolean canContainsChild(String childName) {
		return true;
	}
}

class BlockTag extends ProceduralsTag {
	public BlockTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException,
			ParserConfigurationException {
		context.getDeclaration().setValue(context.getFormItemNames().get(node), "true");
		return super.interpret(context);
	}
}

class FilledTag extends ProceduralsTag {
	private boolean execute = false;

	public FilledTag(Node node) {
		super(node);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException, SAXException,
			ParserConfigurationException {
		if (!execute) {
			System.err.println("wait user input");
			throw new FilledException(node.getParentNode());
		}

		return super.interpret(context);
	}

	public void setExecute(boolean execute) {
		this.execute = execute;
	}
}

class NoinputTag extends ProceduralsTag {
	public NoinputTag(Node node) {
		super(node);
	}
}

class CatchTag extends ProceduralsTag {
	public CatchTag(Node node) {
		super(node);
	}
}

class NomatchTag extends ProceduralsTag {
	public NomatchTag(Node node) {
		super(node);
	}
}
