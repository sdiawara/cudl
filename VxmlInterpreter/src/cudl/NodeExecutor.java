package cudl;

import java.io.IOException;

import org.w3c.dom.Node;

public interface NodeExecutor {
	public void execute(Node node) throws InterpreterException, IOException;
}
