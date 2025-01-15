package backend.javy;

/**
 * Interface for processing tokens in the Javy interpreter.
 * Implementations of this interface define how a token is processed
 * and what value is returned.
 *
 * @param <T> The type of value produced by processing a token.
 * @author Albert Beaupre
 */
public interface TokenProcessor<T> {

    /**
     * A default token processor that returns {@code null} for any token.
     */
    TokenProcessor<Object> Default = interpreter -> null;

    /**
     * Processes a token using the provided interpreter.
     *
     * @param interpreter The interpreter used to process the token.
     * @return The result of processing the token.
     */
    T process(JavyInterpreter interpreter);
}