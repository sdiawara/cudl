package cudl;

import java.lang.reflect.Constructor;

import org.w3c.dom.Node;

import cudl.utils.Utils;

public class TagInterpreterFactory {
	public static VxmlElement getTagInterpreter(Node node, InterpreterContext context) {
		String nodeName = node.getNodeName().replaceAll("#|:", "");
		try {
			Constructor<?> constructor = Class.forName(Utils.getClassName(nodeName)).getConstructor(
					new Class[] { Node.class, InterpreterContext.class });
			return (VxmlElement) constructor.newInstance(node, context);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Not supported tag " + nodeName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
