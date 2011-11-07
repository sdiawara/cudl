package cudl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.ScriptableObject;

import cudl.script.InterpreterVariableDeclaration;

public class TestVariableDeclaration {

	private InterpreterVariableDeclaration declaration;

	@Before
	public void setUp() throws IOException {
		declaration = new InterpreterVariableDeclaration();
	}

	@Test
	public void testWeCanDeclareVariableAndRetreViewItValue()
			throws IOException {
		declaration.enterScope();
		declaration.declareVariableNew("variableDocument", "'document'");
		declaration.enterScope();
		declaration.declareVariableNew("variableDialog", "'dialog'");
		declaration.enterScope();
		declaration.declareVariableNew("variableAnonyme", "'anonyme'");

		assertEquals("anonyme", declaration.getValueNew("variableAnonyme"));
		assertEquals("dialog", declaration.getValueNew("variableDialog"));
		assertEquals("document", declaration.getValueNew("variableDocument"));
	}

	@Test(expected = EcmaError.class)
	public void testWhenScopeIsClearVariableDeclaredIsLose() {
		declaration.enterScope();
		declaration.declareVariableNew("variableDocument", "'document'");

		declaration.enterScope();
		declaration.declareVariableNew("variableDialog", "'dialog'");

		declaration.enterScope();
		declaration.declareVariableNew("variableAnonyme", "'anonyme'");
		declaration.exitScope(); // this exit current scope et clean then

		assertEquals("document", declaration.getValueNew("variableDocument"));
		assertEquals("dialog", declaration.getValueNew("variableDialog"));
		assertEquals("anonyme", declaration.getValueNew("variableAnonyme"));
	}

	@Test(expected = EcmaError.class)
	public void testWhenVariableIsnotDeclaredItAccessMakeError() {
		declaration.enterScope();
		declaration.enterScope();
		declaration.enterScope();
		declaration.getValueNew("variableAnonyme");
	}

	@Test(expected = EcmaError.class)
	public void testWhenVariableIsnotDeclaredItAccessWithScriptMakeError() {
		declaration.enterScope();
		declaration.enterScope();
		declaration.enterScope();
		declaration.evaluateScript("variableAnonyme++",
				InterpreterVariableDeclaration.DIALOG_SCOPE);
	}

	@Test
	public void eachGigestScopeCanBeModifyByLOwestScope() {
		declaration.declareVariable("variableAnonyme", "0",
				InterpreterVariableDeclaration.APPLICATION_SCOPE);
		declaration.evaluateScript("variableAnonyme++",
				InterpreterVariableDeclaration.DOCUMENT_SCOPE);
		declaration.evaluateScript("variableAnonyme++",
				InterpreterVariableDeclaration.DIALOG_SCOPE);
		declaration.evaluateScript("variableAnonyme++",
				InterpreterVariableDeclaration.ANONYME_SCOPE);

		assertEquals(3.0, declaration.getValue("application.variableAnonyme"));
	}

	@Test
	public void testWhenVariableIsDeclaredInscopeNamedIsItAccesSiblePrefixedWithScopeName() {
		declaration.enterScope();
		declaration.declareVariableNew("variable", "'document'");

		declaration.enterScope();
		declaration.declareVariableNew("variable", "'dialog'");

		declaration.enterScope();
		declaration.declareVariableNew("variable", "'anonyme'");

		assertEquals("document", declaration.getValueNew("document.variable"));
		assertEquals("dialog", declaration.getValueNew("dialog.variable"));
		assertEquals("anonyme", declaration.getValueNew("variable"));
	}

	@Test
	public void testwhenWeAssignVariableNameItValeurChange() {
		declaration.enterScope();
		declaration.declareVariableNew("variable", "'document'");

		declaration.enterScope();
		declaration.declareVariableNew("variable", "'dialog'");

		declaration.enterScope();
		declaration.declareVariableNew("variable", "'anonyme'");

		declaration.setValue("document.variable", "'document1'");
		declaration.setValue("dialog.variable", "'dialog1'");
		declaration.setValue("variable", "'anonyme1'");

		assertEquals("document1", declaration.getValueNew("document.variable"));
		assertEquals("dialog1", declaration.getValueNew("dialog.variable"));
		assertEquals("anonyme1", declaration.getValueNew("variable"));
	}

	@Test
	public void testWhenWeDeclareVariableInScopeDialogItValueCanUseWithAnotherVariableInencompassingScope() {
		// declare in scope APPLICATION
		declaration.declareVariableNew("heure_ouverture", "undefined");
		declaration.enterScope(); // Enter Scope Document
		declaration.enterScope(); // Enter Scope Dialog
		declaration.declareVariableNew("recupHeureOuverture", "new Object();");
		declaration.enterScope();

		declaration.setValue("recupHeureOuverture.hOuverture", "'non'");
		declaration.setValue("heure_ouverture",
				"recupHeureOuverture.hOuverture");

		assertEquals("non", ((ScriptableObject) declaration
				.getValueNew("recupHeureOuverture")).get("hOuverture"));
		assertEquals("non", declaration.getValueNew("heure_ouverture"));
	}

	@Test
	public void testVariableCanDeclareInAInlineScript() {
		String script = "var d = 'date'";
		declaration.enterScope();
		declaration.enterScope();
		declaration.enterScope();
		declaration.evaluateScriptNew(script);

		assertEquals("date", declaration.getValueNew("d"));
	}

	@Test(expected = EcmaError.class)
	public void testWhenWeExitOneScopeItContainsVariableLose() {
		String script = "var d = 'date'";
		declaration.enterScope();
		declaration.enterScope();
		declaration.enterScope();
		declaration.evaluateScriptNew(script);
		declaration.exitScope();

		declaration.enterScope();

		assertEquals("date", declaration.getValueNew("d"));
	}

	@Test
	public void testWhenWeExitOneScopeItContainsVariableLoseAndTheVariablesDeclaredInBiggerScopeIsVisible() {
		String script = "var d = 'date'";
		String script1 = "var d1 = 'date'";
		declaration.enterScope(); // scope document
		declaration.enterScope(); // scope dialog
		declaration.evaluateScriptNew(script1);
		declaration.enterScope(); // Scope anonyme
		declaration.evaluateScriptNew(script);
		declaration.exitScope();
		System.err.println(declaration.getValueNew("d1"));
		declaration.enterScope();

		assertEquals("date", declaration.getValueNew("d1"));
	}

	@Test
	public void testVariableCanDeclareInAInlineScriptAsANamedScopeCanModify() {
		String script = "var d = 'date'";
		declaration.evaluateScriptNew(script);

		declaration.setValue("d", "'date1'");

		assertEquals("date1", declaration.getValueNew("d"));
	}

	@Test
	public void testVariableCanDeclareInAInlineScriptAsANamedScopeCanModifyUsingAscopeName() {
		String script = "var d = 'date'";
		declaration.enterScope();
		declaration.evaluateScriptNew(script);

		declaration.enterScope();
		declaration.enterScope();
		declaration.setValue("dialog.d", "'date1'");

		assertEquals("date1", declaration.getValueNew("d"));
		assertEquals("date", declaration.getValueNew("document.d"));
	}

	@Test
	public void testVariableParamsInSubdialog() throws IOException {
		declaration.enterScope();
		declaration.enterScope();
		// declaration in dialog scope
		declaration.declareVariableNew("a656", "undefined");
		declaration.declareVariableNew("block1", "undefined");
		declaration.declareVariableNew("block2", "undefined");

		declaration.enterScope();
		declaration.declareVariableNew("target", "'a656b.txml'");

		declaration.setValue("a656", "target");
		declaration.exitScope();

		InterpreterVariableDeclaration subDeclaration = new InterpreterVariableDeclaration();

		subDeclaration.declareVariableNew("a656", "'"
				+ declaration.evaluateScriptNew("a656") + "'");

		assertEquals("a656b.txml", subDeclaration.getValueNew("a656"));
	}

	@Test
	public void variableDeclaredWithVarTagIsAccessibleInLowestScopeScriptTag() {
		declaration.enterScope();
		declaration.declareVariableNew("nbErreur", "0");
		assertEquals(1.0, declaration.evaluateScriptNew("nbErreur += 1;"));
	}

	// Ce est sera reajuster une fois la class InterpreterVariable declaration
	// sera en bonne uniforme
	@Test
	public void globalVariableCantBeAssignInscript() {
		declaration.declareVariable("nbErreurs", "0",
				InterpreterVariableDeclaration.APPLICATION_SCOPE);

		declaration.evaluateScript("nbErreurs++;",
				InterpreterVariableDeclaration.DIALOG_SCOPE);
		declaration
				.resetScopeBinding(InterpreterVariableDeclaration.DIALOG_SCOPE);

		Object nbErreurs = declaration.getValue("nbErreurs");
		Object appNbErreurs = declaration.getValue("application.nbErreurs");

		assertEquals(1.0, nbErreurs);
		assertEquals(1.0, appNbErreurs);
	}

	// ajouter un nouveau test qui dit : quand une variable est déclarer dans
	// une
	// porter plus grosse les modification sur cette variable dans une autre (
	// plus petit, sans redeclaration) se font sur la porte la plus grande

	@Test
	public void testToRemove() {
		Context context = new ContextFactory().enterContext();
		ScriptableObject sessionScope = context.initStandardObjects();

		ScriptableObject applicationScope = (ScriptableObject) context
				.newObject(sessionScope);
		applicationScope.put("application", applicationScope, applicationScope);
		applicationScope.setParentScope(sessionScope);

		ScriptableObject documentScope = (ScriptableObject) context
				.newObject(applicationScope);
		documentScope.put("document", documentScope, documentScope);
		documentScope.setParentScope(applicationScope);

		context.evaluateString(applicationScope, "var v = 0", "", 1, null);

		context.evaluateString(documentScope, "v++", "", 1, null);
		assertEquals(1.0, context.evaluateString(documentScope, "v", "", 1,
				null));

		context.evaluateString(applicationScope, "v++", "", 1, null);
		assertEquals(2.0, context.evaluateString(documentScope, "v", "", 1,
				null));
		assertEquals(2.0, context.evaluateString(applicationScope, "v", "", 1,
				null));

	}

	@Test
	public void globalVariableCantBeAssignInscriptNew() {
		declaration.declareVariableNew("nbErreurs", "0");
		declaration.enterScope();
		declaration.enterScope();
		declaration.evaluateScriptNew("nbErreurs++;");
		declaration.exitScope();

		Object nbErreurs = declaration.getValue("nbErreurs");
		Object appNbErreurs = declaration.getValue("application.nbErreurs");

		assertEquals(1.0, nbErreurs);
		assertEquals(1.0, appNbErreurs);
	}
}
