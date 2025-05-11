package infrastructure.net.web.ui.css;

/**
 * Enum representing valid CSS {@code pointer-events} property values.
 * <p>
 * The {@code pointer-events} property specifies under what circumstances
 * (if any) a particular element can become the target of pointer events (like mouse clicks or hovers).
 *
 * <ul>
 *     <li>{@code AUTO} – Default behavior; element reacts to pointer events if visible.</li>
 *     <li>{@code NONE} – Element does not receive any pointer events.</li>
 *     <li>{@code VISIBLE_PAINTED} – Only painted (visible fill or stroke) parts are clickable.</li>
 *     <li>{@code VISIBLE_FILL} – Only the fill part is clickable (if visible).</li>
 *     <li>{@code VISIBLE_STROKE} – Only the stroke part is clickable (if visible).</li>
 *     <li>{@code VISIBLE} – Both fill and stroke must be visible to receive events.</li>
 *     <li>{@code PAINTED} – Receives events only on visible painted areas (regardless of visibility).</li>
 *     <li>{@code FILL} – Pointer events apply to the fill area, whether visible or not.</li>
 *     <li>{@code STROKE} – Pointer events apply to the stroke area, whether visible or not.</li>
 *     <li>{@code ALL} – All parts of the element receive pointer events regardless of visibility.</li>
 * </ul>
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/pointer-events">MDN Reference</a>
 */
public enum PointerEvents {

    /**
     * Default behavior: element reacts to pointer events normally.
     */
    AUTO("auto"),

    /**
     * Element will not receive pointer events.
     */
    NONE("none"),

    /**
     * Only visible painted content (fill or stroke) can receive pointer events.
     */
    VISIBLE_PAINTED("visiblePainted"),

    /**
     * Only the visible fill area can receive pointer events.
     */
    VISIBLE_FILL("visibleFill"),

    /**
     * Only the visible stroke can receive pointer events.
     */
    VISIBLE_STROKE("visibleStroke"),

    /**
     * Both fill and stroke must be visible to receive pointer events.
     */
    VISIBLE("visible"),

    /**
     * Only painted areas (regardless of visibility) respond to pointer events.
     */
    PAINTED("painted"),

    /**
     * The fill area (visible or not) receives pointer events.
     */
    FILL("fill"),

    /**
     * The stroke area (visible or not) receives pointer events.
     */
    STROKE("stroke"),

    /**
     * All parts of the element receive pointer events, regardless of visibility or painting.
     */
    ALL("all");

    private final String value;

    PointerEvents(String value) {
        this.value = value;
    }

    /**
     * Gets the CSS property name this enum represents.
     *
     * @return the string {@code "pointer-events"}
     */
    public static String getPropertyName() {
        return "pointer-events";
    }

    /**
     * Gets the raw CSS value for this enum constant.
     *
     * @return the CSS string value (e.g., "none", "visibleStroke")
     */
    public String getValue() {
        return value;
    }
}
