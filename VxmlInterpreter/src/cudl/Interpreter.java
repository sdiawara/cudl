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
		try {
			internalInterpreter.mainLoop();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
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
		try {
			internalInterpreter.event("noinput");
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void noMatch() throws ScriptException, IOException, SAXException {
		try {
			internalInterpreter.event("nomatch");
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public String getTranferDestination() {
		return internalInterpreter.getContext().getTransferDestination();
	}

	public void noAnswer() throws ScriptException, IOException, SAXException {
		try {
			internalInterpreter.noAnswer();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void talk(String sentence) throws UnsupportedEncodingException,
			ScriptException, IOException, SAXException {
		try {
			internalInterpreter.utterance("'"
					+ URLEncoder.encode(sentence, "UTF-8").replaceAll("'", "")
					+ "'", "'voice'");
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void push(String dtmf) throws ScriptException, IOException,
			SAXException {
		try {
			internalInterpreter.utterance("'" + dtmf.replaceAll(" ", "") + "'",
					"'dtmf'");
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void disconnect() throws ScriptException, IOException, SAXException {
		try {
			internalInterpreter.event("connection.disconnect.hangup");
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void callerHangup(int i) throws IOException, ScriptException,
			SAXException {
		try {
			internalInterpreter.callerHangup(i);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
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
		try {
			internalInterpreter.blindTransferSuccess();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void destinationHangup() throws ScriptException, IOException,
			SAXException {
		try {
			internalInterpreter.destinationHangup();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void callerHangDestination() throws ScriptException, IOException,
			SAXException {
		try {
			internalInterpreter.callerHangDestination();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void maxTimeDisconnect() throws ScriptException, IOException,
			SAXException {
		try {
			internalInterpreter.maxTimeDisconnect();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void destinationBusy() throws ScriptException, IOException,
			SAXException {
		try {
			internalInterpreter.destinationBusy();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void networkBusy() throws ScriptException, IOException, SAXException {
		try {
			internalInterpreter.networkBusy();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

}
