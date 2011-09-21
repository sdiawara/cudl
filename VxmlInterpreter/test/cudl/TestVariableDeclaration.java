package cudl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.EcmaError;

import cudl.script.InterpreterVariableDeclaration;

public class TestVariableDeclaration {

	private InterpreterVariableDeclaration declaration;

	@Before
	public void setUp() throws IOException {
		declaration = new InterpreterVariableDeclaration();
	}

	@Test
	public void testWeCanDeclareVariableAndRetreViewItValue() throws IOException {
		declaration.declareVariable("variableDocument", "'document'",
				InterpreterVariableDeclaration.DOCUMENT_SCOPE);
		declaration.declareVariable("variableDialog", "'dialog'",
				InterpreterVariableDeclaration.DIALOG_SCOPE);
		declaration.declareVariable("variableAnonyme", "'anonyme'",
				InterpreterVariableDeclaration.ANONYME_SCOPE);

		assertEquals("anonyme", declaration.getValue("variableAnonyme"));
		assertEquals("dialog", declaration.getValue("variableDialog"));
		assertEquals("document", declaration.getValue("variableDocument"));
	}

	@Test(expected = EcmaError.class)
	public void testWhenScopeIsClearVariableDeclaredIsLose() {
		declaration.declareVariable("variableAnonyme", "'anonyme'",
				InterpreterVariableDeclaration.ANONYME_SCOPE);
		declaration.declareVariable("variableDialog", "'dialog'",
				InterpreterVariableDeclaration.DIALOG_SCOPE);
		declaration.declareVariable("variableDocument", "'document'",
				InterpreterVariableDeclaration.DOCUMENT_SCOPE);

		declaration.resetScopeBinding(InterpreterVariableDeclaration.ANONYME_SCOPE);

		assertEquals("document", declaration.getValue("variableDocument"));
		assertEquals("dialog", declaration.getValue("variableDialog"));
		assertEquals("anonyme", declaration.getValue("variableAnonyme"));
	}

	@Test(expected = EcmaError.class)
	public void testWhenVariableIsnotDeclaredItAccessMakeError() {
		declaration.getValue("variableAnonyme");
	}

	@Test
	public void testWhenVariableIsDeclaredInscopeNamedIsItAccesSiblePrefixedWithScopeName() {
		declaration.declareVariable("variable", "'document'", InterpreterVariableDeclaration.DOCUMENT_SCOPE);
		declaration.declareVariable("variable", "'dialog'", InterpreterVariableDeclaration.DIALOG_SCOPE);
		declaration.declareVariable("variable", "'anonyme'", InterpreterVariableDeclaration.ANONYME_SCOPE);

		assertEquals("document", declaration.getValue("document.variable"));
		assertEquals("dialog", declaration.getValue("dialog.variable"));
		assertEquals("anonyme", declaration.getValue("variable"));
	}

	@Test
	public void testwhenWeAssignVariableNameItValeurChange() {
		declaration.declareVariable("variable", "'document'",
				InterpreterVariableDeclaration.DOCUMENT_SCOPE);
		declaration.declareVariable("variable", "'dialog'",
				InterpreterVariableDeclaration.DIALOG_SCOPE);
		declaration.declareVariable("variable", "'anonyme'",
				InterpreterVariableDeclaration.ANONYME_SCOPE);

		declaration.setValue("document.variable", "'document1'");
		declaration.setValue("dialog.variable", "'dialog1'");
		declaration.setValue("variable", "'anonyme1'");

		assertEquals("document1", declaration.getValue("document.variable"));
		assertEquals("dialog1", declaration.getValue("dialog.variable"));
		assertEquals("anonyme1", declaration.getValue("variable"));
	}

	
	@Test
	public void testWhenWeDeclareVariableInScopeDialogItValueCanUseWithAnotherVariableInencompassingScope() {
		declaration.declareVariable("heure_ouverture", "undefined", InterpreterVariableDeclaration.APPLICATION_SCOPE);
		declaration.declareVariable("recupHeureOuverture", "new Object();", InterpreterVariableDeclaration.DIALOG_SCOPE);
		declaration.setValue("recupHeureOuverture.hOuverture", "'non'");
		
		declaration.setValue("heure_ouverture","recupHeureOuverture.hOuverture");
		
		assertEquals("non", declaration.getValue("heure_ouverture"));
	}
	
	@Test
	public void testVariableCanDeclareInAInlineScript() {
		String script = "var d = 'date'";
		declaration.evaluateScript(script, InterpreterVariableDeclaration.ANONYME_SCOPE);

		assertEquals("date", declaration.getValue("d"));
	}

	@Test
	public void testVariableCanDeclareInAInlineScriptAsANamedScopeCanModify() {
		String script = "var d = 'date'";
		declaration.evaluateScript(script, InterpreterVariableDeclaration.ANONYME_SCOPE);

		declaration.setValue("d", "'date1'");

		assertEquals("date1", declaration.getValue("d"));
	}

	@Test
	public void testVariableCanDeclareInAInlineScriptAsANamedScopeCanModifyUsingAscopeName() {
		String script = "var d = 'date'";
		declaration.evaluateScript(script, InterpreterVariableDeclaration.DIALOG_SCOPE);

		declaration.setValue("dialog.d", "'date1'");

		assertEquals("date1", declaration.getValue("d"));
	}
}
