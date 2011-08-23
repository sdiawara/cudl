package cudl;

import java.io.IOException;
import java.util.EventListener;

import javax.script.ScriptException;

import org.xml.sax.SAXException;

import cudl.event.InterpreterEvent;

interface InterpreterListener extends EventListener {
	void doEvent(InterpreterEvent interpreterEvent) throws ScriptException,
			IOException, SAXException, InterpreterException;
}