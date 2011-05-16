package fr.mbs.vxml.interpreter;

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
	private Document currentdocDocument;
	private Node currentDialog;
	private NodeList dialogs;
	public Interpreter interpreter = new Interpreter();
	private Vector<InterpreterListener> interpreterListeners = new Vector<InterpreterListener>();
	private String url;

	public InterpreterContext(String fileName) throws SAXException, IOException {
		this(FILE_DIR, fileName);
	}

	public InterpreterContext(String url, String fileName) throws SAXException,
			IOException {
		this.url = url;
		buildDocument(fileName);
		interpreterListeners.add(new InterpreterEventHandler());
	}

	public void launchInterpreter() throws SAXException, IOException {
		try {
			interpreter.interpretDialog(currentDialog);
		} catch (InterpreterException e) {
			executionHandler(e);
		} catch (DOMException e) {
			throw new RuntimeException(e);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	public void noInput() throws ScriptException {
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

	public void noMatch() throws ScriptException {
		fireNoMatch();
	}

	private void fireNoInput() throws ScriptException {
		InterpreterEvent interpreterEvent = new InterpreterEvent(this);
		for (Iterator<InterpreterListener> iterator = interpreterListeners
				.iterator(); iterator.hasNext();) {
			InterpreterListener interpreterListener = (InterpreterListener) iterator
					.next();
			interpreterListener.noInput(interpreterEvent);
		}
	}

	private void fireNoMatch() throws ScriptException {
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
			currentdocDocument = builder.parse(url + fileName);
			dialogs = currentdocDocument.getElementsByTagName("form");
			currentDialog = dialogs.item(0);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
}