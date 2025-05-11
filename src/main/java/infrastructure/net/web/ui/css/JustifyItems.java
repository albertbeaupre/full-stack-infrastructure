package infrastructure.net.web.ui.css;

/**
 * Enum representing valid CSS {@code justify-items} property values.
 * <p>
 * The {@code justify-items} property defines the default alignment for items inside a container
 * along the inline (row) axis in a grid or flex layout.
 *
 * <ul>
 *     <li>{@code AUTO} - Inherits the value from the parent or uses default behavior.</li>
 *     <li>{@code START} - Aligns items to the start of the container's inline axis.</li>
 *     <li>{@code END} - Aligns items to the end of the container's inline axis.</li>
 *     <li>{@code CENTER} - Centers items along the inline axis.</li>
 *     <li>{@code STRETCH} - Stretches items to fill the container (default for many layout contexts).</li>
 *     <li>{@code BASELINE} - Aligns items to their baseline.</li>
 * </ul>
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/justify-items">MDN Reference</a>
 */
public enum JustifyItems {

    /**
     * Uses the default alignment or inherits from the parent container.
     */
    AUTO("auto"),

    /**
     * Aligns items to the start of the inline axis.
     */
    START("start"),

    /**
     * Aligns items to the end of the inline axis.
     */
    END("end"),

    /**
     * Centers items along the inline axis.
     */
    CENTER("center"),

    /**
     * Stretches items to fill the container.
     */
    STRETCH("stretch"),

    /**
     * Aligns items based on their baseline.
     */
    BASELINE("baseline");

    private final String value;

    JustifyItems(String value) {
        this.value = value;
    }

    /**
     * Gets the CSS property name this enum represents.
     *
     * @return the string {@code "justify-items"}
     */
    public static String getPropertyName() {
        return "justify-items";
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