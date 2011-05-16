package fr.mbs.vxml.interpreter.event;

import java.util.EventListener;

import javax.script.ScriptException;

public interface InterpreterListener extends EventListener {
	public void noInput(InterpreterEvent interpreterEvent) throws ScriptException;

	public void NoMatch(InterpreterEvent interpreterEvent) throws ScriptException;

	public void error(InterpreterEvent interpreterEvent) throws ScriptException;

	public void help(InterpreterEvent interpreterEvent) throws ScriptException;
}