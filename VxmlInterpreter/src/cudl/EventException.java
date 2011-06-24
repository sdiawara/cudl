package cudl;

public class EventException extends InterpreterException {
	public String type;

	public EventException(String type) {
		this.type = type;
	}
}
