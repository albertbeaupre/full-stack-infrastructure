package backend.javy;

import java.util.*;

/**
 * Represents a scope in the Javy interpreter. A scope contains variables, methods, and binders.
 * It supports hierarchical scoping, where a scope can have a parent scope.
 *
 * @author Albert Beaupre
 */
public class JavyScope {

    private static long INDEX = 0;

    private Map<String, JavyVariable> variables = new HashMap<>();
    private Map<String, JavyMethod> methods = new HashMap<>();
    private ArrayDeque<JavyTokenBinder> binders = new ArrayDeque<>();

    private long index;
    private final JavyScope parent;

    /**
     * Constructs a new scope with the given parent scope.
     *
     * @param parent The parent scope of this scope.
     */
    public JavyScope(JavyScope parent) {
        this.index = INDEX++;
        this.parent = parent;
    }

    /**
     * Creates a copy of this scope, including its variables, methods, and binders.
     * The copy will have the same parent as this scope.
     *
     * @return A new JavyScope instance that is a copy of this scope.
     */
    public JavyScope copy() {
        JavyScope scope = new JavyScope(parent);
        scope.index = index;
        scope.variables = new HashMap<>(variables);
        scope.methods = new HashMap<>(methods);
        scope.binders = new ArrayDeque<>(binders);
        return scope;
    }

    /**
     * Adds a binder to this scope.
     *
     * @param binder The binder to add.
     */
    public void addBinder(JavyTokenBinder binder) {
        this.binders.offer(binder);
    }

    /**
     * Adds a variable to this scope.
     *
     * @param variable The variable to add.
     */
    public void addVariable(JavyVariable variable) {
        this.variables.put(variable.getName(), variable);
    }

    /**
     * Adds a method to this scope.
     *
     * @param method The method to add.
     */
    public void addMethod(JavyMethod method) {
        this.methods.put(method.name(), method);
    }

    /**
     * Retrieves an unmodifiable view of the variables in this scope.
     *
     * @return A map of variable names to variables.
     */
    public Map<String, JavyVariable> variables() {
        return Collections.unmodifiableMap(variables);
    }

    /**
     * Retrieves an unmodifiable view of the methods in this scope.
     *
     * @return A map of method names to methods.
     */
    public Map<String, JavyMethod> methods() {
        return Collections.unmodifiableMap(methods);
    }

    /**
     * Retrieves the binders in this scope.
     *
     * @return A deque of JavyTokenBinder instances.
     */
    public ArrayDeque<JavyTokenBinder> getBinders() {
        return binders;
    }

    /**
     * Gets the unique index of this scope.
     *
     * @return The index of this scope.
     */
    public long index() {
        return index;
    }

    /**
     * Retrieves the parent scope of this scope.
     *
     * @return The parent scope.
     */
    public JavyScope parent() {
        return parent;
    }

    /**
     * Retrieves a variable by name. Searches in this scope and up through parent scopes if necessary.
     *
     * @param name The name of the variable to retrieve.
     * @return The variable with the specified name, or null if not found.
     */
    public JavyVariable getVariable(String name) {
        JavyVariable variable = this.variables().get(name);
        JavyScope scope = this;
        while (variable == null && (scope = scope.parent()) != null) {
            variable = scope.variables().get(name);
        }
        return variable;
    }

    /**
     * Retrieves a method by name. Searches in this scope and up through parent scopes if necessary.
     *
     * @param name The name of the method to retrieve.
     * @return The method with the specified name, or null if not found.
     */
    public JavyMethod getMethod(String name) {
        JavyMethod method = this.methods.get(name);
        JavyScope scope = this;
        while (method == null && (scope = scope.parent()) != null) {
            method = scope.methods.get(name);
        }
        return method;
    }

    /**
     * Checks if the top binder in the binders queue matches any of the specified tokens.
     *
     * @param tokens The tokens to check for.
     * @return True if the top binder matches any of the specified tokens, otherwise false.
     */
    public boolean matchToken(JavyToken... tokens) {
        if (binders.isEmpty())
            return false;

        JavyToken topToken = binders.peek().token();
        return Arrays.stream(tokens).anyMatch(token -> topToken == token);
    }
}