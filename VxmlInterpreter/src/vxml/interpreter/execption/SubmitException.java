package vxml.interpreter.execption;

public class SubmitException extends InterpreterException {
	public String next;
//	String listVarToSubmit;

	public SubmitException(String next) {
		this.next = next;
	//	this.listVarToSubmit = listVarToSubmit;
	}

}
