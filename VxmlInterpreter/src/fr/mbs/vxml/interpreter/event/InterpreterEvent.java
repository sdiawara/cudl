package fr.mbs.vxml.interpreter.event;

import java.util.EventObject;

public final class InterpreterEvent extends EventObject {
	public InterpreterEvent(Object context) {
		super(context);
	}
}