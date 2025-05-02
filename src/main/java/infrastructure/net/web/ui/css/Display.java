package infrastructure.net.web.ui.css;

import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration representing CSS {@code display} property values, designed for production use in
 * large-scale enterprise applications like those at Amazon. This enum maps to CSS utility class names
 * and their corresponding {@code display} values as defined in the CSS Display Module Level 3 specification.
 * It provides robust validation, logging, and integration with ph-css for generating CSS declarations
 * in single-threaded contexts, such as UI styling or static CSS generation.
 * <p>
 * The {@code display} property defines how an element is rendered in the layout, including whether it
 * establishes a flex container or is hidden from view. This enum focuses on common flex-related and
 * visibility values, ensuring type-safe usage and enterprise-grade reliability.
 * <p>
 * Key features:
 * <ul>
 *     <li>Input validation for class names, properties, and values</li>
 *     <li>Logging for instantiation and lookup operations</li>
 *     <li>Direct conversion to ph-css {@link CSSDeclaration}</li>
 *     <li>Utility methods for dynamic lookup and mapping</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * Display display = Display.FLEX;
 * System.out.println(display.getClassName()); // "flex"
 * System.out.println(display.toCSSDeclaration()); // CSSDeclaration{display: flex}
 * </pre>
 *
 * @author Albert Beaupre
 * @version 1.0
 * @since March 15th, 2025
 * @see <a href="https://www.w3.org/TR/css-display-3/">CSS Display Spec</a>
 */
public enum Display {
    /**
     * Sets the element as a block-level flex container.
     * CSS: {@code display: flex}
     */
    FLEX("flex", "flex"),

    /**
     * Sets the element as an inline-level flex container.
     * CSS: {@code display: inline-flex}
     */
    INLINE_FLEX("inline-flex", "inline-flex"),

    /**
     * Hides the element, removing it from the layout entirely.
     * CSS: {@code display: none}
     */
    HIDDEN("hidden", "none");

    /** Logger instance for debugging and auditing enum operations. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Display.class);

    /** The CSS property name constant for {@code display}. */
    private static final String PROPERTY_NAME = "display";

    /** Immutable map of all enum values by class name for efficient lookup. */
    private static final Map<String, Display> CLASS_NAME_MAP;

    static {
        Map<String, Display> tempMap = new HashMap<>();
        for (Display value : values()) {
            tempMap.put(value.className, value);
        }
        CLASS_NAME_MAP = Collections.unmodifiableMap(tempMap);
    }

    /** The CSS utility class name (e.g., "flex"). */
    private final String className;

    /** The CSS value (e.g., "flex"). */
    private final String value;

    /**
     * Constructs a {@code Display} enum constant with the given class name, property, and value.
     *
     * @param className the CSS utility class name (e.g., "flex")
     * @param value the CSS value (e.g., "flex")
     * @throws IllegalArgumentException if any parameter is null, empty, or invalid
     */
    Display(String className, String value) {
        validateInputs(className, value);
        this.className = className;
        this.value = value;
    }

    /**
     * Validates the inputs for an enum constant.
     *
     * @param className the class name to validate
     * @param value the value to validate
     * @throws IllegalArgumentException if any input is invalid
     */
    private void validateInputs(String className, String value) {
        if (className == null || className.trim().isEmpty()) {
            throw new IllegalArgumentException("Class name must not be null or empty");
        }
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Value must not be null or empty");
        }
        // Validate against common CSS display values (subset for this enum)
        if (!isValidDisplayValue(value)) {
            throw new IllegalArgumentException("Invalid display value: " + value);
        }
    }

    /**
     * Checks if a value is a valid {@code display} value for this enum’s scope.
     * Note: This enum focuses on flex-related and visibility values; full CSS spec includes more.
     *
     * @param value the value to check
     * @return true if valid, false otherwise
     */
    private boolean isValidDisplayValue(String value) {
        return switch (value) {
            case "flex", "inline-flex", "none" -> true;
            default -> false;
        };
    }

    /**
     * Returns the CSS utility class name associated with this enum value.
     *
     * @return the class name (e.g., "flex")
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the CSS value associated with this enum.
     *
     * @return the value (e.g., "flex")
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the CSS property name, which is always "display" for this enum.
     *
     * @return the property name
     */
    public static String getPropertyName() {
        return PROPERTY_NAME;
    }

    /**
     * Converts this enum value to a ph-css {@link CSSDeclaration} object.
     * Useful for direct integration with {@link scene.css.Style} or other ph-css components.
     *
     * @return a {@link CSSDeclaration} representing this display setting
     */
    public CSSDeclaration toCSSDeclaration() {
        return new CSSDeclaration(PROPERTY_NAME, CSSExpression.createSimple(value));
    }

    /**
     * Looks up a {@code Display} enum value by its class name.
     *
     * @param className the class name to look up (e.g., "flex")
     * @return the matching {@code Display} enum, or null if not found
     */
    public static Display fromClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            LOGGER.warn("Attempted to lookup Display with null or empty className");
            return null;
        }
        Display result = CLASS_NAME_MAP.get(className);
        if (result == null) {
            LOGGER.debug("No Display enum found for className: {}", className);
        }
        return result;
    }

    /**
     * Returns an immutable map of all {@code Display} values keyed by their class names.
     * Useful for generating CSS utilities or documentation in enterprise systems.
     *
     * @return an unmodifiable map of class names to enum values
     */
    public static Map<String, Display> getAllByClassName() {
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