package infrastructure.net.web.ui.css;

/**
 * Enum representing valid CSS {@code list-style-type} property values.
 * <p>
 * The {@code list-style-type} property specifies the marker (bullet or number)
 * style for list items in ordered or unordered lists.
 *
 * <ul>
 *     <li>{@code NONE} – No marker is displayed.</li>
 *     <li>{@code DISC} – A filled circle (default for unordered lists).</li>
 *     <li>{@code CIRCLE} – A hollow circle.</li>
 *     <li>{@code SQUARE} – A filled square.</li>
 *     <li>{@code DECIMAL} – Decimal numbers (1, 2, 3...).</li>
 *     <li>{@code LOWER_ROMAN} – Lowercase Roman numerals (i, ii, iii...).</li>
 *     <li>{@code UPPER_ROMAN} – Uppercase Roman numerals (I, II, III...).</li>
 *     <li>{@code LOWER_ALPHA} – Lowercase alphabetical markers (a, b, c...).</li>
 *     <li>{@code UPPER_ALPHA} – Uppercase alphabetical markers (A, B, C...).</li>
 * </ul>
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/list-style-type">MDN Reference</a>
 */
public enum ListStyleType {

    /**
     * No marker is shown.
     */
    NONE("none"),

    /**
     * A filled circle marker (default unordered list style).
     */
    DISC("disc"),

    /**
     * A hollow circle marker.
     */
    CIRCLE("circle"),

    /**
     * A filled square marker.
     */
    SQUARE("square"),

    /**
     * Decimal number markers (1, 2, 3...).
     */
    DECIMAL("decimal"),

    /**
     * Lowercase Roman numeral markers (i, ii, iii...).
     */
    LOWER_ROMAN("lower-roman"),

    /**
     * Uppercase Roman numeral markers (I, II, III...).
     */
    UPPER_ROMAN("upper-roman"),

    /**
     * Lowercase alphabetic markers (a, b, c...).
     */
    LOWER_ALPHA("lower-alpha"),

    /**
     * Uppercase alphabetic markers (A, B, C...).
     */
    UPPER_ALPHA("upper-alpha");

    private final String value;

    ListStyleType(String value) {
        this.value = value;
    }

    /**
     * Gets the CSS property name this enum represents.
     *
     * @return the string {@code "list-style-type"}
     */
    public static String getPropertyName() {
        return "list-style-type";
    }

    /**
     * Gets the raw CSS value for this enum constant.
     *
     * @return the CSS string value (e.g., "disc", "decimal")
     */
    public String getValue() {
        return value;
    }
}