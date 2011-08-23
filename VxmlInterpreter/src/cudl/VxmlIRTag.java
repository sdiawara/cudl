package cudl;

import static cudl.utils.Utils.getNodeAttributeValue;

import java.io.IOException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

class VxmlIRTag {
}

class ConfpassTag extends AbstractVxmlElement {

	public ConfpassTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException,
			SAXException {
		Prompt prompt = new Prompt();
		prompt.tts = "pass";
		context.getPrompts().add(prompt);
		throw new ExitException();
	}

}

class ConffailTag extends AbstractVxmlElement {

	public ConffailTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) throws InterpreterException, IOException,
			SAXException {
		String reason = getNodeAttributeValue(node, "reason");
		if (reason != null)
			System.err.println("reason ->" + reason);
		else {
			String expr = getNodeAttributeValue(node, "expr");
			if (expr != null)
				System.err.println("reason ->" + context.getDeclaration().evaluateScript(expr, 50));
		}
		return null;
	}
}

class ConfcommentTag extends CommentTag {

	public ConfcommentTag(Node node, InterpreterContext context) {
		super(node, context);
	}

	@Override
	public Object interpret(InterpreterContext context) {
		return super.interpret(context);
	}
}