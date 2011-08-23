package cudl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import cudl.script.InterpreterVariableDeclaration;

public class Interpreter {
	private InternalInterpreter internalInterpreter1;
	private InterpreterContext context;

	public Interpreter(String fileName) throws IOException, ParserConfigurationException,
			SAXException {
		context = new InterpreterContext(fileName, new InterpreterVariableDeclaration(fileName));
		internalInterpreter1 = new InternalInterpreter(context);
	}

	public void start() throws IOException, SAXException {
		internalInterpreter1.interpret(context);
	}

	public List<String> getTracetWithLabel(String... label) {
		List<String> labeledLog = new ArrayList<String>();
		for (Iterator<Log> iterator = context.getLogs().iterator(); iterator.hasNext();) {
			Log log = (Log) iterator.next();
			for (int i = 0; i < label.length; i++) {
				if (log.label.equals(label[i])) {
					labeledLog.add(log.value);
				}
			}
		}
		return labeledLog;
	}

	public List<String> getTraceLog() {
		List<String> lLog = new ArrayList<String>();
		for (Iterator<Log> iterator = context.getLogs().iterator(); iterator.hasNext();) {
			lLog.add(((Log) iterator.next()).value);
		}

		return lLog;
	}

	public boolean raccrochage() {
		return context.isHangup();
	}

	public List<Prompt> getPrompts() {
		return context.getPrompts();
	}

	public void noInput() throws IOException, SAXException, ParserConfigurationException {
		internalInterpreter1.event("noinput");
	}

	public void noMatch() throws IOException, SAXException, ParserConfigurationException {
		internalInterpreter1.event("nomatch");
	}

	public String getTranferDestination() {
		return internalInterpreter1.getContext().getTransferDestination();
	}

	public void noAnswer() throws IOException, SAXException, ParserConfigurationException {
		internalInterpreter1.noAnswer();
	}

	public void talk(String sentence) throws UnsupportedEncodingException, IOException,
			SAXException, ParserConfigurationException {
		internalInterpreter1.utterance("'" + URLEncoder.encode(sentence, "UTF-8").replaceAll("'", "")
				+ "'", "'voice'");
	}

	public void push(String dtmf) throws IOException, SAXException, ParserConfigurationException {
		internalInterpreter1.utterance("'" + dtmf.replaceAll(" ", "") + "'", "'dtmf'");
	}

	public void disconnect() throws IOException, SAXException, ParserConfigurationException {
		internalInterpreter1.event("connection.disconnect.hangup");
	}

	public void callerHangup(int i) throws IOException, SAXException, ParserConfigurationException {
		internalInterpreter1.callerHangup(i);
	}

	public String getGrammarActive() {
		return internalInterpreter1.getContext().getGrammarActive().get(0).getAttributes()
				.getNamedItem("src").getNodeValue().trim();
	}

	public Properties getCurrentDialogProperties() {
		return internalInterpreter1.getCurrentDialogProperties();
	}

	public void blindTransferSuccess() throws IOException, SAXException,
			ParserConfigurationException {
		internalInterpreter1.blindTransferSuccess();
	}

	public void destinationHangup() throws IOException, SAXException, ParserConfigurationException {
		internalInterpreter1.destinationHangup();
	}

	public void callerHangDestination() throws IOException, SAXException,
			ParserConfigurationException {
		internalInterpreter1.callerHangDestination();
	}

	public void maxTimeDisconnect() throws IOException, SAXException, ParserConfigurationException {
		internalInterpreter1.maxTimeDisconnect();
	}

	public void destinationBusy() throws IOException, SAXException, ParserConfigurationException {
		internalInterpreter1.destinationBusy();
	}

	public void networkBusy() throws IOException, SAXException, ParserConfigurationException {
		internalInterpreter1.networkBusy();
	}
}
