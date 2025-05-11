package infrastructure.net.web.ui.css;

/**
 * Enum representing valid CSS {@code scroll-behavior} property values.
 * <p>
 * The {@code scroll-behavior} property specifies whether scrolling should happen instantly or smoothly
 * when triggered by navigation or JavaScript.
 *
 * <ul>
 *     <li>{@code AUTO} – Default behavior; scrolling happens immediately (no animation).</li>
 *     <li>{@code SMOOTH} – Scrolling is animated smoothly.</li>
 * </ul>
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/scroll-behavior">MDN Reference</a>
 */
public enum ScrollBehavior {

    /**
     * Default scroll behavior: jumps instantly to the target position.
     */
    AUTO("auto"),

    /**
     * Scrolls to the target position smoothly with animation.
     */
    SMOOTH("smooth");

    private final String value;

    ScrollBehavior(String value) {
        this.value = value;
    }

    /**
     * Gets the CSS property name this enum represents.
     *
     * @return the string {@code "scroll-behavior"}
     */
    public static String getPropertyName() {
        return "scroll-behavior";
    }

    /**
     * Gets the raw CSS value for this enum constant.
     *
     * @return the CSS string value (e.g., "auto", "smooth")
     */
    public String getValue() {
        return value;
    }
}
