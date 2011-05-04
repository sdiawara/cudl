package vxml.interpreter;

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

import vxml.interpreter.event.InterpreterEvent;
import vxml.interpreter.event.InterpreterEventHandler;
import vxml.interpreter.event.InterpreterListener;
import vxml.interpreter.execption.GotoException;
import vxml.interpreter.execption.InterpreterException;
import vxml.interpreter.execption.SubmitException;
import vxml.utils.Utils;

public class InterpreterContext {
	public final static String FILE_DIR = "test/docVxml1/";
	private Document currentdocDocument;
	private Node currentDialog;
	private NodeList dialogs;
	public Interpreter interpreter = new Interpreter();
	private Vector<InterpreterListener> interpreterListeners = new Vector<InterpreterListener>();

	public InterpreterContext(String fileName) throws SAXException, IOException {
		buildDocument(fileName);
		interpreterListeners.add(new InterpreterEventHandler());
	}

	public void launchInterpreter() throws SAXException, IOException {
		try {
			interpreter.interpretDialog(currentDialog);
		} catch (InterpreterException e) {
			executionHandler(e);
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}

	public void noInput() {
		fireNoInput();
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
			IOException {
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

	public void noMatch() {
		fireNoMatch();
	}

	private void fireNoInput() {
		InterpreterEvent interpreterEvent = new InterpreterEvent(this);
		for (Iterator<InterpreterListener> iterator = interpreterListeners
				.iterator(); iterator.hasNext();) {
			InterpreterListener interpreterListener = (InterpreterListener) iterator
					.next();
			interpreterListener.noInput(interpreterEvent);
		}
	}

	private void fireNoMatch() {
		InterpreterEvent interpreterEvent = new InterpreterEvent(this);
		for (Iterator<InterpreterListener> iterator = interpreterListeners
				.iterator(); iterator.hasNext();) {
			InterpreterListener interpreterListener = (InterpreterListener) iterator
					.next();
			interpreterListener.NoMatch(interpreterEvent);
		}
	}

	private void buildDocument(String fileName) throws SAXException,
			IOException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		try {
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			currentdocDocument = builder.parse(FILE_DIR + fileName);
			dialogs = currentdocDocument.getElementsByTagName("form");
			currentDialog = dialogs.item(0);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

}