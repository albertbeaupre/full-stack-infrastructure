package infrastructure.net.web.ui.css;

/**
 * Enum representing valid CSS {@code background-attachment} property values.
 * <p>
 * The {@code background-attachment} property determines whether a background image scrolls
 * with the page, is fixed with regard to the viewport, or scrolls within the element.
 *
 * <ul>
 *     <li>{@code SCROLL} - The background scrolls along with the element’s content (default).</li>
 *     <li>{@code FIXED} - The background is fixed relative to the viewport.</li>
 *     <li>{@code LOCAL} - The background scrolls within the element’s scrollable area.</li>
 * </ul>
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/background-attachment">MDN Reference</a>
 */
public enum BackgroundAttachment {

    /**
     * The background scrolls along with the element’s content.
     */
    SCROLL("scroll"),

    /**
     * The background is fixed relative to the viewport.
     */
    FIXED("fixed"),

    /**
     * The background scrolls within the element's scrollable area.
     */
    LOCAL("local");

    private final String value;

    BackgroundAttachment(String value) {
        this.value = value;
    }

    /**
     * Gets the CSS property name this enum represents.
     *
     * @return the string {@code "background-attachment"}
     */
    public static String getPropertyName() {
        return "background-attachment";
    }

    /**
     * Gets the raw CSS value for this enum constant.
     *
     * @return the CSS string value (e.g., "scroll", "fixed")
     */
    public String getValue() {
        return value;
    }
}
