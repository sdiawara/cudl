package cudl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cudl.script.InterpreterScriptContext;
import cudl.script.InterpreterVariableDeclaration;
import cudl.utils.Utils;

class WIPContext {
	private final String location;
	private final InterpreterVariableDeclaration declaration;
	private DocumentBuilder documentBuilder;
	private URLConnection connection;
	private String cookies;
	private Object currentRootFileName;
	private String currentFileName;
	private Document rootDocument;
	private Document currentdDocument;
	private Node currentDialog;
	private Node selectedFormItem;
	private String transferDestination;
	private boolean hangup;
	private List<Node> grammarActive;
	private String next;
	private boolean inSubdialog;
	private Node lastDialog;
	private boolean canExecuteFilled;

	// String method;

	WIPContext(String location, InterpreterVariableDeclaration declaration)
			throws ParserConfigurationException, MalformedURLException,
			IOException, ScriptException, SAXException {
		this.location = location;
		this.declaration = declaration;
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		documentBuilder = builderFactory.newDocumentBuilder();
		connection = new URL(location).openConnection();
		cookies = connection.getHeaderField("Set-Cookie");
		buildDocument(location);
	}

	void buildDocument(String fileName) throws ScriptException, IOException,
			SAXException {
		String url = Utils.tackWeelFormedUrl(location, fileName);
		System.err.println(url);

		connection = new URL(url).openConnection();
		if (cookies != null)
			connection.setRequestProperty("Cookie", cookies);

		currentdDocument = documentBuilder.parse(connection.getInputStream());
		NodeList dialogs = currentdDocument.getElementsByTagName("form");
		if (getNext() != null) {
			currentDialog = Utils.searchDialogByName(dialogs, getNext());
		} else
			currentDialog = dialogs.item(0);
		Node appplicationRoot = currentdDocument.getElementsByTagName("vxml")
				.item(0).getAttributes().getNamedItem("application");
		if (null != appplicationRoot) {
			String rootUrl = Utils.tackWeelFormedUrl(location, appplicationRoot
					.getTextContent());
			rootDocument = documentBuilder.parse(rootUrl);
			declareRootScopeVariableIfNeed(rootUrl);
		} else if (!url.equals(currentRootFileName)) {
			declaration
					.resetScopeBinding(InterpreterScriptContext.APPLICATION_SCOPE);
		}
		declareDocumentScopeVariableIfNeed(fileName);
	}

	private void declareRootScopeVariableIfNeed(String textContent)
			throws ScriptException, IOException {
		if (!textContent.equals(currentRootFileName)) {
			declaration
					.resetScopeBinding(InterpreterScriptContext.APPLICATION_SCOPE);
			NodeList childNodes = rootDocument.getElementsByTagName("vxml")
					.item(0).getChildNodes();
			declareVariable(childNodes,
					InterpreterScriptContext.APPLICATION_SCOPE);
			currentRootFileName = Utils
					.tackWeelFormedUrl(location, textContent);
		}
	}

	private void declareDocumentScopeVariableIfNeed(String fileName)
			throws ScriptException, IOException {
		if (!fileName.equals(getCurrentFileName())) {
			declaration
					.resetScopeBinding(InterpreterScriptContext.DOCUMENT_SCOPE);
			NodeList childNodes = currentdDocument.getElementsByTagName("vxml")
					.item(0).getChildNodes();
			declareVariable(childNodes, InterpreterScriptContext.DOCUMENT_SCOPE);
			setCurrentFileName(Utils.tackWeelFormedUrl(location, fileName));
		}
	}

	private void declareVariable(NodeList childNodes, int scope)
			throws ScriptException, MalformedURLException, IOException {
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			System.err.println(child.getNodeName());
			if (child.getNodeName().equals("var")) {
				String name = Utils.getNodeAttributeValue(child, "name");
				String value = Utils.getNodeAttributeValue(child, "expr");
				if (value == null)
					value = "undefined";
				declaration.declareVariable(name, value, scope);
			} else if (child.getNodeName().equals("script")) {
				String src = Utils.getNodeAttributeValue(child, "src");
				if (src != null) {
					declaration.evaluateFileScript(Utils.tackWeelFormedUrl(
							location, src), scope);
				} else
					declaration.evaluateScript(child.getTextContent(), scope);
			}
		}
	}

	public Node getCurrentDialog() {
		return currentDialog;
	}

	void setCurrentDialog(Node dialog) {
		currentDialog = dialog;
	}

	public boolean getCurrentChange() {
		return false;
	}

	Node getSelectedFormItem() {
		return selectedFormItem;
	}

	void setSelectedFormItem(Node selectedFormItem) {
		this.selectedFormItem = selectedFormItem;
		// notifyObserver
	}

	String getLocation() {
		return location;
	}

	InterpreterVariableDeclaration getDeclaration() {
		return declaration;
	}

	public void setTransfertDestination(String object) {
		this.transferDestination = object;
		// notifyobserver
	}

	public String getTransferDestination() {
		return transferDestination;

	}

	public void setHangup(boolean hangup) {
		this.hangup = hangup;
	}

	public boolean isHangup() {
		return hangup;
	}

	public Document getRootDocument() {
		return rootDocument;
	}

	public void setGrammarActive(List<Node> grammarActive) {
		this.grammarActive = grammarActive;
	}

	public List<Node> getGrammarActive() {
		return grammarActive;
	}

	public void setNexted(String next) {
		this.next = next;
	}

	public String getNext() {
		return next;
	}

	public boolean isInSubdialog() {
		return inSubdialog;
	}

	public void setInSubdialog(boolean inSubdialog) {
		this.inSubdialog = inSubdialog;
	}

	public Node getLastDialog() {
		return lastDialog;
	}

	public void setLastDialog(Node lastDialog) {
		this.lastDialog = lastDialog;
	}

	public void setCanExecuteFilled(boolean canExecuteFilled) {
		this.canExecuteFilled = canExecuteFilled;
	}

	public boolean canExecuteFilled() {
		return this.canExecuteFilled;
	}

	public void setCurrentFileName(String currentFileName) {
		this.currentFileName = currentFileName;
	}

	public String getCurrentFileName() {
		return currentFileName;
	}
}
