package fr.mbs.vxml.interpreter.w3c;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.script.ScriptException;

import org.junit.Before;
import org.junit.Test;

import fr.mbs.vxml.interpreter.InterpreterContext;
import fr.mbs.vxml.interpreter.execption.InterpreterException;
import fr.mbs.vxml.utils.InterpreterRequierement;

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
			// add("w3c/262.txml");

			// FIA ends when it encounters an <goto>.
			add("w3c/assert111.txml");

			// Using the goto nextitem will force the FIA to immediately
			// transition to the chosen form item.
			// FIXME: restore the original file
			add("w3c/assert154.txml");

			// FIA ends when it encounters an <submit>.
			add("w3c/assert112.txml");

			// FIA ends when it encounters an <exit>.
			// add("w3c/assert113.txml");

			// FIA ends when it encounters an <return>.
			// FIXME: Implemeent dialog
			add("w3c/assert114.txml");

			// The next dialog is determined by the previous dialog.
			add("w3c/assert_48_1.txml");

			// Application root document variables are available for use by the
			// leaf document.

			add("w3c/a59leaf.txml");

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
			// add("w3c/assertion-398.txml");

			// and elements that are children of the document's element create
			// their variables at document scope. They are no longer accessible
			// when another document is entered.
			//add("w3c/400.txml");

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
		//	add("w3c/399main.txml");

			// A element not specifying a 'bridge' attribute is executed as a
			// blind transfer.
			add("w3c/294.txml");

			// Upon a successful blind transfer, a
			// 'connection.disconnect.transfer' event is thrown and the transfer
			// name variable remains undefined.
			add("w3c/298.txml");

			// A element specifying a 'cond' attribute that evaluates to false
			// upon selection of the element by the FIA is not executed.
			add("w3c/293.txml");
		}
	};

	@Before
	public void setUp() throws IOException {
		InterpreterRequierement.url = "file://"
				+ new File(".").getCanonicalPath() + "/test/docVxml1/";
	}

	@Test
	public void w3cIRTest() throws IOException, ScriptException {
		int count = 0;
		for (Iterator<String> iterator = fileNames.iterator(); iterator
				.hasNext();) {
			String fileName = iterator.next();

			interpreterContext = new InterpreterContext(fileName);

			interpreterContext.launchInterpreter();

			if (!(interpreterContext.interpreter.w3cNodeConfSuite.get(0)
					.equals("[conf:pass: null]"))) {
				System.out
						.println(interpreterContext.interpreter.w3cNodeConfSuite
								.get(0));
				System.out.println(count + " tests of " + fileNames.size());
			}

			assertTrue(interpreterContext.interpreter.w3cNodeConfSuite.get(0)
					.equals("[conf:pass: null]"));
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

	@Test
	public void w3cVariableScope() throws IOException, ScriptException {
		// If the last main FIA loop did not result in a goto nextitem
		// and there is no form item which is eligible to be visited
		// then an implicit exit is generated.

		// Read file
		interpreterContext = new InterpreterContext("w3c/assert165.txml");
		interpreterContext.launchInterpreter();

		assertTrue(interpreterContext.interpreter.w3cNodeConfSuite.isEmpty());
	}

	@Test()
	public void w3cDefaultValueIsUndefined() throws IOException,
			ScriptException {
		// The default value of the attribute is ECMAScript undefined.
		// The file (w3c/235.txml) has been modified to adapt to interpret the
		// tests

		interpreterContext = new InterpreterContext("w3c/235.txml");
		interpreterContext.launchInterpreter();
		interpreterContext.event("noinput");

		// System.out.println(interpreterContext.interpreter.w3cNodeConfSuite);
		assertTrue(interpreterContext.interpreter.w3cNodeConfSuite.get(0)
				.equals("[conf:pass: null]"));
	}

	@Test()
	public void w3cFilledContainIsexecutedAfterUserInput() throws IOException,
			ScriptException {
		// . The input item 'field' may contain the filled element. Filled
		// elements contain an action to execute after the result input item
		// variable is filled in.

		interpreterContext = new InterpreterContext("w3c/1037.txml");
		interpreterContext.launchInterpreter();
		interpreterContext.talk("'toto'");

	
		assertTrue(interpreterContext.interpreter.w3cNodeConfSuite.get(0)
				.equals("[conf:pass: null]"));
	}
}
