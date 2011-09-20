package cudl;

import org.w3c.dom.Node;

class InterpreterException extends Exception {
}

class TransferException extends InterpreterException {
}

class FilledException extends InterpreterException {
	FilledException(Node formItem) {
	}
}

class ExitException extends InterpreterException {
}

class GotoException extends InterpreterException {
	String next;
	String nextItem;

	GotoException(String next, String nextItem) {
		this.next = next;
		this.nextItem = nextItem;
	}
}

class EventException extends InterpreterException {
	String type;

	EventException(String type) {
		this.type = type;
	}
}

class SubmitException extends InterpreterException {
	String next;

	SubmitException(String next) {
		this.next = next;
	}
}

class ReturnException extends InterpreterException {
	final String namelist;
	final String eventexpr;
	final String event;

	ReturnException(String event, String eventexpr, String namelist) {
		this.event = event;
		this.eventexpr = eventexpr;
		this.namelist = namelist;
	}
}
