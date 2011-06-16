package cudl.event;

import java.util.EventObject;

public final class InterpreterEvent extends EventObject {
	public String type;

	public InterpreterEvent(Object context, String type) {
		super(context);
		this.type = type;
	}
}