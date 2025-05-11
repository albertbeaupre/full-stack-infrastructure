package infrastructure.net.web.ui.css;

/**
 * Enum representing valid CSS {@code list-style-position} property values.
 * <p>
 * The {@code list-style-position} property specifies whether the list marker
 * (bullet or number) should appear inside or outside the content flow of a list item.
 *
 * <ul>
 *     <li>{@code INSIDE} – The marker is placed inside the list item's content box.</li>
 *     <li>{@code OUTSIDE} – The marker is placed outside the content box (default).</li>
 * </ul>
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/list-style-position">MDN Reference</a>
 */
public enum ListStylePosition {

    /**
     * The marker is placed inside the list item's content box.
     */
    INSIDE("inside"),

    /**
     * The marker is placed outside the list item's content box (default).
     */
    OUTSIDE("outside");

    private final String value;

    ListStylePosition(String value) {
        this.value = value;
    }

    /**
     * Gets the CSS property name this enum represents.
     *
     * @return the string {@code "list-style-position"}
     */
    public static String getPropertyName() {
        return "list-style-position";
    }

    /**
     * Gets the raw CSS value for this enum constant.
     *
     * @return the CSS string value (e.g., "inside", "outside")
     */
    public String getValue() {
        return value;
    }
}