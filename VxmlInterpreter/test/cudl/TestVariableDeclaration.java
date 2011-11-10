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
import static cudl.script.InterpreterVariableDeclaration.*;

public class TestVariableDeclaration {

	private InterpreterVariableDeclaration declaration;

	@Before
	public void setUp() throws IOException {
		declaration = new InterpreterVariableDeclaration(null);
	}

	@Test
	public void testWeCanDeclareVariableAndRetreViewItValue() throws IOException {
		declaration.declareVariable("variableDocument", "'document'", DOCUMENT_SCOPE);
		declaration.declareVariable("variableDialog", "'dialog'", DIALOG_SCOPE);
		declaration.declareVariable("variableAnonyme", "'anonyme'", ANONYME_SCOPE);

		assertEquals("anonyme", declaration.getValue("variableAnonyme"));
		assertEquals("dialog", declaration.getValue("variableDialog"));
		assertEquals("document", declaration.getValue("variableDocument"));
	}

	@Test
	public void sessionVariablesDeclaration() {
		declaration.declareVariable("variableSession", "new Object()", SESSION_SCOPE);
		declaration.evaluateScript("variableSession.toto ='blabla'", SESSION_SCOPE);

		// assertEquals("",
		// declaration.evaluateScript("session.variableSession",SESSION_SCOPE));
		assertEquals("blabla", ((ScriptableObject) declaration.evaluateScript("session.variableSession",
				SESSION_SCOPE)).get("toto"));
	}

	@Test(expected = EcmaError.class)
	public void testWhenScopeIsClearVariableDeclaredIsLose() {

		declaration.declareVariable("variableDocument", "'document'", DOCUMENT_SCOPE);
		declaration.declareVariable("variableDialog", "'dialog'", DIALOG_SCOPE);

		declaration.declareVariable("variableAnonyme", "'anonyme'", ANONYME_SCOPE);
		declaration.resetScopeBinding(ANONYME_SCOPE);

		assertEquals("document", declaration.getValue("variableDocument"));
		assertEquals("dialog", declaration.getValue("variableDialog"));
		assertEquals("anonyme", declaration.getValue("variableAnonyme"));
	}

	@Test(expected = EcmaError.class)
	public void testWhenVariableIsnotDeclaredItAccessMakeError() {
		declaration.getValue("variableAnonyme");
	}

	@Test(expected = EcmaError.class)
	public void testWhenVariableIsnotDeclaredItAccessWithScriptMakeError() {
		declaration.evaluateScript("variableAnonyme++", InterpreterVariableDeclaration.DIALOG_SCOPE);
	}

	@Test
	public void eachGigestScopeCanBeModifyByLOwestScope() {
		declaration.declareVariable("variableAnonyme", "0", InterpreterVariableDeclaration.APPLICATION_SCOPE);
		declaration.evaluateScript("variableAnonyme++", InterpreterVariableDeclaration.DOCUMENT_SCOPE);
		declaration.evaluateScript("variableAnonyme++", InterpreterVariableDeclaration.DIALOG_SCOPE);
		declaration.evaluateScript("variableAnonyme++", InterpreterVariableDeclaration.ANONYME_SCOPE);

		assertEquals(3.0, declaration.getValue("application.variableAnonyme"));
	}

	@Test
	public void testWhenVariableIsDeclaredInscopeNamedIsItAccesSiblePrefixedWithScopeName() {
		declaration.declareVariable("variable", "'document'", DOCUMENT_SCOPE);
		declaration.declareVariable("variable", "'dialog'", DIALOG_SCOPE);
		declaration.declareVariable("variable", "'anonyme'", ANONYME_SCOPE);

		assertEquals("document", declaration.getValue("document.variable"));
		assertEquals("dialog", declaration.getValue("dialog.variable"));
		assertEquals("anonyme", declaration.getValue("variable"));
	}

	@Test
	public void testwhenWeAssignVariableNameItValeurChange() {
		declaration.declareVariable("variable", "'document'", DOCUMENT_SCOPE);

		declaration.declareVariable("variable", "'dialog'", DIALOG_SCOPE);

		declaration.declareVariable("variable", "'anonyme'", ANONYME_SCOPE);

		declaration.setValue("document.variable", "'document1'");
		declaration.setValue("dialog.variable", "'dialog1'");
		declaration.setValue("variable", "'anonyme1'");

		assertEquals("document1", declaration.getValue("document.variable"));
		assertEquals("dialog1", declaration.getValue("dialog.variable"));
		assertEquals("anonyme1", declaration.getValue("variable"));
	}

	@Test
	public void testWhenWeDeclareVariableInScopeDialogItValueCanUseWithAnotherVariableInencompassingScope() {
		// declare in scope APPLICATION
		declaration.declareVariable("heure_ouverture", "undefined", APPLICATION_SCOPE);
		declaration.declareVariable("recupHeureOuverture", "new Object();", DIALOG_SCOPE);

		declaration.setValue("recupHeureOuverture.hOuverture", "'non'");
		declaration.setValue("heure_ouverture", "recupHeureOuverture.hOuverture");

		assertEquals("non", ((ScriptableObject) declaration.getValue("recupHeureOuverture")).get("hOuverture"));
		assertEquals("non", declaration.getValueNew("heure_ouverture"));
	}

	@Test
	public void testVariableCanDeclareInAInlineScript() {
		String script = "var d = 'date'";
		declaration.evaluateScript(script, ANONYME_SCOPE);

		assertEquals("date", declaration.getValueNew("d"));
	}

	@Test(expected = EcmaError.class)
	public void testWhenWeExitOneScopeItContainsVariableLose() {
		String script = "var d = 'date'";
		declaration.evaluateScript(script, ANONYME_SCOPE);
		declaration.resetScopeBinding(ANONYME_SCOPE);

		declaration.enterScope();

		assertEquals("date", declaration.getValue("d"));
	}

	@Test
	public void testWhenWeExitOneScopeItContainsVariableLoseAndTheVariablesDeclaredInBiggerScopeIsVisible() {
		String script = "var d = 'date'";
		String script1 = "var d = 'date1'";
		declaration.evaluateScript(script, DIALOG_SCOPE);
		declaration.evaluateScript(script1, ANONYME_SCOPE);
		declaration.resetScopeBinding(ANONYME_SCOPE);
		assertEquals("date", declaration.getValueNew("d"));
	}

	@Test
	public void testVariableCanDeclareInAInlineScriptAsANamedScopeCanModify() {
		String script = "var d = 'date'";
		declaration.evaluateScript(script,DOCUMENT_SCOPE);

		declaration.setValue("d", "'date1'");

		assertEquals("date1", declaration.getValueNew("d"));
	}

	@Test
	public void testVariableCanDeclareInAInlineScriptAsANamedScopeCanModifyUsingAscopeName() {
		String script = "var d = 'date'";
		declaration.evaluateScript(script,DOCUMENT_SCOPE);

		declaration.setValue("dialog.d", "'date1'");

		assertEquals("date1", declaration.getValue("d"));
		assertEquals("date", declaration.getValue("document.d"));
	}

	@Test
	public void testVariableParamsInSubdialog() throws IOException {
		declaration.enterScope();
		declaration.enterScope();
		// declaration in dialog scope
		declaration.declareVariable("a656", "undefined",DIALOG_SCOPE);
		declaration.declareVariable("block1", "undefined",DIALOG_SCOPE);
		declaration.declareVariable("block2", "undefined",DIALOG_SCOPE);

		declaration.declareVariable("target", "'a656b.txml'",ANONYME_SCOPE);

		declaration.setValue("a656", "target");
		declaration.resetScopeBinding(DIALOG_SCOPE);
		
		InterpreterVariableDeclaration subDeclaration = new InterpreterVariableDeclaration(null);

		subDeclaration.declareVariable("a656", "'" + declaration.evaluateScriptNew("a656") + "'",APPLICATION_SCOPE);

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
		declaration.declareVariable("nbErreurs", "0", InterpreterVariableDeclaration.APPLICATION_SCOPE);

		declaration.evaluateScript("nbErreurs++;", InterpreterVariableDeclaration.DIALOG_SCOPE);
		declaration.resetScopeBinding(InterpreterVariableDeclaration.DIALOG_SCOPE);

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

		ScriptableObject applicationScope = (ScriptableObject) context.newObject(sessionScope);
		applicationScope.put("application", applicationScope, applicationScope);
		applicationScope.setParentScope(sessionScope);

		ScriptableObject documentScope = (ScriptableObject) context.newObject(applicationScope);
		documentScope.put("document", documentScope, documentScope);
		documentScope.setParentScope(applicationScope);

		context.evaluateString(applicationScope, "var v = 0", "", 1, null);

		context.evaluateString(documentScope, "v++", "", 1, null);
		assertEquals(1.0, context.evaluateString(documentScope, "v", "", 1, null));

		context.evaluateString(applicationScope, "v++", "", 1, null);
		assertEquals(2.0, context.evaluateString(documentScope, "v", "", 1, null));
		assertEquals(2.0, context.evaluateString(applicationScope, "v", "", 1, null));

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
