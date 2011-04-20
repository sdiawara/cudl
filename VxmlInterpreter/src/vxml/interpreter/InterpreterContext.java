package vxml.interpreter;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import vxml.interpreter.execption.GotoException;
import vxml.interpreter.execption.InterpreterExecption;

public class InterpreterContext {
	public final static String FILE_DIR = "/home/sdiawara/workspace/VoiceXmlInterpreter/test/docVxml1/";
	private String currentFileName;
	private Node currentDialog;

	public Node selectedItem;
	private Document doc;
	public Interpreter interpreter = new Interpreter();

	public InterpreterContext(String fileName) throws SAXException, IOException {
		this.currentFileName = fileName;
		buildDocument();
	}

	private void buildDocument() throws SAXException, IOException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		try {
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			doc = builder.parse(FILE_DIR + currentFileName);
			currentDialog = doc.getElementsByTagName("form").item(0);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public Node getCurrentDialog() {
		return currentDialog;	
	}

	public void launchInterpreter() {
		try {
			interpreter.interpretDialog(currentDialog);
		} catch (InterpreterExecption e) {
			if (e instanceof GotoException) {
				launchInterpreter();
			} else if (e instanceof GotoException) {
				finalProcessing();
			}
		}
	}

	private void finalProcessing() {
		System.err.println("TODO : implement finalProcessing methode");
	}
}
