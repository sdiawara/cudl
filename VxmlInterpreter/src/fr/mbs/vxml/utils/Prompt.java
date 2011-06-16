package fr.mbs.vxml.utils;

public class Prompt {

	public String audio = "";
	public String timeout = "";
	public String bargein = "";

	// TODO the two following fields should not be here since they
	// are not drectly prompt related but interaction field
	// related.
	// May be we should create a new Field class and reference
	// an instance of it here... may be not :)
	// Pascal 2009/007/29
	public String bargeinType ="";
	public String tts="";
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Prompt) {
			Prompt other = (Prompt) obj;
			return equalOrBothNull(audio, other.audio)
					&& equalOrBothNull(timeout, other.timeout)
					&& equalOrBothNull(bargein, other.bargein)
					&& equalOrBothNull(bargeinType, other.bargeinType)
					&& equalOrBothNull(tts, other.tts);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "prompt[" + "audio:" + audio + "," + "timeout:" + timeout + ","
				+ "bargein:" + bargein + "," + "bargeinType:"
				+ bargeinType + "," + "tts:" + tts + "]";
	}

	private boolean equalOrBothNull(Object o1, Object o2) {
		if (o1 == "") {
			return o2 == "";
		} else {
			return o1.equals(o2);
		}
	}

}
