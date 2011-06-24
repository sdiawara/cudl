package cudl;

import javax.script.ScriptException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import cudl.script.InterpreterVariableDeclaration;


public class InterpreterException extends Exception {
    //Should'nt this class also have package visibility ? 
}

class TransferException extends InterpreterException {
}

class FilledException extends InterpreterException {
}

class ExitException extends InterpreterException {
}

class DisconnectException extends InterpreterException {
}

class GotoException extends InterpreterException {
    public String next;

    public GotoException(String string) {
        this.next = string;
    }
}

class EventException extends InterpreterException {
    public String type;

    public EventException(String type) {
        this.type = type;
    }
}

class SubmitException extends InterpreterException {
    public String next;

    public SubmitException(Node node, InterpreterVariableDeclaration declaration)
            throws ScriptException {
        NamedNodeMap attributes = node.getAttributes();
        this.next = attributes.getNamedItem("next").getNodeValue();

        Node namedItem = attributes.getNamedItem("namelist");
        String[] namelist = namedItem != null ? namedItem.getNodeValue().split(
                " ") : new String[0];
        String urlSuite = "?";
        for (int i = 0; i < namelist.length; i++) {
            String declareVariable = namelist[i];

            urlSuite += declareVariable + "="
                    + declaration.getValue(declareVariable) + "&";

        }
        this.next += urlSuite;
    }
}