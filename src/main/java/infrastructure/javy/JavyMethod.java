package infrastructure.javy;

import java.util.Arrays;

/**
 * Represents a method with its associated scope, name, and parameters.
 * The method can be defined with a specific scope and a list of parameters.
 *
 * @author Albert Beaupre
 */
public record JavyMethod(JavyScope scope, String name, String... parameters) {

    @Override
    public String toString() {
        return String.format("Method{scope=%s, name='%s', parameters=%s}",
                scope, name, Arrays.toString(parameters));
    }
}