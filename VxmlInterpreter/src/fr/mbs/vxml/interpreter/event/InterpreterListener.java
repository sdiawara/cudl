package fr.mbs.vxml.interpreter.event;

import java.io.IOException;
import java.util.EventListener;

import javax.script.ScriptException;

public interface InterpreterListener extends EventListener {
	public void noInput(InterpreterEvent interpreterEvent)
			throws ScriptException, IOException;

	public void NoMatch(InterpreterEvent interpreterEvent)
			throws ScriptException, IOException;

	public void error(InterpreterEvent interpreterEvent)
			throws ScriptException, IOException;

	public void help(InterpreterEvent interpreterEvent) throws ScriptException,
			IOException;
}