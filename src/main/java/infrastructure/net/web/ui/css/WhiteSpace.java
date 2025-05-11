package infrastructure.net.web.ui.css;

/**
 * Enum representing valid CSS {@code white-space} property values.
 * <p>
 * The {@code white-space} property specifies how white space inside an element is handled.
 *
 * <ul>
 *     <li>{@code NORMAL} – Collapses whitespace and breaks lines as needed (default behavior).</li>
 *     <li>{@code NOWRAP} – Collapses whitespace but prevents line wrapping.</li>
 *     <li>{@code PRE} – Preserves whitespace and line breaks exactly as written.</li>
 *     <li>{@code PRE_WRAP} – Preserves whitespace and line breaks, and allows text wrapping when needed.</li>
 *     <li>{@code PRE_LINE} – Collapses whitespace but preserves line breaks.</li>
 * </ul>
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/white-space">MDN Reference</a>
 */
public enum WhiteSpace {

    /**
     * Collapses whitespace and wraps text when necessary (default behavior).
     */
    NORMAL("normal"),

    /**
     * Collapses whitespace but prevents text from wrapping to the next line.
     */
    NOWRAP("nowrap"),

    /**
     * Preserves whitespace and line breaks; behaves like the {@code <pre>} tag.
     */
    PRE("pre"),

    /**
     * Preserves whitespace and line breaks, but also allows wrapping when needed.
     */
    PRE_WRAP("pre-wrap"),

    /**
     * Collapses whitespace but preserves line breaks.
     */
    PRE_LINE("pre-line");

    private final String value;

    WhiteSpace(String value) {
        this.value = value;
    }

    /**
     * Gets the CSS property name this enum represents.
     *
     * @return the string {@code "white-space"}
     */
    public static String getPropertyName() {
        return "white-space";
    }

    /**
     * Gets the raw CSS value for this enum constant.
     *
     * @return the CSS string value (e.g., "pre-wrap", "nowrap")
     */
    public String getValue() {
        return value;
    }
}