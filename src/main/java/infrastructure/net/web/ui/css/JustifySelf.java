package infrastructure.net.web.ui.css;

/**
 * Enum representing valid CSS {@code justify-self} property values.
 * <p>
 * The {@code justify-self} property sets the alignment of a single grid or flex item
 * inside the parent container along the inline (row) axis.
 *
 * <ul>
 *     <li>{@code AUTO} – Inherits the alignment from the container or uses the default.</li>
 *     <li>{@code START} – Aligns the item to the start of the inline axis.</li>
 *     <li>{@code END} – Aligns the item to the end of the inline axis.</li>
 *     <li>{@code CENTER} – Centers the item along the inline axis.</li>
 *     <li>{@code STRETCH} – Stretches the item to fill the container.</li>
 * </ul>
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/justify-self">MDN Reference</a>
 */
public enum JustifySelf {

    /**
     * Uses the default alignment or inherits it from the parent.
     */
    AUTO("auto"),

    /**
     * Aligns the item to the start of the inline axis.
     */
    START("start"),

    /**
     * Aligns the item to the end of the inline axis.
     */
    END("end"),

    /**
     * Centers the item along the inline axis.
     */
    CENTER("center"),

    /**
     * Stretches the item to fill the container.
     */
    STRETCH("stretch");

    private final String value;

    JustifySelf(String value) {
        this.value = value;
    }

    /**
     * Gets the CSS property name this enum represents.
     *
     * @return the string {@code "justify-self"}
     */
    public static String getPropertyName() {
        return "justify-self";
    }

    /**
     * Gets the raw CSS value for this enum constant.
     *
     * @return the CSS string value (e.g., "center", "stretch")
     */
    public String getValue() {
        return value;
    }
}