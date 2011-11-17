package cudl;

import static cudl.script.Utils.scriptableObjectToString;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import cudl.script.InterpreterVariableDeclaration;

public class UtilsTest {

	private InterpreterVariableDeclaration declaration;

	@Before
	public void setUp() throws IOException {
		declaration = new InterpreterVariableDeclaration(null);
	}

	@Test
	public void nativeObjectTest(){
		declaration.declareVariable("doubles", "1", 50);
		declaration.declareVariable("chaine", "'1'", 50);
		declaration.declareVariable("obj", "2", 50);
		
		assertEquals("1.0", scriptableObjectToString(declaration.getValue("doubles")));
		assertEquals("'1'", scriptableObjectToString(declaration.getValue("chaine")));
		assertEquals("2", scriptableObjectToString(declaration.getValue("obj")));
	}
	
	@Test
	public void nativeArrayToString() {
		declaration.declareVariable("array", "[1,2,'dsds']", 50);

		assertEquals("[1.0, 2, 'dsds']", scriptableObjectToString(declaration.getValue("array")));
	}

	@Test
	public void jsonToString() {
		declaration.declareVariable("y", "x = {'id' : 245, 'name' : 'bal selenium'}", 50);
		assertEquals("{'id' : 245, 'name' : 'bal selenium'}", scriptableObjectToString(declaration.getValue("y")));
	}

}
