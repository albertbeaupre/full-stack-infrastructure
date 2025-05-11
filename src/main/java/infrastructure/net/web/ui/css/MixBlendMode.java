package infrastructure.net.web.ui.css;

/**
 * Enum representing valid CSS {@code mix-blend-mode} property values.
 * <p>
 * The {@code mix-blend-mode} property defines how an element's content should blend
 * with the background or content beneath it.
 *
 * <ul>
 *     <li>{@code NORMAL} – Default rendering with no blending.</li>
 *     <li>{@code MULTIPLY} – Multiplies the background and foreground colors, resulting in darker tones.</li>
 *     <li>{@code SCREEN} – Multiplies the inverse of the background and foreground, resulting in lighter tones.</li>
 *     <li>{@code OVERLAY} – Combines multiply and screen based on background lightness.</li>
 *     <li>{@code DARKEN} – Chooses the darker of the background and foreground colors.</li>
 *     <li>{@code LIGHTEN} – Chooses the lighter of the background and foreground colors.</li>
 *     <li>{@code COLOR_DODGE} – Brightens the background to reflect the foreground.</li>
 *     <li>{@code COLOR_BURN} – Darkens the background to reflect the foreground.</li>
 *     <li>{@code HARD_LIGHT} – Uses multiply or screen depending on the foreground value.</li>
 *     <li>{@code SOFT_LIGHT} – Similar to hard light but softer transitions.</li>
 *     <li>{@code DIFFERENCE} – Subtracts darker from lighter colors.</li>
 *     <li>{@code EXCLUSION} – Similar to difference but lower contrast.</li>
 *     <li>{@code HUE} – Applies the hue of the foreground to the background.</li>
 *     <li>{@code SATURATION} – Applies the saturation of the foreground to the background.</li>
 *     <li>{@code COLOR} – Applies the hue and saturation of the foreground while keeping the background lightness.</li>
 *     <li>{@code LUMINOSITY} – Applies the lightness of the foreground while keeping the background hue and saturation.</li>
 * </ul>
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/mix-blend-mode">MDN Reference</a>
 */
public enum MixBlendMode {

    /** Renders the element normally without blending. */
    NORMAL("normal"),

    /** Multiplies the background and foreground colors. */
    MULTIPLY("multiply"),

    /** Screens the background and foreground colors. */
    SCREEN("screen"),

    /** Uses overlay mode: a combination of multiply and screen. */
    OVERLAY("overlay"),

    /** Displays the darker of the background and foreground. */
    DARKEN("darken"),

    /** Displays the lighter of the background and foreground. */
    LIGHTEN("lighten"),

    /** Brightens the background to reflect the foreground. */
    COLOR_DODGE("color-dodge"),

    /** Darkens the background to reflect the foreground. */
    COLOR_BURN("color-burn"),

    /** Applies hard light blending based on foreground brightness. */
    HARD_LIGHT("hard-light"),

    /** Applies soft light blending for subtle effects. */
    SOFT_LIGHT("soft-light"),

    /** Subtracts the darker color from the lighter one. */
    DIFFERENCE("difference"),

    /** Reduces contrast for subtle differences. */
    EXCLUSION("exclusion"),

    /** Applies only the hue of the foreground. */
    HUE("hue"),

    /** Applies only the saturation of the foreground. */
    SATURATION("saturation"),

    /** Applies hue and saturation of the foreground while preserving background luminosity. */
    COLOR("color"),

    /** Applies luminosity of the foreground while preserving background hue and saturation. */
    LUMINOSITY("luminosity");

    private final String value;

    MixBlendMode(String value) {
        this.value = value;
    }

    /**
     * Gets the CSS property name this enum represents.
     *
     * @return the string {@code "mix-blend-mode"}
     */
    public static String getPropertyName() {
        return "mix-blend-mode";
    }

    /**
     * Gets the raw CSS value for this enum constant.
     *
     * @return the CSS string value (e.g., "multiply", "screen")
     */
    public String getValue() {
        return value;
    }
}
