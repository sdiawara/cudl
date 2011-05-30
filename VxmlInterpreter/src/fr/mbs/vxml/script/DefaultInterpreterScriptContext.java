package fr.mbs.vxml.script;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

public class DefaultInterpreterScriptContext extends SimpleScriptContext
		implements InterpreterScriptContext {
	protected static final int STEP = 10;
	protected static final int LOWEST_SCOPE = 50;
	protected static final int BIGEST_SCOPE = 90;

	protected Bindings sessionScope = new SimpleBindings();
	protected Bindings applicationScope = new SimpleBindings();
	protected Bindings documentScope = new SimpleBindings();
	protected Bindings dialogScope = new SimpleBindings();
	protected Bindings anonymeScope = new SimpleBindings();

	private static final List<Integer> SCOPES = Collections
			.unmodifiableList(Arrays.asList(new Integer[] {
					new Integer(SESSION_SCOPE), new Integer(APPLICATION_SCOPE),
					new Integer(DOCUMENT_SCOPE), new Integer(DIALOG_SCOPE),
					new Integer(ANONYME_SCOPE) }));

	@Override
	public Object getAttribute(String name) {
		checkName(name);

		int tmp = LOWEST_SCOPE;
		while (tmp <= BIGEST_SCOPE) {
			Bindings bindings = getBindings(tmp);
			if (bindings.containsKey(name)) {
				return bindings.get(name);
			}
			tmp += STEP;
		}

		return super.getAttribute(name);
	}

	@Override
	public Object getAttribute(String name, int scope) {
		checkName(name);

		if (DefaultInterpreterScriptContext.SCOPES.contains(scope))
			return getAttribute(name);
		return super.getAttribute(name, scope);
	}

	@Override
	public int getAttributesScope(String name) {
		checkName(name);

		if (anonymeScope.containsKey(name))
			return ANONYME_SCOPE;
		else if (dialogScope.containsKey(name))
			return ANONYME_SCOPE;
		else if (documentScope.containsKey(name))
			return ANONYME_SCOPE;
		else if (applicationScope.containsKey(name))
			return ANONYME_SCOPE;
		else if (sessionScope.containsKey(name))
			return ANONYME_SCOPE;

		return super.getAttributesScope(name);
	}

	@Override
	public Bindings getBindings(int scope) {
		// System.err.print("--->");
		switch (scope) {
		case InterpreterScriptContext.SESSION_SCOPE:
			// System.err.println("SESSION_SCOPE");
			return sessionScope;
		case InterpreterScriptContext.APPLICATION_SCOPE:
			// System.err.println("APPLICATION_SCOPE");
			return applicationScope;
		case InterpreterScriptContext.DOCUMENT_SCOPE:
			// System.err.println("DOCUMENT_SCOPE");
			return documentScope;
		case InterpreterScriptContext.DIALOG_SCOPE:
			// System.err.println("DIALOG_SCOPE");
			return dialogScope;
		case InterpreterScriptContext.ANONYME_SCOPE:
			// System.err.println("ANONYME_SCOPE");
			return anonymeScope;

		default:
			return super.getBindings(scope);
		}
	}

	@Override
	public List<Integer> getScopes() {
		return SCOPES;
	}

	@Override
	public Object removeAttribute(String name, int scope) {
		checkName(name);

		switch (scope) {
		case InterpreterScriptContext.SESSION_SCOPE:
			return sessionScope.remove(name);
		case InterpreterScriptContext.APPLICATION_SCOPE:
			return applicationScope.remove(name);
		case InterpreterScriptContext.DOCUMENT_SCOPE:
			return documentScope.remove(name);
		case InterpreterScriptContext.DIALOG_SCOPE:
			return dialogScope.remove(name);
		case InterpreterScriptContext.ANONYME_SCOPE:
			return anonymeScope.remove(name);

		default:
			return super.removeAttribute(name, scope);
		}
	}

	@Override
	public void setAttribute(String name, Object value, int scope) {
		switch (scope) {
		case InterpreterScriptContext.SESSION_SCOPE:
			sessionScope.put(name, value);
			break;
		case InterpreterScriptContext.APPLICATION_SCOPE:
			applicationScope.put(name, value);
			break;
		case InterpreterScriptContext.DOCUMENT_SCOPE:
			documentScope.put(name, value);
			break;
		case InterpreterScriptContext.DIALOG_SCOPE:
			dialogScope.put(name, value);
			break;
		case InterpreterScriptContext.ANONYME_SCOPE:
			anonymeScope.put(name, value);
			break;

		default:
			super.setAttribute(name, value, scope);
		}
	}

	@Override
	public void setBindings(Bindings bindings, int scope) {
		if (null == bindings)
			throw new IllegalArgumentException("null binding");

		switch (scope) {
		case InterpreterScriptContext.SESSION_SCOPE:
			sessionScope = bindings;
		case InterpreterScriptContext.APPLICATION_SCOPE:
			applicationScope = bindings;
		case InterpreterScriptContext.DOCUMENT_SCOPE:
			documentScope = bindings;
		case InterpreterScriptContext.DIALOG_SCOPE:
			dialogScope = bindings;
		case InterpreterScriptContext.ANONYME_SCOPE:
			anonymeScope = bindings;
		default:
			throw new IllegalArgumentException("invalid scope for interpreter");
		}
	}

	private void checkName(String name) {
		if (name == null) {
			throw new NullPointerException("name must not be null");
		}
		if (name.length() == 0) {
			throw new IllegalArgumentException(
					"name must not be an empty string");
		}
	}
}
