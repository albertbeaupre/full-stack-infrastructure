package infrastructure.net.web.ui.css;

/**
 * Enum representing valid CSS {@code font-style} property values.
 * <p>
 * The {@code font-style} property allows you to make text italic, oblique, or normal.
 *
 * <ul>
 *     <li>{@code NORMAL} - Standard, upright text.</li>
 *     <li>{@code ITALIC} - Italicized text typically using a specially designed italic face.</li>
 *     <li>{@code OBLIQUE} - Slanted text typically using a skewed version of the normal face.</li>
 * </ul>
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/font-style">MDN Reference</a>
 */
public enum FontStyle {

    /**
     * The font is displayed normally, without italics or obliques.
     */
    NORMAL("normal"),

    /**
     * The font is displayed in italic style.
     */
    ITALIC("italic"),

    /**
     * The font is displayed in oblique style (a slanted version of the normal font).
     */
    OBLIQUE("oblique");

    private final String value;

    FontStyle(String value) {
        this.value = value;
    }

    /**
     * Gets the CSS property name this enum represents.
     *
     * @return the string {@code "font-style"}
     */
    public static String getPropertyName() {
        return "font-style";
    }

    /**
     * Gets the raw CSS value for this enum constant.
     *
     * @return the CSS string value (e.g., "italic", "oblique")
     */
    public String getValue() {
        return value;
    }
}