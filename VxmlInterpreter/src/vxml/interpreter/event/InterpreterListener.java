package vxml.interpreter.event;

import java.util.EventListener;

public interface InterpreterListener extends EventListener {
	public void noInput(InterpreterEvent interpreterEvent);

	public void NoMatch(InterpreterEvent interpreterEvent);

	public void error(InterpreterEvent interpreterEvent);

	public void help(InterpreterEvent interpreterEvent);
}