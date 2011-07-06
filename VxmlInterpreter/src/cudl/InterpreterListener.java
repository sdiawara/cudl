package cudl;

import java.io.IOException;
import java.util.EventListener;

import javax.script.ScriptException;

import org.xml.sax.SAXException;

import cudl.event.InterpreterEvent;

public interface InterpreterListener extends EventListener {
	public void doEvent(InterpreterEvent interpreterEvent,Executor executor)
			throws ScriptException, IOException, SAXException, InterpreterException;
}