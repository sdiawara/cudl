package fr.mbs.vxml.interpreter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.mbs.vxml.interpreter.event.InterpreterEvent;
import fr.mbs.vxml.interpreter.event.InterpreterEventHandler;
import fr.mbs.vxml.interpreter.event.InterpreterListener;
import fr.mbs.vxml.interpreter.execption.GotoException;
import fr.mbs.vxml.interpreter.execption.InterpreterException;
import fr.mbs.vxml.interpreter.execption.SubmitException;
import fr.mbs.vxml.utils.Utils;

public class InterpreterContext {
	public final static String FILE_DIR = "test/docVxml1/";
	private Document currentdDocument;
	private Node currentDialog;
	private NodeList dialogs;
	public Interpreter interpreter = new Interpreter();
	private Vector<InterpreterListener> interpreterListeners = new Vector<InterpreterListener>();
	private String url;
	public Node field;
	private Object currentFileName;

	public InterpreterContext(String fileName) throws SAXException,
			IOException, DOMException, ScriptException {
		this(FILE_DIR, fileName);
	}

	public InterpreterContext(String url, String fileName) throws SAXException,
			IOException, DOMException, ScriptException {
		this.url = url;
		buildDocument(fileName);
		interpreterListeners.add(new InterpreterEventHandler());
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

	private void buildDocument(String fileName) throws SAXException,
			IOException, DOMException, ScriptException {

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		try {
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			currentdDocument = builder.parse(url + fileName);
			declareDocumentScopeVariableIfNeed(fileName);
			dialogs = currentdDocument.getElementsByTagName("form");
			currentDialog = dialogs.item(0);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	private void declareDocumentScopeVariableIfNeed(String fileName)
			throws FileNotFoundException, ScriptException {
		if (!fileName.equals(currentFileName)) {
			interpreter.declareVariable(currentdDocument.getElementsByTagName(
					"vxml").item(0).getChildNodes());
		
			currentFileName = fileName;

			
		}
	}

	private void fireNoInput() throws ScriptException {
		InterpreterEvent interpreterEvent = new InterpreterEvent(this);
		for (Iterator<InterpreterListener> iterator = interpreterListeners
				.iterator(); iterator.hasNext();) {
			InterpreterListener interpreterListener = iterator
					.next();
			interpreterListener.noInput(interpreterEvent);
		}
	}

	private void fireNoMatch() throws ScriptException {
		InterpreterEvent interpreterEvent = new InterpreterEvent(this);
		for (Iterator<InterpreterListener> iterator = interpreterListeners
				.iterator(); iterator.hasNext();) {
			InterpreterListener interpreterListener = iterator
					.next();
			interpreterListener.NoMatch(interpreterEvent);
		}
	}

}