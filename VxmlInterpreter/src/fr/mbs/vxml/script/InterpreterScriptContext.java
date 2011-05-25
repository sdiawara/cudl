package fr.mbs.vxml.script;

import javax.script.ScriptContext;

public interface InterpreterScriptContext extends ScriptContext {
	public static final int SESSION_SCOPE = 90;
	public static final int APPLICATION_SCOPE = 80;
	public static final int DOCUMENT_SCOPE = 70;
	public static final int DIALOG_SCOPE = 60;
	public static final int ANONYME_SCOPE = 50;
}
