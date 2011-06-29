package cudl.event;

import java.io.IOException;
import java.util.EventListener;

import javax.script.ScriptException;

import org.xml.sax.SAXException;

import cudl.InterpreterException;

public interface InterpreterListener extends EventListener {
	public void doEvent(InterpreterEvent interpreterEvent)
			throws ScriptException, IOException, SAXException, InterpreterException;
}