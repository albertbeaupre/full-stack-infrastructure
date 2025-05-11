package infrastructure.net.web.ui.css;

/**
 * Enum representing valid CSS {@code user-select} property values.
 * <p>
 * The {@code user-select} property controls whether the user can select text or elements.
 *
 * <ul>
 *     <li>{@code AUTO} – Default behavior, usually allows text selection depending on the element type.</li>
 *     <li>{@code NONE} – Disables text selection by the user.</li>
 *     <li>{@code TEXT} – Enables selection of text content only.</li>
 *     <li>{@code CONTAIN} – Restricts selection to the element and its descendants.</li>
 *     <li>{@code ALL} – Allows selection of all text within the element when any part is selected.</li>
 *     <li>{@code ELEMENT} – Applies selection to the entire element (non-standard, may not be widely supported).</li>
 * </ul>
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/user-select">MDN Reference</a>
 */
public enum UserSelect {

    /**
     * Uses the browser's default behavior for text selection.
     */
    AUTO("auto"),

    /**
     * Disables any text selection within the element.
     */
    NONE("none"),

    /**
     * Allows selection of text content only.
     */
    TEXT("text"),

    /**
     * Limits selection to within the element boundary.
     */
    CONTAIN("contain"),

    /**
     * Selecting any part selects the entire content of the element.
     */
    ALL("all"),

    /**
     * Applies selection rules to the element as a whole (non-standard).
     */
    ELEMENT("element");

    private final String value;

    UserSelect(String value) {
        this.value = value;
    }

    /**
     * Gets the CSS property name this enum represents.
     *
     * @return the string {@code "user-select"}
     */
    public static String getPropertyName() {
        return "user-select";
    }

    /**
     * Gets the raw CSS value for this enum constant.
     *
     * @return the CSS string value (e.g., "none", "text")
     */
    public String getValue() {
        return value;
    }
}
