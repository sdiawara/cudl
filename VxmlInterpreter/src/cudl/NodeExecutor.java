package cudl;

import java.io.IOException;

import javax.script.ScriptException;

import org.w3c.dom.Node;

import cudl.exception.InterpreterException;


public interface NodeExecutor {
	public void execute(Node node) throws InterpreterException,
			ScriptException, IOException;
}
