package cudl;

import javax.script.ScriptException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import cudl.script.InterpreterVariableDeclaration;

public class InterpreterException extends Exception {
	// Should'nt this class also have package visibility ?
}

class TransferException extends InterpreterException {
}

class FilledException extends InterpreterException {
}

class ExitException extends InterpreterException {
}

class DisconnectException extends InterpreterException {
}

class GotoException extends InterpreterException {
	public String next;

	public GotoException(String string) {
		this.next = string;
	}
}

class EventException extends InterpreterException {
	public String type;

	public EventException(String type) {
		this.type = type;
	}
}

class SubmitException extends InterpreterException {
	public String next;

	public SubmitException(String next) {
		this.next = next;
	}
}