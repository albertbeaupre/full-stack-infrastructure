package infrastructure.net.web.ui.css;

/**
 * Enum representing valid CSS {@code background-repeat} property values.
 * <p>
 * The {@code background-repeat} property defines how background images are repeated
 * within an element.
 *
 * <ul>
 *     <li>{@code REPEAT} - The background image is repeated both horizontally and vertically (default).</li>
 *     <li>{@code NO_REPEAT} - The background image is not repeated.</li>
 *     <li>{@code REPEAT_X} - The background image is repeated only horizontally.</li>
 *     <li>{@code REPEAT_Y} - The background image is repeated only vertically.</li>
 *     <li>{@code SPACE} - The image is repeated as often as possible without being clipped and extra space is distributed between images.</li>
 *     <li>{@code ROUND} - The image is repeated and scaled so that it fits exactly into the container without clipping.</li>
 * </ul>
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/background-repeat">MDN Reference</a>
 */
public enum BackgroundRepeat {

    /**
     * The background image is repeated in both horizontal and vertical directions.
     */
    REPEAT("repeat"),

    /**
     * The background image is not repeated.
     */
    NO_REPEAT("no-repeat"),

    /**
     * The background image is repeated only in the horizontal direction.
     */
    REPEAT_X("repeat-x"),

    /**
     * The background image is repeated only in the vertical direction.
     */
    REPEAT_Y("repeat-y"),

    /**
     * The background image is repeated and spaced to fill the container without clipping.
     */
    SPACE("space"),

    /**
     * The background image is repeated and scaled to fill the container without clipping.
     */
    ROUND("round");

    private final String value;

    BackgroundRepeat(String value) {
        this.value = value;
    }

    /**
     * Gets the CSS property name this enum represents.
     *
     * @return the string {@code "background-repeat"}
     */
    public static String getPropertyName() {
        return "background-repeat";
    }

    /**
     * Gets the raw CSS value for this enum constant.
     *
     * @return the CSS string value (e.g., "repeat", "no-repeat")
     */
    public String getValue() {
        return value;
    }
}
