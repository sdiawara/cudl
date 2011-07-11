package cudl;

import org.w3c.dom.Node;

class InterpreterException extends Exception {
	// Should'nt this class also have package visibility ?
}

class TransferException extends InterpreterException {
}

class FilledException extends InterpreterException {
	public FilledException(Node formItem) {
	}
}

class ExitException extends InterpreterException {
}

class DisconnectException extends InterpreterException {
}

class GotoException extends InterpreterException {
	public String next;
	public String nextItem;

	public GotoException(String next, String nextItem) {
		this.next = next;
		this.nextItem = nextItem;
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

class ReturnException extends Exception{
	private final String namelist;

	public ReturnException(String namelist) {
		this.namelist = namelist;
	}
}
