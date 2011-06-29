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
	private InterpreterContext context;

	public Interpreter(String fileName) throws IOException, ScriptException,
			ParserConfigurationException, SAXException {
		context = new InterpreterContext(fileName);
	}

	public void start() throws IOException, ScriptException, SAXException {
		System.err.println("Start interpretation");
		context.launchInterpreter();
	}

	public List<String> getTracetWithLabel(String... label) {
		return context.interpreter.getTracetWithLabel(label);
	}

	public List<String> getTraceLog() {
		return context.interpreter.getTraceLog();
	}

	public boolean raccrochage() {
		return context.interpreter.raccrochage();
	}

	public List<Prompt> getPrompts() {
		return context.interpreter.getPrompts();
	}

	public void noInput() throws ScriptException, IOException, SAXException {
		context.event("noinput");
	}

	public void noMatch() throws ScriptException, IOException, SAXException {
		context.event("nomatch");
	}

	public String getTranferDestination() {
		return context.interpreter.transfertDestination;
	}

	public void noAnswer() throws ScriptException, IOException, SAXException {
		context.noAnswer();
	}

	public void talk(String sentence) throws UnsupportedEncodingException,
			ScriptException, IOException, SAXException {
		context.talk("'"
				+ URLEncoder.encode(sentence, "UTF-8").replaceAll("'", "")
				+ "'");
	}

	public void push(String dtmf) throws ScriptException, IOException,
			SAXException {
		context.push("'" + dtmf.replaceAll(" ", "") + "'");
	}

	public void disconnect() throws ScriptException, IOException, SAXException {
		context.event("connection.disconnect.hangup");
	}

	public void callerHangup(int i) throws IOException, ScriptException,
			SAXException {
		context.callerHangup(i);
	}

	public String getGrammarActive() {
		return context.interpreter.grammarActive.get(0).getAttributes()
				.getNamedItem("src").getNodeValue().trim();
	}

	public Properties getCurrentDialogProperties() {
		return context.interpreter.getCurrentDialogProperties();
	}

	public void blindTransferSuccess() throws ScriptException, IOException,
			SAXException {
		context.blindTransferSuccess();
	}

	public void destinationHangup() throws ScriptException, IOException,
			SAXException {
		context.destinationHangup();

	}

	public void callerHangDestination() throws ScriptException, IOException,
			SAXException {
		context.callerHangDestination();
	}

	public void maxTimeDisconnect() throws ScriptException, IOException,
			SAXException {
		context.maxTimeDisconnect();
	}

	public void destinationBusy() throws ScriptException, IOException,
			SAXException {
		context.destinationBusy();
	}

	public void networkBusy() throws ScriptException, IOException, SAXException {
		context.networkBusy();
	}

}
