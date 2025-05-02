package infrastructure.net.web.ui.css;

import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration representing CSS {@code flex-wrap} property values, designed for production use in
 * large-scale enterprise applications like those at Amazon. This enum maps to CSS utility class names
 * and their corresponding {@code flex-wrap} values as defined in the CSS Flexible Box Layout Module
 * Level 1 specification. It provides robust input validation, logging, and integration with ph-css for
 * generating CSS declarations in single-threaded contexts, such as UI styling or static CSS generation.
 * <p>
 * The {@code flex-wrap} property determines whether flex items are forced onto a single line or can wrap
 * onto multiple lines within a flex container, and if wrapping, the direction of the wrap. This enum ensures
 * type-safe usage and enterprise-grade reliability with features tailored for maintainability and scalability.
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
 * FlexWrap wrap = FlexWrap.WRAP;
 * System.out.println(wrap.getClassName()); // "flex-wrap"
 * System.out.println(wrap.getValue());     // "wrap"
 * System.out.println(wrap.toCSSDeclaration().getAsCSSString()); // "flex-wrap: wrap"
 * FlexWrap lookedUp = FlexWrap.fromClassName("flex-nowrap");
 * System.out.println(lookedUp); // "[NOWRAP] flex-nowrap -> flex-wrap: nowrap"
 * </pre>
 *
 * @author Albert Beaupre
 * @version 1.0
 * @see <a href="https://www.w3.org/TR/css-flexbox-1/#flex-wrap-property">CSS Flexbox Spec</a>
 * @since March 15th, 2025
 */
public enum FlexWrap {
    /**
     * Allows flex items to wrap onto multiple lines as needed, from top to bottom or left to right.
     * CSS: {@code flex-wrap: wrap}
     * <p>
     * Items wrap in the normal direction when they exceed the container's main axis size.
     */
    WRAP("flex-wrap", "wrap"),

    /**
     * Forces all flex items onto a single line, preventing wrapping.
     * CSS: {@code flex-wrap: nowrap}
     * <p>
     * Items remain in a single line, potentially overflowing the container if space is insufficient.
     */
    NOWRAP("flex-nowrap", "nowrap"),

    /**
     * Allows flex items to wrap onto multiple lines, but in reverse order (bottom to top or right to left).
     * CSS: {@code flex-wrap: wrap-reverse}
     * <p>
     * Items wrap in the opposite direction compared to {@code wrap}.
     */
    WRAP_REVERSE("flex-wrap-reverse", "wrap-reverse");

    /**
     * Logger instance for debugging and auditing enum operations.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FlexWrap.class);

    /**
     * The CSS property name constant for {@code flex-wrap}.
     */
    private static final String PROPERTY_NAME = "flex-wrap";

    /**
     * Immutable map of all enum values by class name for efficient lookup.
     */
    private static final Map<String, FlexWrap> CLASS_NAME_MAP;

    static {
        Map<String, FlexWrap> tempMap = new HashMap<>();
        for (FlexWrap value : values()) {
            tempMap.put(value.className, value);
        }
        CLASS_NAME_MAP = Collections.unmodifiableMap(tempMap);
        LOGGER.debug("Initialized CLASS_NAME_MAP with {} FlexWrap enum values", CLASS_NAME_MAP.size());
    }

    /**
     * The CSS utility class name (e.g., "flex-wrap").
     */
    private final String className;

    /**
     * The CSS value (e.g., "wrap").
     */
    private final String value;

    /**
     * Constructs a {@code FlexWrap} enum constant with the given class name, property, and value.
     * Validates inputs to ensure they conform to expected CSS standards for the {@code flex-wrap} property.
     *
     * @param className the CSS utility class name (e.g., "flex-wrap")
     * @param value     the CSS value (e.g., "wrap")
     * @throws IllegalArgumentException if any parameter is null, empty, or invalid
     */
    FlexWrap(String className, String value) {
        validateInputs(className, value);
        this.className = className;
        this.value = value;
    }

    /**
     * Validates the inputs for an enum constant to ensure they are valid for the {@code flex-wrap} property.
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
        // Validate against CSS spec for flex-wrap
        if (!isValidFlexWrapValue(value)) {
            throw new IllegalArgumentException("Invalid flex-wrap value: " + value);
        }
    }

    /**
     * Checks if a value is a valid {@code flex-wrap} value per CSS spec.
     *
     * @param value the value to check
     * @return true if valid, false otherwise
     */
    private boolean isValidFlexWrapValue(String value) {
        return switch (value) {
            case "wrap", "nowrap", "wrap-reverse" -> true;
            default -> false;
        };
    }

    /**
     * Returns the CSS utility class name associated with this enum value.
     *
     * @return the class name (e.g., "flex-wrap")
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the CSS value associated with this enum.
     *
     * @return the value (e.g., "wrap")
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the CSS property name, which is always "flex-wrap" for this enum.
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
     * @return a {@link CSSDeclaration} representing this flex-wrap setting
     */
    public CSSDeclaration toCSSDeclaration() {
        return new CSSDeclaration(PROPERTY_NAME, CSSExpression.createSimple(value));
    }

    /**
     * Looks up a {@code FlexWrap} enum value by its class name.
     * Returns null if the class name is not found, logging a debug message to aid troubleshooting.
     *
     * @param className the class name to look up (e.g., "flex-nowrap")
     * @return the matching {@code FlexWrap} enum, or null if not found
     */
    public static FlexWrap fromClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            LOGGER.warn("Attempted to lookup FlexWrap with null or empty className");
            return null;
        }
        FlexWrap result = CLASS_NAME_MAP.get(className);
        if (result == null) {
            LOGGER.debug("No FlexWrap enum found for className: {}", className);
        }
        return result;
    }

    /**
     * Returns an immutable map of all {@code FlexWrap} values keyed by their class names.
     * This is useful for generating CSS utilities, documentation, or validation tables in enterprise systems.
     *
     * @return an unmodifiable map of class names to enum values
     */
    public static Map<String, FlexWrap> getAllByClassName() {
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