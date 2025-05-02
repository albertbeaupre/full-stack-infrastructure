package infrastructure.net.web.ui.css;

import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration representing CSS {@code flex-direction} property values, designed for production use in
 * large-scale enterprise applications like those at Amazon. This enum maps to CSS utility class names
 * and their corresponding {@code flex-direction} values as defined in the CSS Flexible Box Layout Module
 * Level 1 specification. It provides robust input validation, logging, and integration with ph-css for
 * generating CSS declarations in single-threaded contexts, such as UI styling or static CSS generation.
 * <p>
 * The {@code flex-direction} property defines the main axis direction along which flex items are placed
 * in a flex container. This enum ensures type-safe usage and enterprise-grade reliability with features
 * tailored for maintainability and scalability.
 * <p>
 * Key features:
 * <ul>
 *     <li>Input validation for class names, properties, and values to ensure correctness</li>
 *     <li>Logging for instantiation and lookup operations to aid debugging and auditing</li>
 *     <li>Direct conversion to ph-css {@link CSSDeclaration} for seamless integration</li>
 *     <li>Utility methods for dynamic lookup and mapping of enum values</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * FlexDirection direction = FlexDirection.ROW;
 * System.out.println(direction.getClassName()); // "flex-row"
 * System.out.println(direction.getValue());     // "row"
 * System.out.println(direction.toCSSDeclaration().getAsCSSString()); // "flex-direction: row"
 * FlexDirection lookedUp = FlexDirection.fromClassName("flex-col");
 * System.out.println(lookedUp); // "[COLUMN] flex-col -> flex-direction: column"
 * </pre>
 *
 * @author Albert Beaupre
 * @version 1.0
 * @see <a href="https://www.w3.org/TR/css-flexbox-1/#flex-direction-property">CSS Flexbox Spec</a>
 * @since March 15th, 2025
 */
public enum FlexDirection {
    /**
     * Sets the main axis to horizontal, left-to-right.
     * CSS: {@code flex-direction: row}
     * <p>
     * Flex items are laid out in a row from left to right (default behavior).
     */
    ROW("flex-row", "row"),

    /**
     * Sets the main axis to horizontal, right-to-left.
     * CSS: {@code flex-direction: row-reverse}
     * <p>
     * Flex items are laid out in a row from right to left.
     */
    ROW_REVERSE("flex-row-reverse", "row-reverse"),

    /**
     * Sets the main axis to vertical, top-to-bottom.
     * CSS: {@code flex-direction: column}
     * <p>
     * Flex items are laid out in a column from top to bottom.
     */
    COLUMN("flex-col", "column"),

    /**
     * Sets the main axis to vertical, bottom-to-top.
     * CSS: {@code flex-direction: column-reverse}
     * <p>
     * Flex items are laid out in a column from bottom to top.
     */
    COLUMN_REVERSE("flex-col-reverse", "column-reverse");

    /**
     * Logger instance for debugging and auditing enum operations.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FlexDirection.class);

    /**
     * The CSS property name constant for {@code flex-direction}.
     */
    private static final String PROPERTY_NAME = "flex-direction";

    /**
     * Immutable map of all enum values by class name for efficient lookup.
     */
    private static final Map<String, FlexDirection> CLASS_NAME_MAP;

    static {
        Map<String, FlexDirection> tempMap = new HashMap<>();
        for (FlexDirection value : values()) {
            tempMap.put(value.className, value);
        }
        CLASS_NAME_MAP = Collections.unmodifiableMap(tempMap);
        LOGGER.debug("Initialized CLASS_NAME_MAP with {} FlexDirection enum values", CLASS_NAME_MAP.size());
    }

    /**
     * The CSS utility class name (e.g., "flex-row").
     */
    private final String className;

    /**
     * The CSS value (e.g., "row").
     */
    private final String value;

    /**
     * Constructs a {@code FlexDirection} enum constant with the given class name, property, and value.
     * Validates inputs to ensure they conform to expected CSS standards for the {@code flex-direction} property.
     *
     * @param className the CSS utility class name (e.g., "flex-row")
     * @param value     the CSS value (e.g., "row")
     * @throws IllegalArgumentException if any parameter is null, empty, or invalid
     */
    FlexDirection(String className, String value) {
        validateInputs(className, value);
        this.className = className;
        this.value = value;
    }

    /**
     * Validates the inputs for an enum constant to ensure they are valid for the {@code flex-direction} property.
     *
     * @param className the class name to validate
     * @param value     the value to validate
     * @throws IllegalArgumentException if any input is null, empty, or does not conform to expected values
     */
    private void validateInputs(String className, String value) {
        if (className == null || className.trim().isEmpty()) {
            throw new IllegalArgumentException("Class name must not be null or empty");
        }
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Value must not be null or empty");
        }
        // Validate against CSS spec for flex-direction
        if (!isValidFlexDirectionValue(value)) {
            throw new IllegalArgumentException("Invalid flex-direction value: " + value);
        }
    }

    /**
     * Checks if a value is a valid {@code flex-direction} value per CSS spec.
     *
     * @param value the value to check
     * @return true if valid, false otherwise
     */
    private boolean isValidFlexDirectionValue(String value) {
        return switch (value) {
            case "row", "row-reverse", "column", "column-reverse" -> true;
            default -> false;
        };
    }

    /**
     * Returns the CSS utility class name associated with this enum value.
     *
     * @return the class name (e.g., "flex-row")
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the CSS value associated with this enum.
     *
     * @return the value (e.g., "row")
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the CSS property name, which is always "flex-direction" for this enum.
     *
     * @return the property name
     */
    public static String getPropertyName() {
        return PROPERTY_NAME;
    }

    /**
     * Converts this enum value to a ph-css {@link CSSDeclaration} object.
     * This facilitates direct integration with {@link Style} or other ph-css components for
     * generating CSS rules programmatically.
     *
     * @return a {@link CSSDeclaration} representing this flex-direction setting
     */
    public CSSDeclaration toCSSDeclaration() {
        return new CSSDeclaration(PROPERTY_NAME, CSSExpression.createSimple(value));
    }

    /**
     * Looks up a {@code FlexDirection} enum value by its class name.
     * Returns null if the class name is not found, logging a debug message to aid troubleshooting.
     *
     * @param className the class name to look up (e.g., "flex-col")
     * @return the matching {@code FlexDirection} enum, or null if not found
     */
    public static FlexDirection fromClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            LOGGER.warn("Attempted to lookup FlexDirection with null or empty className");
            return null;
        }
        FlexDirection result = CLASS_NAME_MAP.get(className);
        if (result == null) {
            LOGGER.debug("No FlexDirection enum found for className: {}", className);
        }
        return result;
    }

    /**
     * Returns an immutable map of all {@code FlexDirection} values keyed by their class names.
     * This is useful for generating CSS utilities, documentation, or validation tables in enterprise systems.
     *
     * @return an unmodifiable map of class names to enum values
     */
    public static Map<String, FlexDirection> getAllByClassName() {
        return CLASS_NAME_MAP;
    }

    /**
     * Returns a string representation of this enum value, including the enum name, class name, and
     * CSS property-value pair. This format is designed for logging and debugging in production environments.
     *
     * @return a string in the format "[enumName] className -> property: value"
     */
    @Override
    public String toString() {
        return String.format("[%s] %s -> %s: %s", name(), className, PROPERTY_NAME, value);
    }
}