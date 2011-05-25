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
		setPageCreator(new VxmlDefaultPageCreator());
		interpreter.setSessionVariable();
		buildDocument(fileName);
		interpreterListeners.add(new InterpreterEventHandler());
	}

	private String tackWeelFormedUrl(String relativePath) throws IOException {
		if (relativePath.startsWith("http://")
				|| relativePath.startsWith("file://")) {
			return relativePath;
		}

		if (null != currentFileName && currentFileName.startsWith("http://")) {
			URL tempUrl = new URL(currentFileName);

			// System.err.println(tempUrl.getProtocol() + "://"
			// + tempUrl.getHost() + "/" + relativePath);
			return tempUrl.getProtocol() + "://" + tempUrl.getHost() + "/"
					+ relativePath;
		}

		return "file://" + new File(".").getCanonicalPath().toString()
				+ FILE_DIR + relativePath;
	}

	public void launchInterpreter() throws SAXException, IOException,
			DOMException, ScriptException {
		try {

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
			currentDialog = Utils.searchDialogByName(dialogs,
					gotoException.next.replace("#", ""));
			launchInterpreter();
		} else if (e instanceof SubmitException) {
			buildDocument(((SubmitException) e).next);
			launchInterpreter();
		}
	}

	public void noInput() throws ScriptException {
		fireNoInput();
	}

	public void noMatch() throws ScriptException {
		fireNoMatch();
	}

	private void buildDocument(String fileName) throws ScriptException,
			IOException {

		String url = tackWeelFormedUrl(fileName);

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
					"vxml").item(0).getChildNodes(), 3);
			currentRootFileName = tackWeelFormedUrl(textContent);
		}
	}

	private void declareDocumentScopeVariableIfNeed(String fileName)
			throws ScriptException, IOException {
		if (!fileName.equals(currentFileName)) {
			interpreter.declareVariable(currentdDocument.getElementsByTagName(
					"vxml").item(0).getChildNodes());
			currentFileName = tackWeelFormedUrl(fileName);

		}
	}

	private void fireNoInput() throws ScriptException {
		InterpreterEvent interpreterEvent = new InterpreterEvent(this);
		for (Iterator<InterpreterListener> iterator = interpreterListeners
				.iterator(); iterator.hasNext();) {
			InterpreterListener interpreterListener = iterator.next();
			interpreterListener.noInput(interpreterEvent);
		}
	}

	private void fireNoMatch() throws ScriptException {
		InterpreterEvent interpreterEvent = new InterpreterEvent(this);
		for (Iterator<InterpreterListener> iterator = interpreterListeners
				.iterator(); iterator.hasNext();) {
			InterpreterListener interpreterListener = iterator.next();
			interpreterListener.NoMatch(interpreterEvent);
		}
	}

}