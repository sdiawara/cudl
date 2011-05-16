package fr.mbs.vxml.utils;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VxmlElementType {
	
	private static final Set<String> EVENT = new HashSet<String>() {
		{
			add("noinput");
			add("nomatch");
			add("error");
			add("catch");
			add("help");
		}
	};
	
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

	public static boolean isFormItem(Node node) {
		return isInputItem(node)
				|| FORM_ITEM_TYPES.contains(node.getNodeName());
	}

	public static boolean isConditionalItem(Node node) {
		return IF_CONDITIONAL_CHILDS_TYPE.contains(node.getNodeName());
	}

	public static boolean isAnEvent(Node node) {
		return EVENT.contains(node.getNodeName());
	}
	
	public static boolean isSimpleIfItem(Node item) {
		assert (item.getNodeName().equals("if"));
		return getIfItemConditionChildsNumber(item) == 0;
	}

	public static boolean isIfElseItem(Node item) {
		assert (item.getNodeName().equals("if"));
		return getIfItemConditionChildsNumber(item) == 1;
	}

	public static boolean isSimpleIfElseifElseItem(Node item) {
		assert (item.getNodeName().equals("if"));
		return getIfItemConditionChildsNumber(item) > 1;
	}

	public static boolean isInputItem(Node item) {
		return FORM_INPUT_ITEM_TYPES.contains(item.getNodeName());
	}

	public static boolean isADialog(Node item) {
		return DIALOG.contains(item.getNodeName());
	}

	private static int getIfItemConditionChildsNumber(Node node) {
		NodeList childs = node.getChildNodes();

		int count = 0;
		for (int i = 0; i < childs.getLength(); i++) {
			if (isConditionalItem(childs.item(i))) {
				count++;
			}
		}
		return count;
	}

	public static boolean isAModalItem(Node item) {
		NamedNodeMap attributes = item.getAttributes();
		Node namedItem = attributes.getNamedItem("modal");
		return attributes != null && namedItem != null
				&& namedItem.getNodeValue().equals("true");
	}
}
