package infrastructure.javy;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * The JavyInterpreter class is responsible for interpreting source code written in the Javy language.
 * It performs lexical analysis, parsing, and execution of the source code. This interpreter processes
 * the code, manages scopes, and evaluates expressions and statements according to Javy language syntax.
 *
 * @author Albert Beaupre
 */
public class JavyInterpreter {

    // Limit on the length of identifiers
    private static final byte IdentifierLimit = Byte.MAX_VALUE;

    // The source code to interpret
    private final String source;

    // Keeps track of the current location in the source code
    private final JavyLocation location;

    // Builder used for constructing and checking strings during scanning
    private final StringBuilder builder;

    // The current scope being processed
    private JavyScope currentScope;

    /**
     * Constructs a JavyInterpreter instance with the given source code.
     * Initializes the source code, location tracker, builder, and current scope.
     * Also triggers scanning and parsing of the source code.
     *
     * @param source The source code to be interpreted.
     */
    public JavyInterpreter(String source) {
        this.location = new JavyLocation(source.length());
        this.builder = new StringBuilder();
        this.source = source;
        this.currentScope = new JavyScope(null);
        this.scan();

        // Debugging output to list all token binders in the current scope
        for (JavyTokenBinder binder : this.currentScope.getBinders())
            System.out.println(binder);

        // Start parsing the source code
        this.parse(this.currentScope);
    }

    /**
     * Entry point of the interpreter. Reads the source code from a file and
     * creates an instance of JavyInterpreter to process it.
     *
     * @param args Command-line arguments.
     * @throws Exception If an error occurs while reading the file or interpreting the code.
     */
    public static void main(String[] args) throws Exception {
        String source = Files.readString(Paths.get("Hello.js"));
        JavyInterpreter interpreter = new JavyInterpreter(source);
        System.out.println(interpreter.currentScope.getVariable("x"));
    }

    /**
     * Scans the source code for tokens, handling whitespace, newlines, string literals,
     * numbers, identifiers, and other language constructs.
     */
    private void scan() {
        scanning:
        while (notEndOfFile()) {
            switch (current()) {
                case ' ', '\t', '\r' -> consume(); // Consume whitespace characters
                case '\n' -> {
                    location.incrementLine().column(1);
                    consume(); // Consume newline character
                }
                case '"' -> JavyToken.String.process(this); // Process string literals
                default -> {
                    if (Character.isDigit(current())) {
                        JavyToken.Number.process(this); // Process numbers
                    } else if (Character.isLetter(current())) {
                        String identifier = (String) JavyToken.Identifier.process(this);
                        for (JavyToken token : JavyToken.Keywords) {
                            if (token.lexeme().equals(identifier)) {
                                this.currentScope.getBinders().removeLast();
                                System.out.println("Replacing: " + token.lexeme());
                                token.process(this);
                                consume(); // Consume last character of token
                                continue scanning;
                            }
                        }
                    } else {
                        for (JavyToken token : JavyToken.All) {
                            if (check(token.lexeme())) {
                                token.process(this);
                                consume(); // Consume last character of token
                                continue scanning;
                            }
                        }
                        consume(); // Consume unknown characters
                    }
                }
            }
        }
    }

    /**
     * Parses the tokens in the current scope, handling different types of statements and expressions.
     * Supports variable declarations, method definitions, while loops, if statements, and assignments.
     *
     * @param scope The scope to be parsed.
     */
    private void parse(JavyScope scope) {
        while (!scope.getBinders().isEmpty()) {
            JavyTokenBinder binder = scope.getBinders().poll();
            JavyToken token = binder.token();

            switch (token) {
                case While -> {
                    JavyExpression expression = JavyExpression.Parser.parse(scope, true);
                    binder = scope.getBinders().poll();
                    if (binder.token() == JavyToken.LeftBrace) {
                        if (expression.evaluate() instanceof Boolean condition) {
                            if (condition) {
                                JavyScope newScope = this.beginScope();
                                while (!scope.matchToken(JavyToken.RightBrace))
                                    newScope.addBinder(scope.getBinders().poll());
                                while ((boolean) expression.evaluate()) {
                                    parse(newScope.copy());
                                }
                            } else {

                            }
                        } else {
                            throw new RuntimeException(STR."\{binder.location()}While conditions must be true or false");
                        }
                    }
                }
                case Var -> {
                    JavyTokenBinder next = scope.getBinders().poll();
                    if (next.token() != JavyToken.Identifier)
                        throw new RuntimeException(STR."\{next.location()}Variables must have an identifier");

                    String name = (String) next.value();

                    if (scope.variables().containsKey(name))
                        throw new RuntimeException(STR."\{next.location()}Variable \{name} already exists in its scope");

                    next = scope.getBinders().poll();

                    switch (next.token()) {
                        case LeftParameter -> {
                            List<String> parameters = new ArrayList<>();
                            while (scope.matchToken(JavyToken.Identifier))
                                parameters.add((String) scope.getBinders().poll().value());

                            if (scope.matchToken(JavyToken.RightParameter))
                                scope.getBinders().poll();
                            else
                                throw new RuntimeException(STR."\{scope.getBinders().peek().location()}) is expected after ending method parameters");

                            if (scope.matchToken(JavyToken.LeftBrace)) {
                                scope.getBinders().poll();

                                JavyScope newScope;
                                scope.addMethod(new JavyMethod(newScope = beginScope(), name, parameters.toArray(new String[0])));
                                while (!scope.matchToken(JavyToken.RightBrace))
                                    newScope.addBinder(scope.getBinders().poll());
                                this.endScope();
                            } else {
                                throw new RuntimeException(STR."\{scope.getBinders().peek().location()}{ is expected after ending a method statement");
                            }
                        }
                        case Equal -> scope.addVariable(new JavyVariable(name, JavyExpression.Parser.parse(scope)));
                        case Semicolon -> scope.addVariable(new JavyVariable(name));
                        default ->
                                throw new RuntimeException(STR."\{next.location()}Undefined variables must end with a semicolon");
                    }
                }
                case Identifier -> {
                    JavyTokenBinder next = scope.getBinders().poll();
                    String name = (String) binder.value();
                    if (next.token() == JavyToken.Equal) {
                        JavyVariable variable = scope.getVariable(name);

                        if (variable == null)
                            throw new RuntimeException(STR."\{binder.location()}No variable with name \{name}");
                        variable.set(JavyExpression.Parser.parse(scope));
                    } else if (next.token() == JavyToken.LeftParameter) {
                        parse(scope.getMethod(name).scope());
                    }
                }
                case If -> {
                    Object value = JavyExpression.Parser.parse(scope);
                    binder = scope.getBinders().poll();

                    if (binder.token() == JavyToken.LeftBrace) {
                        if (value instanceof Boolean condition) {
                            if (!condition) {
                                while (!scope.matchToken(JavyToken.RightBrace))
                                    scope.getBinders().poll(); // Skip past the statement
                            }
                        } else {
                            throw new RuntimeException("If conditions must be true or false");
                        }
                    } else {
                        throw new RuntimeException("{ is expected after an if condition");
                    }
                }
            }
        }
    }

    /**
     * Checks if the given lexeme matches a sequence of characters in the source code.
     * Advances the current location if a match is found.
     *
     * @param lexeme The string to match against the source code.
     * @return True if the lexeme matches a sequence in the source code, false otherwise.
     */
    private boolean check(String lexeme) {
        builder.setLength(0);
        int newLocation = 0;
        while (location.canIncrementBy(newLocation)) {
            if (newLocation >= IdentifierLimit)
                throw new RuntimeException(STR."Identifiers have a character limit of \{IdentifierLimit}");
            builder.append(source.charAt(location.getCursor() + newLocation));
            if (lexeme.contentEquals(builder)) {
                this.location.incrementCursor(newLocation);
                return true;
            }
            newLocation++;
        }
        return false;
    }

    /**
     * Begins a new scope, creating a child scope from the current scope.
     *
     * @return The new child scope.
     */
    public JavyScope beginScope() {
        JavyScope previous = this.currentScope;
        return this.currentScope = new JavyScope(previous);
    }

    /**
     * Ends the current scope, reverting back to the parent scope.
     * Throws an exception if trying to end the global scope.
     *
     * @return The previous scope that was ended.
     */
    public JavyScope endScope() {
        if (currentScope.parent() == null)
            throw new RuntimeException("Cannot end scope when current scope is global");
        JavyScope previous = this.currentScope;
        this.currentScope = previous.parent(); // Return back to global scope
        return previous;
    }

    /**
     * Retrieves the current scope.
     *
     * @return The current scope.
     */
    public JavyScope currentScope() {
        return currentScope;
    }

    /**
     * Retrieves the current character from the source code.
     *
     * @return The current character.
     */
    public char current() {
        return source.charAt(location.getCursor());
    }

    /**
     * Consumes the current character from the source code, advancing the cursor and column
     * in the process. This method is typically used during the scanning phase to process
     * characters one at a time.
     *
     * @return The character that was consumed.
     */
    public char consume() {
        char current = current(); // Get the current character from the source code
        location.incrementCursor(); // Move the cursor to
        location.incrementColumn();
        return current;
    }

    /**
     * Checks whether the current location has reached the end of the source code.
     *
     * @return True if there are more characters to process; false if the end of the file has been reached.
     */
    public boolean notEndOfFile() {
        return location.getCursor() < source.length();
    }

    /**
     * Retrieves the entire source code being interpreted.
     *
     * @return The source code as a String.
     */
    public String source() {
        return source;
    }

    /**
     * Retrieves the current position and tracking information within the source code.
     * This includes line and column information for better error reporting and diagnostics.
     *
     * @return The current JavyLocation instance tracking the cursor, line, and column.
     */
    public JavyLocation location() {
        return location;
    }

    /**
     * Pushes a new token, its name, and value into the current scope. This is useful when processing tokens
     * and adding them to the binder in the current scope during scanning and parsing phases.
     *
     * @param token The token being pushed (e.g., Identifier, String, Number).
     * @param name  The name of the token, such as an identifier name or keyword.
     * @param value The associated value of the token, such as the content of a string or number.
     */
    public void pushToken(JavyToken token, String name, Object value) {
        this.currentScope.getBinders().offer(new JavyTokenBinder(token, name, value, location.at()));
    }
}
