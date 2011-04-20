package vxml.interpreter;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VxmlElement {
	public static final Set<String> FORM_ITEM_TYPES = new HashSet<String>() {
		private static final long serialVersionUID = -6039585007704420391L;
		{
			add("block");
			add("initial");
		}
	};

	public static final Set<String> FORM_INPUT_ITEM_TYPES = new HashSet<String>() {
		private static final long serialVersionUID = 1056605885559257656L;
		{
			add("field");
			add("subdialog");
			add("object");
			add("record");
			add("transfer");
		}
	};

	public static final Set<String> IF_CONDITIONAL_CHILDS_TYPE = new HashSet<String>() {
		private static final long serialVersionUID = 8591667871320502483L;
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
}
