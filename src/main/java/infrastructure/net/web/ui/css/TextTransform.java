package infrastructure.net.web.ui.css;

/**
 * Enum representing valid CSS {@code text-transform} property values.
 * <p>
 * The {@code text-transform} property controls the capitalization of text.
 *
 * <ul>
 *     <li>{@code NONE} – No transformation; text renders as-is.</li>
 *     <li>{@code CAPITALIZE} – Transforms the first character of each word to uppercase.</li>
 *     <li>{@code UPPERCASE} – Transforms all characters to uppercase.</li>
 *     <li>{@code LOWERCASE} – Transforms all characters to lowercase.</li>
 * </ul>
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/text-transform">MDN Reference</a>
 */
public enum TextTransform {

    /**
     * No text transformation is applied.
     */
    NONE("none"),

    /**
     * Transforms the first character of each word to uppercase.
     */
    CAPITALIZE("capitalize"),

    /**
     * Transforms all characters to uppercase.
     */
    UPPERCASE("uppercase"),

    /**
     * Transforms all characters to lowercase.
     */
    LOWERCASE("lowercase");

    private final String value;

    TextTransform(String value) {
        this.value = value;
    }

    /**
     * Gets the CSS property name this enum represents.
     *
     * @return the string {@code "text-transform"}
     */
    public static String getPropertyName() {
        return "text-transform";
    }

    /**
     * Gets the raw CSS value for this enum constant.
     *
     * @return the CSS string value (e.g., "capitalize", "uppercase")
     */
    public String getValue() {
        return value;
    }
}
