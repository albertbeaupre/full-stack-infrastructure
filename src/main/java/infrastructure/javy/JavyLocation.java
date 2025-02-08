package infrastructure.javy;

/**
 * The {@code JavyLocation} class represents a position in the source code being interpreted.
 * It tracks the current cursor position, line number, and column number to assist in token scanning
 * and error reporting. The location helps in determining where the interpreter is within the
 * source file at any given time.
 *
 * @author Albert Beaupre
 */
public class JavyLocation {

    private final int limit;  // The total length of the source code
    private int cursor = 0;   // The current cursor position (index in the source code)
    private int line = 1;     // The current line number (1-based)
    private int column = 1;   // The current column number (1-based)

    /**
     * Constructs a {@code JavyLocation} object with the specified source limit.
     * The limit determines how far the cursor can move within the source code.
     *
     * @param limit The maximum number of characters in the source code.
     */
    public JavyLocation(int limit) {
        this.limit = limit;
    }

    /**
     * Checks if the cursor can be incremented by a specified amount without exceeding the source code limit.
     *
     * @param amount The number of characters to increment.
     * @return {@code true} if the cursor can be incremented, {@code false} otherwise.
     */
    public boolean canIncrementBy(int amount) {
        return this.cursor + amount < limit;
    }

    /**
     * Increments the cursor position by a specified amount.
     *
     * @param amount The number of characters to increment.
     * @return The current {@code JavyLocation} instance for method chaining.
     */
    public JavyLocation incrementCursor(int amount) {
        this.cursor += amount;
        return this;
    }

    /**
     * Increments the cursor position by one character.
     *
     * @return The current {@code JavyLocation} instance for method chaining.
     */
    public JavyLocation incrementCursor() {
        return incrementCursor(1);
    }

    /**
     * Decrements the cursor position by a specified amount.
     *
     * @param amount The number of characters to decrement.
     * @return The current {@code JavyLocation} instance for method chaining.
     */
    public JavyLocation decrementCursor(int amount) {
        this.cursor -= amount;
        return this;
    }

    /**
     * Decrements the cursor position by one character.
     *
     * @return The current {@code JavyLocation} instance for method chaining.
     */
    public JavyLocation decrementCursor() {
        return decrementCursor(1);
    }

    /**
     * Increments the line number by a specified amount.
     *
     * @param amount The number of lines to increment.
     * @return The current {@code JavyLocation} instance for method chaining.
     */
    public JavyLocation incrementLine(int amount) {
        this.line += amount;
        return this;
    }

    /**
     * Increments the line number by one.
     *
     * @return The current {@code JavyLocation} instance for method chaining.
     */
    public JavyLocation incrementLine() {
        return incrementLine(1);
    }

    /**
     * Decrements the line number by a specified amount.
     *
     * @param amount The number of lines to decrement.
     * @return The current {@code JavyLocation} instance for method chaining.
     */
    public JavyLocation decrementLine(int amount) {
        this.line -= amount;
        return this;
    }

    /**
     * Decrements the line number by one.
     *
     * @return The current {@code JavyLocation} instance for method chaining.
     */
    public JavyLocation decrementLine() {
        return decrementLine(1);
    }

    /**
     * Increments the column number by a specified amount.
     *
     * @param amount The number of columns to increment.
     * @return The current {@code JavyLocation} instance for method chaining.
     */
    public JavyLocation incrementColumn(int amount) {
        this.column += amount;
        return this;
    }

    /**
     * Increments the column number by one.
     *
     * @return The current {@code JavyLocation} instance for method chaining.
     */
    public JavyLocation incrementColumn() {
        return incrementColumn(1);
    }

    /**
     * Decrements the column number by a specified amount.
     *
     * @param amount The number of columns to decrement.
     * @return The current {@code JavyLocation} instance for method chaining.
     */
    public JavyLocation decrementColumn(int amount) {
        this.column -= amount;
        return this;
    }

    /**
     * Decrements the column number by one.
     *
     * @return The current {@code JavyLocation} instance for method chaining.
     */
    public JavyLocation decrementColumn() {
        return decrementColumn(1);
    }

    /**
     * Sets the cursor to a specified position.
     *
     * @param increment The new cursor position.
     * @return The current {@code JavyLocation} instance for method chaining.
     */
    public JavyLocation cursor(int increment) {
        this.cursor = increment;
        return this;
    }

    /**
     * Sets the line number to a specified value.
     *
     * @param line The new line number.
     * @return The current {@code JavyLocation} instance for method chaining.
     */
    public JavyLocation line(int line) {
        this.line = line;
        return this;
    }

    /**
     * Sets the column number to a specified value.
     *
     * @param column The new column number.
     * @return The current {@code JavyLocation} instance for method chaining.
     */
    public JavyLocation column(int column) {
        this.column = column;
        return this;
    }

    /**
     * Returns the current cursor position.
     *
     * @return The current cursor index.
     */
    public int getCursor() {
        return cursor;
    }

    /**
     * Returns the current line number.
     *
     * @return The current line number.
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the current column number.
     *
     * @return The current column number.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Creates a copy of the current {@code JavyLocation} object with the same cursor, line, and column values.
     *
     * @return A new {@code JavyLocation} object with the same state as the current one.
     */
    public JavyLocation at() {
        return new JavyLocation(limit).line(line).column(column).cursor(cursor);
    }

    /**
     * Returns a string representation of the location, formatted as [line:column].
     *
     * @return A string representing the current line and column.
     */
    @Override
    public String toString() {
        return String.format("[%s:%s]", line, column);
    }
}