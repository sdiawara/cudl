package vxml.interpreter;

import org.w3c.dom.Node;

import vxml.interpreter.execption.InterpreterException;

public interface NodeExecutor {
	public void execute(Node node) throws InterpreterException;
}
