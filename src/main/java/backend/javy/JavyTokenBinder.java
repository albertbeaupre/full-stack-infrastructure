package backend.javy;

/**
 * Represents a binding of a token with its associated metadata in the Javy interpreter.
 * This includes the token itself, its lexeme, value, and its location in the source code.
 *
 * @author Albert Beaupre
 */
public record JavyTokenBinder(JavyToken token, String lexeme, Object value, JavyLocation location) {

    /**
     * Returns a string representation of the JavyTokenBinder instance.
     * The format includes the token, lexeme, value, and location.
     *
     * @return A string representation of the JavyTokenBinder.
     */
    @Override
    public String toString() {
        return STR."JavyTokenBinder{token=\{token}, lexeme='\{lexeme}', value=\{value}, location=\{location}}";
    }
}
