package fr.mbs.vxml.interpreter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import javax.script.ScriptException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.mbs.vxml.interpreter.event.InterpreterEvent;
import fr.mbs.vxml.interpreter.event.InterpreterEventHandler;
import fr.mbs.vxml.interpreter.event.InterpreterListener;
import fr.mbs.vxml.interpreter.execption.GotoException;
import fr.mbs.vxml.interpreter.execption.InterpreterException;
import fr.mbs.vxml.interpreter.execption.SubmitException;
import fr.mbs.vxml.script.DefaultInterpreterScriptContext;
import fr.mbs.vxml.utils.Utils;
import fr.mbs.vxml.utils.VxmlDefaultPageCreator;

public class InterpreterContext extends WebClient {
	public final static String FILE_DIR = "/test/docVxml1/";
	private Document currentdDocument;
	private Node currentDialog;
	private Document rootDocument;
	private NodeList dialogs;
	public Interpreter interpreter = new Interpreter();
	private Vector<InterpreterListener> interpreterListeners = new Vector<InterpreterListener>();

	public Node field;
	private String currentFileName;
	private String currentRootFileName;

	public InterpreterContext(String fileName) throws SAXException,
			IOException, DOMException, ScriptException {
		System.err.println(fileName);
		interpreter.setLocation(fileName
				.substring(0, fileName.lastIndexOf("/") == -1 ? 0 : fileName
						.lastIndexOf("/")));
		setPageCreator(new VxmlDefaultPageCreator());
		buildDocument(fileName);
		interpreterListeners.add(new InterpreterEventHandler());
	}

	public void launchInterpreter() throws SAXException, IOException,
			DOMException, ScriptException {
		try {
			interpreter
					.setSessionVariable("/home/sdiawara/workspace/cudl/VxmlInterpreter/src/js/session.js");
			interpreter.interpretDialog(currentDialog);
			field = interpreter.selectedItem;
			interpreter.selectedItem = null;
		} catch (InterpreterException e) {
			executionHandler(e);
		}
	}

	public synchronized void addInterpreterListener(
			InterpreterListener interpreterListener) {
		if (interpreterListeners.contains(interpreterListener))
			return;
		interpreterListeners.add(interpreterListener);
	}

	public synchronized void removeInterpreterListener(
			InterpreterListener interpreterListener) {
		interpreterListeners.remove(interpreterListener);
	}

	public void executionHandler(InterpreterException e) throws SAXException,
			IOException, DOMException, ScriptException {
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
				interpreter.resetDocumentScope();
				buildDocument(next);
			}

			launchInterpreter();
		} else if (e instanceof SubmitException) {
			interpreter.resetDocumentScope();
			buildDocument(((SubmitException) e).next);
			interpreter.selectedItem = null;
			launchInterpreter();
		}
	}

	public void noInput() throws ScriptException, DOMException, IOException {
		fireNoInput();
	}

	public void noMatch() throws ScriptException, DOMException, IOException {
		fireNoMatch();
	}

	private void buildDocument(String fileName) throws ScriptException,
			IOException {

		String url = tackWeelFormedUrl(fileName);
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
		}
		declareDocumentScopeVariableIfNeed(fileName);
	}

	private void declareRootScopeVariableIfNeed(String textContent)
			throws DOMException, ScriptException, IOException {
		if (!textContent.equals(currentRootFileName)) {
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

	private void fireNoInput() throws ScriptException, DOMException,
			IOException {
		InterpreterEvent interpreterEvent = new InterpreterEvent(this);
		for (Iterator<InterpreterListener> iterator = interpreterListeners
				.iterator(); iterator.hasNext();) {
			InterpreterListener interpreterListener = iterator.next();
			interpreterListener.noInput(interpreterEvent);
		}
	}

	private void fireNoMatch() throws ScriptException, DOMException,
			IOException {
		InterpreterEvent interpreterEvent = new InterpreterEvent(this);
		for (Iterator<InterpreterListener> iterator = interpreterListeners
				.iterator(); iterator.hasNext();) {
			InterpreterListener interpreterListener = iterator.next();
			interpreterListener.NoMatch(interpreterEvent);
		}
	}

	private String tackWeelFormedUrl(String relativePath) throws IOException {
		if (relativePath.startsWith("http://")
				|| relativePath.startsWith("file://")) {
			return relativePath;
		}

		if (null != currentFileName && currentFileName.startsWith("http://")) {
			URL tempUrl = new URL(currentFileName);
			return tempUrl.getProtocol() + "://" + tempUrl.getHost() + "/"
					+ relativePath;
		}

		return "file://" + new File(".").getCanonicalPath().toString()
				+ FILE_DIR + relativePath;
	}
}