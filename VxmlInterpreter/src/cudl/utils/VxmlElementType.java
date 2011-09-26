package cudl.utils;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class VxmlElementType {

	private static final Set<String> DIALOG = new HashSet<String>() {
		{
			add("menu");
			add("form");
		}
	};

	private static final Set<String> FORM_ITEM_TYPES = new HashSet<String>() {
		{
			add("block");
			add("initial");
		}
	};

	private static final Set<String> FORM_INPUT_ITEM_TYPES = new HashSet<String>() {
		{
			add("field");
			add("subdialog");
			add("object");
			add("record");
			add("transfer");
		}
	};

	private static final Set<String> IF_CONDITIONAL_CHILDS_TYPE = new HashSet<String>() {
		{
			add("elseif");
			add("else");
		}
	};

	private static final Set<String> EXECUTABLE_ITEM = new HashSet<String>() {
		{
			add("var");
			add("assign");
			add("clear");
			add("if");
			add("prompt");
			add("reprompt");
			add("goto");
			add("submit");
			add("exit");
			add("return");
			add("disconnect");
			add("script");
			add("log");
			add("conf:pass");
			add("conf:fail");
			add("throw");
		}
	};

	public static boolean isFormItem(Node node) {
		return isInputItem(node) || FORM_ITEM_TYPES.contains(node.getNodeName());
	}

	public static boolean isConditionalItem(Node node) {
		return IF_CONDITIONAL_CHILDS_TYPE.contains(node.getNodeName());
	}

	public static boolean isInputItem(Node item) {
		return FORM_INPUT_ITEM_TYPES.contains(item.getNodeName());
	}

	public static boolean isADialog(Node item) {
		return DIALOG.contains(item.getNodeName());
	}

	public static boolean isAModalItem(Node item) {
		NamedNodeMap attributes = item.getAttributes();
		Node namedItem = attributes.getNamedItem("modal");
		return attributes != null && namedItem != null && namedItem.getNodeValue().equals("true");
	}

	public static boolean isAnExecutableItem(Node item) {
		return EXECUTABLE_ITEM.contains(item.getNodeName());
	}
}