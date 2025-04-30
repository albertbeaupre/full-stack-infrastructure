package infrastructure.web.ui.css;

import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration representing CSS {@code flex-grow} property values, designed for production use in
 * large-scale enterprise applications like those at Amazon. This enum maps to CSS utility class names
 * and their corresponding {@code flex-grow} values as defined in the CSS Flexible Box Layout Module
 * Level 1 specification. It provides robust input validation, logging, and integration with ph-css for
 * generating CSS declarations in single-threaded contexts, such as UI styling or static CSS generation.
 * <p>
 * The {@code flex-grow} property specifies how much a flex item will grow relative to other flex items
 * in the same container when there is extra space along the main axis. This enum ensures type-safe usage
 * and enterprise-grade reliability with features tailored for maintainability and scalability.
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
 * FlexGrow grow = FlexGrow.GROW;
 * System.out.println(grow.getClassName()); // "flex-grow"
 * System.out.println(grow.getValue());     // "1"
 * System.out.println(grow.toCSSDeclaration().getAsCSSString()); // "flex-grow: 1"
 * FlexGrow lookedUp = FlexGrow.fromClassName("flex-grow-0");
 * System.out.println(lookedUp); // "[GROW_0] flex-grow-0 -> flex-grow: 0"
 * </pre>
 *
 * @author Albert Beaupre
 * @version 1.0
 * @see <a href="https://www.w3.org/TR/css-flexbox-1/#flex-grow-property">CSS Flexbox Spec</a>
 * @since March 15th, 2025
 */
public enum FlexGrow {
    /**
     * Prevents the flex item from growing beyond its initial size.
     * CSS: {@code flex-grow: 0}
     * <p>
     * The item takes up only its intrinsic size and does not expand to fill available space.
     */
    GROW_0("flex-grow-0", "0"),

    /**
     * Allows the flex item to grow proportionally to fill available space.
     * CSS: {@code flex-grow: 1}
     * <p>
     * The item expands equally with other items set to 1, sharing the extra space.
     */
    GROW("flex-grow", "1");

    /**
     * Logger instance for debugging and auditing enum operations.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FlexGrow.class);

    /**
     * The CSS property name constant for {@code flex-grow}.
     */
    private static final String PROPERTY_NAME = "flex-grow";

    /**
     * Immutable map of all enum values by class name for efficient lookup.
     */
    private static final Map<String, FlexGrow> CLASS_NAME_MAP;

    static {
        Map<String, FlexGrow> tempMap = new HashMap<>();
        for (FlexGrow value : values()) {
            tempMap.put(value.className, value);
        }
        CLASS_NAME_MAP = Collections.unmodifiableMap(tempMap);
        LOGGER.debug("Initialized CLASS_NAME_MAP with {} FlexGrow enum values", CLASS_NAME_MAP.size());
    }

    /**
     * The CSS utility class name (e.g., "flex-grow-0").
     */
    private final String className;

    /**
     * The CSS value (e.g., "0").
     */
    private final String value;

    /**
     * Constructs a {@code FlexGrow} enum constant with the given class name, property, and value.
     * Validates inputs to ensure they conform to expected CSS standards for the {@code flex-grow} property.
     *
     * @param className the CSS utility class name (e.g., "flex-grow-0")
     * @param value     the CSS value (e.g., "0")
     * @throws IllegalArgumentException if any parameter is null, empty, or invalid
     */
    FlexGrow(String className, String value) {
        validateInputs(className, value);
        this.className = className;
        this.value = value;
    }

    /**
     * Validates the inputs for an enum constant to ensure they are valid for the {@code flex-grow} property.
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
        // Validate against common flex-grow values (expandable for full numeric range if needed)
        if (!isValidFlexGrowValue(value)) {
            throw new IllegalArgumentException("Invalid flex-grow value: " + value);
        }
    }

    /**
     * Checks if a value is a valid {@code flex-grow} value for this enum’s scope.
     * This enum focuses on common integer values (0 and 1); the full CSS spec allows any non-negative number.
     *
     * @param value the value to check
     * @return true if valid, false otherwise
     */
    private boolean isValidFlexGrowValue(String value) {
        return switch (value) {
            case "0", "1" -> true;
            default -> false;
        };
    }

    /**
     * Returns the CSS utility class name associated with this enum value.
     *
     * @return the class name (e.g., "flex-grow-0")
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the CSS property name, which is always "flex-grow" for this enum.
     *
     * @return the property name
     */
    public static String getPropertyName() {
        return PROPERTY_NAME;
    }

    /**
     * Returns the CSS value associated with this enum.
     *
     * @return the value (e.g., "0")
     */
    public String getValue() {
        return value;
    }

    /**
     * Converts this enum value to a ph-css {@link CSSDeclaration} object.
     * This facilitates direct integration with {@link Style} or other ph-css components for
     * generating CSS rules programmatically.
     *
     * @return a {@link CSSDeclaration} representing this flex-grow setting
     */
    public CSSDeclaration toCSSDeclaration() {
        return new CSSDeclaration(PROPERTY_NAME, CSSExpression.createSimple(value));
    }

    /**
     * Looks up a {@code FlexGrow} enum value by its class name.
     * Returns null if the class name is not found, logging a debug message to aid troubleshooting.
     *
     * @param className the class name to look up (e.g., "flex-grow")
     * @return the matching {@code FlexGrow} enum, or null if not found
     */
    public static FlexGrow fromClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            LOGGER.warn("Attempted to lookup FlexGrow with null or empty className");
            return null;
        }
        FlexGrow result = CLASS_NAME_MAP.get(className);
        if (result == null) {
            LOGGER.debug("No FlexGrow enum found for className: {}", className);
        }
        return result;
    }

    /**
     * Returns an immutable map of all {@code FlexGrow} values keyed by their class names.
     * This is useful for generating CSS utilities, documentation, or validation tables in enterprise systems.
     *
     * @return an unmodifiable map of class names to enum values
     */
    public static Map<String, FlexGrow> getAllByClassName() {
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