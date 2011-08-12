package cudl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Interpreter {
	private InternalInterpreter internalInterpreter;
	
	public Interpreter(String fileName) throws IOException, 
			ParserConfigurationException, SAXException {
		internalInterpreter = new InternalInterpreter(fileName);
	}

	public void start() throws IOException,  SAXException {
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

	public void noInput() throws  IOException, SAXException {
		try {
			internalInterpreter.event("noinput");
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void noMatch() throws  IOException, SAXException {
		try {
			internalInterpreter.event("nomatch");
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public String getTranferDestination() {
		return internalInterpreter.getContext().getTransferDestination();
	}

	public void noAnswer() throws  IOException, SAXException {
		try {
			internalInterpreter.noAnswer();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void talk(String sentence) throws UnsupportedEncodingException, 
			IOException, SAXException {
		try {
			internalInterpreter.utterance("'"
					+ URLEncoder.encode(sentence, "UTF-8").replaceAll("'", "") + "'", "'voice'");
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void push(String dtmf) throws  IOException, SAXException {
		try {
			internalInterpreter.utterance("'" + dtmf.replaceAll(" ", "") + "'", "'dtmf'");
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void disconnect() throws  IOException, SAXException {
		try {
			internalInterpreter.event("connection.disconnect.hangup");
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void callerHangup(int i) throws IOException,  SAXException {
		try {
			internalInterpreter.callerHangup(i);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public String getGrammarActive() {
		return internalInterpreter.getContext().getGrammarActive().get(0).getAttributes()
				.getNamedItem("src").getNodeValue().trim();
	}

	public Properties getCurrentDialogProperties() {
		return internalInterpreter.getCurrentDialogProperties();
	}

	public void blindTransferSuccess() throws  IOException, SAXException {
		try {
			internalInterpreter.blindTransferSuccess();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void destinationHangup() throws  IOException, SAXException {
		try {
			internalInterpreter.destinationHangup();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void callerHangDestination() throws  IOException, SAXException {
		try {
			internalInterpreter.callerHangDestination();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void maxTimeDisconnect() throws  IOException, SAXException {
		try {
			internalInterpreter.maxTimeDisconnect();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void destinationBusy() throws  IOException, SAXException {
		try {
			internalInterpreter.destinationBusy();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public void networkBusy() throws  IOException, SAXException {
		try {
			internalInterpreter.networkBusy();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

}
