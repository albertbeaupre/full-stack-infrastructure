package backend.javy;

/**
 * Interface for parsing tokens in the Javy interpreter.
 * Implementations of this interface define how tokens are parsed
 * and processed by the interpreter.
 *
 * @author Albert Beaupre
 */
public interface TokenParser {

    /**
     * Parses tokens using the provided interpreter.
     *
     * @param interpreter The interpreter that manages the parsing process.
     */
    void parse(JavyInterpreter interpreter);
}
