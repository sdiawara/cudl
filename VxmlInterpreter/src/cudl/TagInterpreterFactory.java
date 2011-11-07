package cudl;

import java.lang.reflect.Constructor;

import org.w3c.dom.Node;

class TagInterpreterFactory {
	static VoiceXmlNode getTagInterpreter(Node node) {
		String nodeName = node.getNodeName().replaceAll("#|:", "");
		String className = getClassName(nodeName);
		try {
			Constructor<?> constructor = Class.forName(className)
					.getConstructor(new Class[] { Node.class });
			return (VoiceXmlNode) constructor.newInstance(node);
		} catch (ClassNotFoundException e) {
			// FIXME check expected vxml behavior if unknown tag in page
			throw new UnsupportedOperationException("Not supported tag "
					+ nodeName);
		} catch (Exception e) {
			throw new RuntimeException("Error while trying to instanciate "
					+ nodeName + " tag handler (" + className + "): " + e, e);
		}
	}

	private static String getClassName(String tagName) {
		return "cudl." + capitalize(tagName) + "Tag";
	}

	private static String capitalize(String tagName) {
		return ((tagName.charAt(0) + "").toUpperCase()) + tagName.substring(1);
	}
}
