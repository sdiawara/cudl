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
			return audio.equals(other.audio) && timeout.equals(other.timeout)
					&& bargein.equals(other.bargein) && tts.equals(other.tts)
					&& bargeinType.equals(other.bargeinType);

		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "prompt[" + "audio:" + audio + "," + "timeout:" + timeout + "," + "bargein:" + bargein
				+ "," + "bargeinType:" + bargeinType + "," + "tts:" + tts + "]";
	}
}
