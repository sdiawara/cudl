package cudl.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Utils {
	public static Node searchItemByName(Node dialog, String id) {
		NodeList childs = dialog.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			String name = getNodeAttributeValue(child, "name");
			if (id.equals(name)) {
				return child;
			}
		}
		return null;
	}

	public static Node searchDialogByName(NodeList nodeList, String id) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			String idd = getNodeAttributeValue(node, "id");
			if (id.equals(idd))
				return node;
		}
		return null;
	}

	// FIXME: search all occurrence of searchNode
	public static Node serachItem(Node parent, String searchNodeName) {
		NodeList nodeList = parent.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeName().equals(searchNodeName)) {
				return node;
			}
		}
		return null;
	}

	public static String tackWeelFormedUrl(String location, String relativePath)
			throws MalformedURLException {
		URL target = new URL(new URL(location), relativePath);
		return target.toString();
	}

	public static String getNodeAttributeValue(Node node, String attributeName) {
		NamedNodeMap attributes = node.getAttributes();
		if (attributes == null)
			return null;

		Node attributeValue = attributes.getNamedItem(attributeName);
		return attributeValue == null ? null : attributeValue.getNodeValue();
	}

	public static String getClassName(String tagName) {
		String string = "cudl." + ((tagName.charAt(0) + "").toUpperCase()) + tagName.substring(1)
				+ "Tag";
		return string;
	}

}
