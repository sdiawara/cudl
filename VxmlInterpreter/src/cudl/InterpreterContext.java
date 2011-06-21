package cudl;

import java.io.File;
import java.io.IOException;

import javax.script.ScriptException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

import cudl.event.InterpreterEvent;
import cudl.event.InterpreterEventHandler;
import cudl.event.InterpreterListener;
import cudl.exception.EventException;
import cudl.exception.GotoException;
import cudl.exception.InterpreterException;
import cudl.exception.SubmitException;
import cudl.script.DefaultInterpreterScriptContext;
import cudl.utils.InterpreterRequierement;
import cudl.utils.Utils;
import cudl.utils.VxmlDefaultPageCreator;


public class InterpreterContext extends WebClient {
	private Document currentdDocument;
	private Node currentDialog;
	public Document rootDocument;
	private NodeList dialogs;
	public Interpreter interpreter = new Interpreter();
	private InterpreterListener interpreterListener;

	public Node field;
	private String currentFileName;
	private String currentRootFileName;

	public InterpreterContext(String fileName, File session)
			throws IOException, ScriptException {
		setPageCreator(new VxmlDefaultPageCreator());
		buildDocument(fileName);
		interpreterListener = new InterpreterEventHandler();
	}

	public InterpreterContext(String fileName) throws IOException,
			ScriptException {
		this(fileName, null);
	}

	public void launchInterpreter() throws IOException, ScriptException {
		try {
			interpreter.interpretDialog(currentDialog);
			field = interpreter.selectedItem;
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	public void executionHandler(InterpreterException e) throws IOException,
			ScriptException {
		if (e instanceof GotoException) {
			GotoException gotoException = (GotoException) e;
			String next = gotoException.next;
			interpreter.resetDialogScope();
			if (next.startsWith("#")) {
				currentDialog = Utils.searchDialogByName(dialogs, next.replace(
						"#", ""));
			} else {
				interpreter.resetDocumentScope();
				interpreter.selectedItem = null;
				buildDocument(next);
			}
			launchInterpreter();
		} else if (e instanceof SubmitException) {
			interpreter.resetDocumentScope();
			buildDocument(((SubmitException) e).next);
			interpreter.selectedItem = null;
			launchInterpreter();
		} else if (e instanceof EventException) {
			event(((EventException) e).type);
		}
	}

	public void event(String eventType) throws ScriptException, IOException {
		field = interpreter.selectedItem;
		interpreterListener.doEvent(new InterpreterEvent(this, eventType));
	}

	private void buildDocument(String fileName) throws ScriptException,
			IOException {
		String url = tackWeelFormedUrl(fileName).replaceAll("#", "%23");
		System.err.println(url);
		XmlPage page = getPage(url);
		currentdDocument = page.getXmlDocument();
		dialogs = currentdDocument.getElementsByTagName("form");
		currentDialog = dialogs.item(0);
		Node appplicationRoot = currentdDocument.getElementsByTagName("vxml")
				.item(0).getAttributes().getNamedItem("application");
		if (null != appplicationRoot) {
			String rootUrl = tackWeelFormedUrl(appplicationRoot
					.getTextContent());
			XmlPage rootPage = getPage(rootUrl);
			rootDocument = rootPage.getXmlDocument();
			declareRootScopeVariableIfNeed(rootUrl);
		} else if (!url.equals(currentRootFileName)) {
			interpreter.resetApplicationScope();
		}
		declareDocumentScopeVariableIfNeed(fileName);
	}

	private void declareRootScopeVariableIfNeed(String textContent)
			throws ScriptException, IOException {
		if (!textContent.equals(currentRootFileName)) {
			interpreter.resetApplicationScope();
			interpreter.declareVariable(rootDocument.getElementsByTagName(
					"vxml").item(0).getChildNodes(),
					DefaultInterpreterScriptContext.APPLICATION_SCOPE);
			currentRootFileName = tackWeelFormedUrl(textContent);
		}
	}

	private void declareDocumentScopeVariableIfNeed(String fileName)
			throws ScriptException, IOException {
		if (!fileName.equals(currentFileName)) {
			interpreter.declareVariable(currentdDocument.getElementsByTagName(
					"vxml").item(0).getChildNodes(),
					DefaultInterpreterScriptContext.DOCUMENT_SCOPE);
			currentFileName = tackWeelFormedUrl(fileName);
		}
	}

	private String tackWeelFormedUrl(String relativePath) throws IOException {

		// URL target = new URL(new URL(currentFileName),relativePath);

		if (relativePath.startsWith("http://")
				|| relativePath.startsWith("file://")) {
			return relativePath;
		}

		if (null != InterpreterRequierement.url) {
			return InterpreterRequierement.url + "/" + relativePath;
		}

		return relativePath;
	}

	// we assume that the user is in a context where the word he utters is
	// recognized if it is not the case we use the function nomatch
	public void talk(String sentence) throws ScriptException, IOException {
		try {
			interpreter.utterance(sentence, "'voice'");
			interpreter.execute(Utils.serachItem(interpreter.selectedItem,
					"filled"));
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	public void push(String dtmf) throws ScriptException, IOException {
		try {
			System.err.println(currentFileName + " \t" + "saisi dtmf  " + dtmf
					+ "---->");
			interpreter.utterance(dtmf, "'dtmf'");
			interpreter.execute(Utils.serachItem(interpreter.selectedItem,
					"filled"));
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	public void destinationHangup() throws ScriptException, IOException {
		try {
			interpreter.destinationHangup();
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	public void callerHangDestination() throws ScriptException, IOException {
		try {
			interpreter.callerHangDestination();
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	public void blindTransferSuccess() throws ScriptException, IOException {
		try {
			interpreter.blindTransferSuccess();
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	public void callerHangup() throws IOException, ScriptException {
		try {
			interpreter.callerHangup();
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	public void noAnswer() throws ScriptException, IOException {
		try {
			interpreter.noAnswer();
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	public void maxTimeDisconnect() throws ScriptException, IOException {
		try {
			interpreter.maxTimeDisconnect();
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	public void destinationBusy() throws ScriptException, IOException {
		try {
			interpreter.destinationBusy();
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	public void networkBusy() throws ScriptException, IOException {
		try {
			interpreter.networkBusy();
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}
}