package cudl.w3c;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.script.ScriptException;

import org.junit.Before;
import org.junit.Test;

import cudl.InterpreterContext;
import cudl.utils.InterpreterRequierement;

public class W3cInterpreterTest {
	private InterpreterContext interpreterContext;
	private List<String> fileNames = new ArrayList<String>() {
		{
			/*
			 * These test file is from
			 * http://www.w3.org/Voice/2004/vxml-ir/#test_api
			 */

			// The interpreter must be able to freely sequence TTS and audio
			// output.
			add("w3c/a002.txml");

			// The interpreter executes content contained in the block.
			add("w3c/259.txml");

			// The interpreter visits the block when the cond attribute
			// evaluates to true and the form item variable associated with
			// the block is undefined.
			add("w3c/260.txml");

			// The interpreter ignores the block when the form item variable
			// associated with the block is defined via expr.
			add("w3c/261.txml");

			// The interpreter ignores the block when the form item variable
			// associated with the block is set via an assign.
			add("w3c/262.txml");

			// FIA ends when it encounters an <goto>.
			add("w3c/assert111.txml");

			// Using the goto nextitem will force the FIA to immediately
			// transition to the chosen form item.
			// FIXME: restore the original file
			add("w3c/assert154.txml");

			// FIA ends when it encounters an <submit>.
			add("w3c/assert112.txml");

			// FIA ends when it encounters an <return>.
			// FIXME: Implemeent dialog
			add("w3c/assert114.txml");

			// A variable declared at document scope is accessible within an
			// anonymous scope contained within the same document.
			add("w3c/510.txml");

			// Common ECMAScript code can be defined in the application root and
			// used in leaf documents.
			add("w3c/a61leaf.txml");

			// If the expr attribute specifies a valid ECMAScript expression,
			// the value element evaluates it correctly.
			// FIXME : script evaluation
			add("w3c/377.txml");

			// VoiceXML variables and ECMAScript variables are contained in the
			// same variable space.
			add("w3c/assertion390.txml");

			// VoiceXML variables can be used in a . Variables defined in a can
			// be used in VoiceXML.
			add("w3c/assertion391.txml");

			// Variables declared without an explicit initial value are
			// initialized to ECMAScript undefined.
			add("w3c/394.txml");

			// Variable references match the closest enclosing scope.
			add("w3c/408.txml");

			// Variables in session scope can be read but not written by
			// VoiceXML documents.
			add("w3c/assertion-398.txml");

			// and elements that are children of the document's element create
			// their variables at document scope. They are no longer accessible
			// when another document is entered.
			// add("w3c/400.txml");

			// If specified, the value of the attribute is evaluated and serves
			// as the form item variable's initial value.
			add("w3c/234.txml");

			// When declaring the same variable multiple times with different
			// initial values in the same scope, declarations will apply in
			// document order.
			add("w3c/513.txml");

			// When expr is set to a valid expression in an assign, the named
			// variable is set correctly.
			add("w3c/514.txml");

			// "session", "application", "document", and "dialog" are not
			// reserved words.
			add("w3c/406.txml");

			// Each scope contains a predefined variable whose name is the same
			// as the scope that refers to the scope itself.
			add("w3c/405.txml");

			// FIXME: ADD normalization to scripe
			// add("w3c/399main.txml");

			// A VoiceXML document can initiate a transfer to another entity
			// using the tag, such that the Interpreter remains connected to the
			// original caller and interpretation resumes upon termination of
			// the transfer.
			add("w3c/288.txml");

			// A VoiceXML document can initiate a transfer to another entity
			// using the tag, such that the Interpreter disconnects from the
			// caller immediately upon attempting the transfer and continues
			// execution as it would under termination of the Session.
			add("w3c/289.txml");

			// A bridged transfer can contain speech grammars such that the
			// interpreter listens to the original caller for the duration of
			// the transfer, terminating the transfer as soon as a spoken
			// utterance matches an active speech grammar.
			add("w3c/290.txml");

			// A element specifying a 'cond' attribute that evaluates to false
			// upon selection of the element by the FIA is not executed.
			add("w3c/293.txml");

			// A element not specifying a 'bridge' attribute is executed as a
			// blind transfer.
			add("w3c/294.txml");

			// A bridged transfer specifying a 'connecttimeout' attribute with a
			// W3C time specification will terminate a transfer attempt if the
			// destination entity cannot be connected to within that period of
			// time.
			add("w3c/295.txml");

			// A bridged transfer specifying a 'maxtime' attribute with a W3C
			// time specification will terminate a transfer after that period of
			// time has elapsed after connecting to the destination entity if it
			// has not already been terminated for other reasons.
			add("w3c/296.txml");

			// Upon a successful blind transfer, a
			// 'connection.disconnect.transfer' event is thrown and the transfer
			// name variable remains undefined.
			add("w3c/298.txml");

			// If the originating caller hangs up during a bridged transfer, a
			// 'connection.disconnect.hangup' event is thrown and the transfer
			// name variable remains undefined.
			add("w3c/300.txml");

			// If the Interpreter is unable to connect to the destination entity
			// when attempting a transfer because it is busy, the transfer name
			// variable is filled with the value 'busy'.
			add("w3c/301.txml");

			// If the Interpreter is unable to connect to the destination entity
			// when attempting a transfer because network is busy, the transfer
			// name variable is filled with the value 'network_busy'.
			add("w3c/302.txml");

			// If a URI does not refer to a document, the current document is
			// assumed.
			add("w3c/a11.txml");

			// If a URI does not refer to a dialog, the first dialog is assumed.
			add("w3c/a12a.txml");

			// An application root document's variables are defined and
			// reachable via the application scope upon the loading of a
			// document that specifies it as the application root.
			add("w3c/a24.txml");

			// An application root document's variables are not reinitialized as
			// the user transitions between documents that both specify it as
			// the application root.
			add("w3c/a25a.txml");

			// An application root document's variables are no longer reachable
			// from the application scope when the user transitions to a
			// document not in that application.
			add("w3c/a26a.txml");

			// A document may have var elements.
			add("w3c/42.txml");

			// A document may have script elements.
			add("w3c/assert_43_1.txml");

			// A document may have property elements.
			add("w3c/assert_44_1.txml");

			// A document may have catch elements.
			add("w3c/assert_45_1.txml");

			// The next dialog is determined by the previous dialog.
			add("w3c/assert_48_1.txml");

			// The interpreter supports having an application root document and
			// an application leaf document.
			add("w3c/a56-leaf.txml");

			// When a leaf document causes a root document to be loaded, none of
			// the dialogs in the root document are executed.
			add("w3c/a58-leaf.txml");

			// Application root document variables are available for use by the
			// leaf document.
			add("w3c/a59leaf.txml");

			// Application root catch handlers are default handlers for leaf
			// documents.
			add("w3c/a62-leaf.txml");

			// When transitioning between two leaf documents that both specify
			// the same application fully resolved URI then the transition must
			// preserve the application root document's variables for use by the
			// second leaf document.
			add("w3c/a72-var-driver.txml");

			// A transition from an application leaf document to its own
			// application root document caused by a 'goto' must preserve the
			// application root document's variables for use by the root
			// document.
			// FIXME : GOTO
			// add("w3c/a73-var-driver.txml");
			// ADD file
			// add("w3c/a75-var-driver.txml");

			// If a transition occurs as the result of a submit between an
			// application leaf document and its own application root document
			// the application root document's variables must be re-initialized
			add("w3c/a74-var-driver.txml");

			// If a transition occurs from an application root document to a
			// different application root document it must initialize the new
			// application root document and use the new application root
			// document's variables.
			add("w3c/a76-var-driver.txml");

			// While executing a filled, if a throw is encountered the remaining
			// filled actions are skipped.
			add("w3c/assert1148.txml");

			// . The input item 'field' may contain the filled element. Filled
			// elements contain an action to execute after the result input item
			// variable is filled in.
			add("w3c/1037.txml");

			// The default value of the attribute is ECMAScript undefined.
			add("w3c/235.txml");

			// When the interpreter executes a disconnect element, it must drop
			// the call.
			add("w3c/552.txml");

			// The use of the log element has no side-effects on interpretation.
			add("w3c/562.txml");

			// When the namelist attribute of the clear element specifies a
			// specific set of one or more form item variables, only those form
			// items are cleared.
			// add("w3c/518.txml");

			// When the namelist attribute of the clear element is omitted, all
			// form items in the current form are cleared.
			// add("w3c/519.txml");

			// A form may contain form items, which are subdivided into input
			// items ( <field>, <record>, <transfer>, <object>, <subdialog>) and
			// control items (<block> and <initial>).
			add("w3c/assert93.txml");

			// A form may contain <var> elements.
			add("w3c/assert96.txml");

			// The interpreter throws the named event.
			add("w3c/431.txml");

			// The interpreter observes event counting when an
			// application-defined event is thrown.
			// add("w3c/432.txml");

			// An implementation platform must support text-to-speech
			add("w3c/a626.txml");
		}

	};

	@Before
	public void setUp() throws IOException {
		InterpreterRequierement.url = "file://"
				+ new File(".").getCanonicalPath() + "/test/docVxml/";
	}

	@Test()
	public void w3cIRTest() throws IOException, ScriptException {

		InterpreterRequierement.sessionFileName = "file://"
				+ new File(".").getCanonicalPath() + "/session.js";
		int count = 0;
		for (Iterator<String> iterator = fileNames.iterator(); iterator
				.hasNext();) {
			String fileName = iterator.next();

			interpreterContext = new InterpreterContext(fileName);

			interpreterContext.launchInterpreter();

			String tmp = interpreterContext.interpreter.w3cNodeConfSuite.get(0);
			System.out.println("tmp =" + tmp);
			if (tmp.contains("transfer")) {
				if (tmp.contains("blind"))
					interpreterContext.blindTransferSuccess();
				else if (fileName.endsWith("288.txml")) {
					interpreterContext.destinationHangup();
				} else if (fileName.endsWith("290.txml")) {
					interpreterContext.callerHangDestination();
				} else if (fileName.endsWith("300.txml")) {
					interpreterContext.callerHangup(0);
				} else if (fileName.endsWith("295.txml")) {
					interpreterContext.noAnswer();
				} else if (fileName.endsWith("296.txml")) {
					interpreterContext.maxTimeDisconnect();
				} else if (fileName.endsWith("301.txml")) {
					interpreterContext.destinationBusy();
				} else if (fileName.endsWith("302.txml")) {
					interpreterContext.networkBusy();
				}

				tmp = interpreterContext.interpreter.w3cNodeConfSuite.get(1);
			}

			if (!(tmp.equals("[conf:pass: null]"))) {
				System.out.println(tmp);
				System.out.println(count + " tests of " + fileNames.size());
			}

			assertTrue(tmp.equals("[conf:pass: null]"));
			count++;
			// System.out.println(fileName + " test pass");
		}
		System.out.println(count + " tests of " + fileNames.size());
	}

	@Test
	public void w3cManual1() throws IOException, ScriptException {
		// If the last main FIA loop did not result in a goto nextitem
		// and there is no form item which is eligible to be visited
		// then an implicit exit is generated.

		// Read file
		interpreterContext = new InterpreterContext("w3c/assert165.txml");
		interpreterContext.launchInterpreter();

		assertTrue(interpreterContext.interpreter.w3cNodeConfSuite.isEmpty());
	}
}
