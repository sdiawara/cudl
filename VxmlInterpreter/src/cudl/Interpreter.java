package cudl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Properties;

import javax.script.ScriptException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Interpreter {
	private InternalInterpreter internalInterpreter;

	public Interpreter(String fileName) throws IOException, ScriptException,
			ParserConfigurationException, SAXException {
		internalInterpreter = new InternalInterpreter(fileName);
	}

	public void start() throws IOException, ScriptException, SAXException {
		internalInterpreter.interpretDialog();
	}

	public List<String> getTracetWithLabel(String... label) {
		return internalInterpreter.getTracetWithLabel(label);
	}

	public List<String> getTraceLog() {
		return internalInterpreter.getTraceLog();
	}

	public boolean raccrochage() {
		return internalInterpreter.getContext().isHangup();
	}

	public List<Prompt> getPrompts() {
		return internalInterpreter.getPrompts();
	}

	public void noInput() throws ScriptException, IOException, SAXException {
		internalInterpreter.event("noinput");
	}

	public void noMatch() throws ScriptException, IOException, SAXException {
		internalInterpreter.event("nomatch");
	}

	public String getTranferDestination() {
		return internalInterpreter.getContext().getTransferDestination();
	}

	public void noAnswer() throws ScriptException, IOException, SAXException {
		internalInterpreter.noAnswer();
	}

	public void talk(String sentence) throws UnsupportedEncodingException,
			ScriptException, IOException, SAXException {
		internalInterpreter.utterance("'"
				+ URLEncoder.encode(sentence, "UTF-8").replaceAll("'", "")
				+ "'", "'voice'");
	}

	public void push(String dtmf) throws ScriptException, IOException,
			SAXException {
		internalInterpreter.utterance("'" + dtmf.replaceAll(" ", "") + "'",
				"'dtmf'");
	}

	public void disconnect() throws ScriptException, IOException, SAXException {
		internalInterpreter.event("connection.disconnect.hangup");
	}

	public void callerHangup(int i) throws IOException, ScriptException,
			SAXException {
		internalInterpreter.callerHangup(i);
	}

	public String getGrammarActive() {
		return internalInterpreter.getContext().getGrammarActive().get(0)
				.getAttributes().getNamedItem("src").getNodeValue().trim();
	}

	public Properties getCurrentDialogProperties() {
		return internalInterpreter.getCurrentDialogProperties();
	}

	public void blindTransferSuccess() throws ScriptException, IOException,
			SAXException {
		internalInterpreter.blindTransferSuccess();
	}

	public void destinationHangup() throws ScriptException, IOException,
			SAXException {
		internalInterpreter.destinationHangup();

	}

	public void callerHangDestination() throws ScriptException, IOException,
			SAXException {
		internalInterpreter.callerHangDestination();
	}

	public void maxTimeDisconnect() throws ScriptException, IOException,
			SAXException {
		internalInterpreter.maxTimeDisconnect();
	}

	public void destinationBusy() throws ScriptException, IOException,
			SAXException {
		internalInterpreter.destinationBusy();
	}

	public void networkBusy() throws ScriptException, IOException, SAXException {
		internalInterpreter.networkBusy();
	}

}
