package cudl;

public class Prompt {
	public String audio = "";
	public String timeout = "";
	public String bargein = "";
	public String bargeinType = "";
	public String tts = "";

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Prompt) {
			Prompt other = (Prompt) obj;
			return equalOrBothNull(audio, other.audio) && equalOrBothNull(timeout, other.timeout)
					&& equalOrBothNull(bargein, other.bargein) && equalOrBothNull(tts, other.tts)
					&& equalOrBothNull(bargeinType, other.bargeinType);

		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "prompt[" + "audio:" + audio + "," + "timeout:" + timeout + "," + "bargein:"
				+ bargein + "," + "bargeinType:" + bargeinType + "," + "tts:" + tts + "]";
	}

	private boolean equalOrBothNull(Object o1, Object o2) {
		if (o1 == "") {
			return o2 == "";
		} else {
			return o1.equals(o2);
		}
	}

}
