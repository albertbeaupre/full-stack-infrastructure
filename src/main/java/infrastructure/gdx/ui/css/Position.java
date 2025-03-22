package infrastructure.gdx.ui.css;

import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration representing CSS {@code position} property values, designed for production use in
 * large-scale enterprise applications like those at Amazon. This enum maps to CSS utility class names
 * and their corresponding {@code position} values as defined in the CSS Positioned Layout Module Level 3
 * specification. It provides robust validation, logging, and integration with ph-css for generating
 * CSS declarations.
 * <p>
 * The {@code position} property specifies the positioning scheme for an element, affecting how it is
 * placed relative to its normal position, parent, or viewport. This enum is intended for single-threaded
 * contexts, such as UI styling or static CSS generation.
 * <p>
 * Key features:
 * <ul>
 *     <li>Input validation for class names and values</li>
 *     <li>Logging for enum instantiation and usage</li>
 *     <li>Direct conversion to ph-css {@link CSSDeclaration}</li>
 *     <li>Utility methods for lookup and mapping</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * Position position = Position.RELATIVE;
 * System.out.println(position.getClassName()); // "relative"
 * System.out.println(position.toCSSDeclaration()); // CSSDeclaration{position: relative}
 * </pre>
 *
 * @author Albert Beaupre
 * @version 1.0
 * @see <a href="https://www.w3.org/TR/css-position-3/#position-property">CSS Position Spec</a>
 * @since March 15th, 2025
 */
public enum Position {
    /**
     * Positions the element relative to its normal position, offset by top/right/bottom/left properties.
     * CSS: {@code position: relative}
     */
    RELATIVE("relative", "relative"),

    /**
     * Positions the element absolutely relative to its nearest positioned ancestor.
     * CSS: {@code position: absolute}
     */
    ABSOLUTE("absolute", "absolute"),

    /**
     * Fixes the element’s position relative to the viewport, remaining in place during scrolling.
     * CSS: {@code position: fixed}
     */
    FIXED("fixed", "fixed"),

    /**
     * Positions the element relative to its normal position until scrolled out of view, then fixes it.
     * CSS: {@code position: sticky}
     */
    STICKY("sticky", "sticky");

    /**
     * Logger instance for debugging and auditing enum operations.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Position.class);

    /**
     * The CSS property name constant for {@code position}.
     */
    private static final String PROPERTY_NAME = "position";

    /**
     * Immutable map of all enum values by class name for efficient lookup.
     */
    private static final Map<String, Position> CLASS_NAME_MAP;

    static {
        Map<String, Position> tempMap = new HashMap<>();
        for (Position value : values()) {
            tempMap.put(value.className, value);
        }
        CLASS_NAME_MAP = Collections.unmodifiableMap(tempMap);
    }

    /**
     * The CSS utility class name (e.g., "relative").
     */
    private final String className;

    /**
     * The CSS value (e.g., "relative").
     */
    private final String value;

    /**
     * Constructs a {@code Position} enum constant with the given class name and value.
     *
     * @param className the CSS utility class name (e.g., "relative")
     * @param value     the CSS value (e.g., "relative")
     * @throws IllegalArgumentException if any parameter is null, empty, or invalid
     */
    Position(String className, String value) {
        validateInputs(className, value);
        this.className = className;
        this.value = value;
    }

    /**
     * Validates the inputs for an enum constant.
     *
     * @param className the class name to validate
     * @param value     the value to validate
     * @throws IllegalArgumentException if any input is invalid
     */
    private void validateInputs(String className, String value) {
        if (className == null || className.trim().isEmpty()) {
            throw new IllegalArgumentException("Class name must not be null or empty");
        }
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Value must not be null or empty");
        }
        if (!isValidPositionValue(value)) {
            throw new IllegalArgumentException("Invalid position value: " + value);
        }
    }

    /**
     * Checks if a value is a valid {@code position} value per CSS spec.
     *
     * @param value the value to check
     * @return true if valid, false otherwise
     */
    private boolean isValidPositionValue(String value) {
        return switch (value) {
            case "relative", "absolute", "fixed", "sticky" -> true;
            default -> false;
        };
    }

    /**
     * Returns the CSS utility class name associated with this enum value.
     *
     * @return the class name (e.g., "relative")
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the CSS value associated with this enum.
     *
     * @return the value (e.g., "relative")
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the CSS property name, which is always "position" for this enum.
     *
     * @return the property name
     */
    public static String getPropertyName() {
        return PROPERTY_NAME;
    }

    /**
     * Converts this enum value to a ph-css {@link CSSDeclaration} object.
     * Useful for direct integration with {@link infrastructure.gdx.ui.css.Style} or other ph-css components.
     *
     * @return a {@link CSSDeclaration} representing this position setting
     */
    public CSSDeclaration toCSSDeclaration() {
        return new CSSDeclaration(PROPERTY_NAME, CSSExpression.createSimple(value));
    }

    /**
     * Looks up a {@code Position} enum value by its class name.
     *
     * @param className the class name to look up (e.g., "relative")
     * @return the matching {@code Position} enum, or null if not found
     */
    public static Position fromClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            LOGGER.warn("Attempted to lookup Position with null or empty className");
            return null;
        }
        Position result = CLASS_NAME_MAP.get(className);
        if (result == null) {
            LOGGER.debug("No Position enum found for className: {}", className);
        }
        return result;
    }

    /**
     * Returns an immutable map of all {@code Position} values keyed by their class names.
     * Useful for generating CSS utilities or documentation in enterprise systems.
     *
     * @return an unmodifiable map of class names to enum values
     */
    public static Map<String, Position> getAllByClassName() {
        return CLASS_NAME_MAP;
    }

    /**
     * Returns a string representation of this enum value, including class name and CSS property-value pair.
     *
     * @return a string in the format "[enumName] className -> property: value"
     */
    @Override
    public String toString() {
        return String.format("[%s] %s -> %s: %s", name(), className, PROPERTY_NAME, value);
    }
}