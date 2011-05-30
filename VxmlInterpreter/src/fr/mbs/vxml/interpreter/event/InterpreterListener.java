package fr.mbs.vxml.interpreter.event;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EventListener;

import javax.script.ScriptException;

import org.w3c.dom.DOMException;

public interface InterpreterListener extends EventListener {
	public void noInput(InterpreterEvent interpreterEvent)
			throws ScriptException, DOMException, FileNotFoundException,
			IOException;

	public void NoMatch(InterpreterEvent interpreterEvent)
			throws ScriptException, DOMException, FileNotFoundException,
			IOException;

	public void error(InterpreterEvent interpreterEvent)
			throws ScriptException, DOMException, FileNotFoundException,
			IOException;

	public void help(InterpreterEvent interpreterEvent) throws ScriptException,
			DOMException, FileNotFoundException, IOException;

}