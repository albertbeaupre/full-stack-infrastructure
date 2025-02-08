package infrastructure.javy;

import java.util.stream.Stream;

/**
 * The {@code JavyToken} enum represents different types of tokens in the source code.
 * It categorizes tokens such as delimiters, operators, statements, and values.
 * Each token may have a corresponding lexeme (symbol or keyword) and a processing
 * behavior to handle its interpretation.
 *
 * @author Albert Beaupre
 */
public enum JavyToken {
    /**
     * Delimiters used to structure code blocks and parameters.
     */
    LeftBrace("{"),
    RightBrace("}"),
    LeftBracket("["),
    RightBracket("]"),
    LeftParameter("("),
    RightParameter(")"),
    Semicolon(";"),

    /**
     * Arithmetic and bitwise operators.
     */
    Plus("+"),
    Minus("-"),
    Slash("/"),
    Star("*"),
    Equal("="),
    Bang("!"),
    BitwiseOr("|"),
    BitwiseAnd("&"),
    BitwiseXOr("^"),
    BitwiseComplement("~"),

    /**
     * Comparison operators.
     */
    GreaterThanOrEqualTo(">="),
    LessThanOrEqualTo("<="),
    GreaterThan(">"),
    LessThan("<"),
    Is("is"),
    Not("not"),

    /**
     * Statement keywords used for control flow.
     */
    If("if"),
    While("while"),
    Do("do"),
    For("for"),
    Var("var"),
    Return("return"),

    /**
     * Boolean values.
     */
    True("true"),
    False("false"),
    Null("null"),

    /**
     * Logical comparators.
     */
    And("and"),
    Or("or"),

    /**
     * Token for identifiers, representing variable and function names.
     * This token processes a sequence of letters to form an identifier.
     */
    Identifier(null, (interpreter) -> {
        StringBuilder identifier = new StringBuilder();
        while (interpreter.notEndOfFile() && Character.isLetter(interpreter.current())) {
            identifier.append(interpreter.consume());
        }
        return identifier.toString();
    }),

    /**
     * Token for numeric values. It processes sequences of digits to form a number.
     */
    Number(null, (interpreter) -> {
        StringBuilder number = new StringBuilder();
        while (interpreter.notEndOfFile() && Character.isDigit(interpreter.current())) {
            number.append(interpreter.consume());
        }
        return Double.parseDouble(number.toString());
    }),

    /**
     * Token for string literals. It processes characters within quotes.
     */
    String(null, (interpreter) -> {
        interpreter.consume(); // consume the starting quote
        StringBuilder string = new StringBuilder();
        while (interpreter.notEndOfFile() && interpreter.current() != '"') {
            string.append(interpreter.consume());
        }
        interpreter.consume(); // consume the closing quote
        return string.toString();
    });

    /**
     * Array of tokens that are keywords in the language.
     */
    public static final JavyToken[] Keywords = Stream.of(Var, If, Do, While, For, And, Or, Is).toArray(JavyToken[]::new);

    /**
     * Array of all tokens with a defined lexeme.
     */
    public static final JavyToken[] All = Stream.of(values()).filter(f -> f.lexeme != null).toArray(JavyToken[]::new);

    private final String lexeme;  // The symbol or keyword representing the token
    private final TokenProcessor processor;  // The behavior that defines how the token is processed

    /**
     * Constructs a {@code JavyToken} with a lexeme and a custom processing behavior.
     *
     * @param lexeme    The lexeme that represents the token (e.g., "+", "if").
     * @param processor The processing logic associated with the token.
     */
    JavyToken(String lexeme, TokenProcessor processor) {
        this.lexeme = lexeme;
        this.processor = processor;
    }

    /**
     * Constructs a {@code JavyToken} with a lexeme and a default processor.
     *
     * @param lexeme The lexeme that represents the token.
     */
    JavyToken(String lexeme) {
        this.lexeme = lexeme;
        this.processor = TokenProcessor.Default;
    }

    /**
     * Retrieves the lexeme associated with the token.
     *
     * @return The lexeme, which is the symbol or keyword of the token.
     */
    public String lexeme() {
        return lexeme;
    }

    /**
     * Retrieves the processor for the token. The processor defines how this token should be
     * interpreted by the {@code JavyInterpreter}.
     *
     * @return The {@code TokenProcessor} for the token.
     */
    public TokenProcessor processor() {
        return processor;
    }

    /**
     * Processes the token using the associated {@code TokenProcessor} in the given interpreter.
     * It then pushes the token and its resulting value to the interpreter's scope.
     *
     * @param interpreter The interpreter that processes the token.
     * @return The result of the token processing, such as a parsed identifier, number, or string.
     */
    public Object process(JavyInterpreter interpreter) {
        Object value = this.processor.process(interpreter);  // Process the token using the processor
        String name = lexeme == null ? this.name().toLowerCase() : this.lexeme;  // Use the token's lexeme or name
        interpreter.pushToken(this, name, value);  // Push the token into the interpreter's scope
        return value;
    }
}