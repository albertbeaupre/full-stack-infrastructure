package infrastructure.net.web.ui.css;

import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration representing CSS {@code flex} shorthand property values, designed for production use in
 * large-scale enterprise applications like those at Amazon. This enum maps to CSS utility class names
 * and their corresponding {@code flex} values as defined in the CSS Flexible Box Layout Module Level 1
 * specification. It provides robust input validation, logging, and integration with ph-css for generating
 * CSS declarations in single-threaded contexts, such as UI styling or static CSS generation.
 * <p>
 * The {@code flex} property is a shorthand for {@code flex-grow}, {@code flex-shrink}, and {@code flex-basis},
 * controlling how flex items grow, shrink, and establish their base size within a flex container. This enum
 * ensures type-safe usage and enterprise-grade reliability with features tailored for maintainability and
 * scalability.
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
 * Flex flex = Flex.FLEX_1;
 * System.out.println(flex.getClassName()); // "flex-1"
 * System.out.println(flex.getValue());     // "1 1 0%"
 * System.out.println(flex.toCSSDeclaration().getAsCSSString()); // "flex: 1 1 0%"
 * Flex lookedUp = Flex.fromClassName("flex-auto");
 * System.out.println(lookedUp); // "[AUTO] flex-auto -> flex: 1 1 auto"
 * </pre>
 *
 * @author Albert Beaupre
 * @version 1.0
 * @see <a href="https://www.w3.org/TR/css-flexbox-1/#flex-property">CSS Flexbox Spec</a>
 * @since March 15th, 2025
 */
public enum Flex {
    /**
     * Sets flex to grow and shrink equally with a base size of 0%.
     * CSS: {@code flex: 1 1 0%}
     * <p>
     * This allows the item to grow and shrink proportionally, starting from no intrinsic size.
     */
    FLEX_1("flex-1", "1 1 0%"),

    /**
     * Sets flex to grow and shrink equally with an automatic base size.
     * CSS: {@code flex: 1 1 auto}
     * <p>
     * This allows the item to grow and shrink based on its content size or specified width/height.
     */
    AUTO("flex-auto", "1 1 auto"),

    /**
     * Sets flex to not grow but shrink with an automatic base size.
     * CSS: {@code flex: 0 1 auto}
     * <p>
     * This prevents growth beyond the item's intrinsic size while allowing it to shrink if needed.
     */
    INITIAL("flex-initial", "0 1 auto"),

    /**
     * Disables flex growth and shrinkage, fixing the item’s size.
     * CSS: {@code flex: none}
     * <p>
     * Equivalent to {@code flex: 0 0 auto}, making the item inflexible.
     */
    NONE("flex-none", "none");

    /**
     * Logger instance for debugging and auditing enum operations.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Flex.class);

    /**
     * The CSS property name constant for {@code flex}.
     */
    private static final String PROPERTY_NAME = "flex";

    /**
     * Immutable map of all enum values by class name for efficient lookup.
     */
    private static final Map<String, Flex> CLASS_NAME_MAP;

    static {
        Map<String, Flex> tempMap = new HashMap<>();
        for (Flex value : values()) {
            tempMap.put(value.className, value);
        }
        CLASS_NAME_MAP = Collections.unmodifiableMap(tempMap);
        LOGGER.debug("Initialized CLASS_NAME_MAP with {} Flex enum values", CLASS_NAME_MAP.size());
    }

    /**
     * The CSS utility class name (e.g., "flex-1").
     */
    private final String className;

    /**
     * The CSS value (e.g., "1 1 0%").
     */
    private final String value;

    /**
     * Constructs a {@code Flex} enum constant with the given class name, property, and value.
     * Validates inputs to ensure they conform to expected CSS standards for the {@code flex} property.
     *
     * @param className the CSS utility class name (e.g., "flex-1")
     * @param value     the CSS value (e.g., "1 1 0%")
     * @throws IllegalArgumentException if any parameter is null, empty, or invalid
     */
    Flex(String className, String value) {
        validateInputs(className, value);
        this.className = className;
        this.value = value;
    }

    /**
     * Validates the inputs for an enum constant to ensure they are valid for the {@code flex} property.
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
        // Validate against common flex shorthand values (expandable for full spec if needed)
        if (!isValidFlexValue(value)) {
            throw new IllegalArgumentException("Invalid flex value: " + value);
        }
    }

    /**
     * Checks if a value is a valid {@code flex} shorthand value for this enum’s scope.
     * This enum focuses on common shorthand values; the full CSS spec allows more combinations.
     *
     * @param value the value to check
     * @return true if valid, false otherwise
     */
    private boolean isValidFlexValue(String value) {
        return switch (value) {
            case "1 1 0%", "1 1 auto", "0 1 auto", "none" -> true;
            default -> false;
        };
    }

    /**
     * Returns the CSS utility class name associated with this enum value.
     *
     * @return the class name (e.g., "flex-1")
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the CSS property name, which is always "flex" for this enum.
     *
     * @return the property name
     */
    public static String getPropertyName() {
        return PROPERTY_NAME;
    }

    /**
     * Returns the CSS value associated with this enum.
     *
     * @return the value (e.g., "1 1 0%")
     */
    public String getValue() {
        return value;
    }

    /**
     * Converts this enum value to a ph-css {@link CSSDeclaration} object.
     * This facilitates direct integration with {@link Style} or other ph-css components for
     * generating CSS rules programmatically.
     *
     * @return a {@link CSSDeclaration} representing this flex setting
     */
    public CSSDeclaration toCSSDeclaration() {
        return new CSSDeclaration(PROPERTY_NAME, CSSExpression.createSimple(value));
    }

    /**
     * Looks up a {@code Flex} enum value by its class name.
     * Returns null if the class name is not found, logging a debug message to aid troubleshooting.
     *
     * @param className the class name to look up (e.g., "flex-auto")
     * @return the matching {@code Flex} enum, or null if not found
     */
    public static Flex fromClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            LOGGER.warn("Attempted to lookup Flex with null or empty className");
            return null;
        }
        Flex result = CLASS_NAME_MAP.get(className);
        if (result == null) {
            LOGGER.debug("No Flex enum found for className: {}", className);
        }
        return result;
    }

    /**
     * Returns an immutable map of all {@code Flex} values keyed by their class names.
     * This is useful for generating CSS utilities, documentation, or validation tables in enterprise systems.
     *
     * @return an unmodifiable map of class names to enum values
     */
    public static Map<String, Flex> getAllByClassName() {
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