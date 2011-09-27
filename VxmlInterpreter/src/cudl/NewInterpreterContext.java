package cudl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class NewInterpreterContext {
	private DocumentBuilder documentBuilder;
	private URLConnection connection;
	private final String serverLocation;
	private String cookies;

	public NewInterpreterContext(String url) throws ParserConfigurationException, MalformedURLException, IOException {
		this.serverLocation = url;
		documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		connection = new URL(url).openConnection();
		cookies = connection.getHeaderField("Set-Cookie");
	}

	public Document getDocumentFromUri(String url) throws SAXException, IOException {
		return documentBuilder.parse(url);
	}
}
