package backend.javy;

/**
 * Represents a variable in the Javy interpreter.
 * Each variable has a unique name, an index, and a value.
 * Variables are comparable based on their index.
 *
 * @author Albert Beaupre
 */
public class JavyVariable implements Comparable<JavyVariable> {

    private static long INDEX = 0;

    private final String name;
    private final long index;
    private Object value;

    /**
     * Creates a new variable with the specified name and a default value of {@code null}.
     *
     * @param name The name of the variable.
     */
    public JavyVariable(String name) {
        this(name, null);
    }

    /**
     * Creates a new variable with the specified name and value.
     *
     * @param name  The name of the variable.
     * @param value The value of the variable.
     */
    public JavyVariable(String name, Object value) {
        this.name = name;
        this.value = value;
        this.index = INDEX++;
    }

    /**
     * Sets the value of the variable.
     *
     * @param value The new value for the variable.
     */
    public void set(Object value) {
        this.value = value;
    }

    /**
     * Returns the name of the variable.
     *
     * @return The name of the variable.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the current value of the variable.
     *
     * @return The value of the variable.
     */
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("Variable{name='%s', value=%s}", name, value);
    }

    @Override
    public int compareTo(JavyVariable o) {
        return Long.compare(index, o.index);
    }
}