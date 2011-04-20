package vxml.interpreter;

public class VariableVxml {
	public final static String DEFAULT_VARIABLE_NAME = "var";
	public final static String DEFAULT_VARIABLE_VALUE = "undefined";
	private static int count = 0;
	public String name;
	public String value;

	public VariableVxml() {
		this(DEFAULT_VARIABLE_NAME + (count++), DEFAULT_VARIABLE_VALUE);
	}

	public VariableVxml(String name) {
		this(name, DEFAULT_VARIABLE_VALUE);
	}

	public VariableVxml(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof VariableVxml) {
			VariableVxml variableVxml = (VariableVxml) obj;
			return name.equals(variableVxml.name)
					&& value.equals(variableVxml.value);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "name =" + name + " value =" + value;
	}
}
