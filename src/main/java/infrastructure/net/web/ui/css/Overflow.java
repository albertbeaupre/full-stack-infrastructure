package infrastructure.net.web.ui.css;

import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration representing CSS {@code overflow} property values, designed for production use in
 * large-scale enterprise applications like those at Amazon. This enum maps to CSS utility class names
 * and their corresponding {@code overflow} values as defined in the CSS Overflow Module Level 3
 * specification. It provides robust validation, logging, and integration with ph-css for generating
 * CSS declarations.
 * <p>
 * The {@code overflow} property specifies how content that overflows an element’s box is handled,
 * controlling clipping, scrolling, or visibility. This enum is intended for single-threaded contexts,
 * such as UI styling or static CSS generation.
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
 * Overflow overflow = Overflow.AUTO;
 * System.out.println(overflow.getClassName()); // "overflow-auto"
 * System.out.println(overflow.toCSSDeclaration()); // CSSDeclaration{overflow: auto}
 * </pre>
 *
 * @author Albert Beaupre
 * @version 1.0
 * @see <a href="https://www.w3.org/TR/css-overflow-3/#overflow-property">CSS Overflow Spec</a>
 * @since March 15th, 2025
 */
public enum Overflow {
    /**
     * Adds scrollbars only when content overflows, allowing scrolling to view hidden content.
     * CSS: {@code overflow: auto}
     */
    AUTO("overflow-auto", "auto"),

    /**
     * Clips overflowing content, hiding it without providing scrollbars.
     * CSS: {@code overflow: hidden}
     */
    HIDDEN("overflow-hidden", "hidden"),

    /**
     * Always adds scrollbars, enabling scrolling even if content fits within the element.
     * CSS: {@code overflow: scroll}
     */
    SCROLL("overflow-scroll", "scroll");

    /**
     * Logger instance for debugging and auditing enum operations.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Overflow.class);

    /**
     * The CSS property name constant for {@code overflow}.
     */
    private static final String PROPERTY_NAME = "overflow";

    /**
     * Immutable map of all enum values by class name for efficient lookup.
     */
    private static final Map<String, Overflow> CLASS_NAME_MAP;

    static {
        Map<String, Overflow> tempMap = new HashMap<>();
        for (Overflow value : values()) {
            tempMap.put(value.className, value);
        }
        CLASS_NAME_MAP = Collections.unmodifiableMap(tempMap);
    }

    /**
     * The CSS utility class name (e.g., "overflow-auto").
     */
    private final String className;

    /**
     * The CSS value (e.g., "auto").
     */
    private final String value;

    /**
     * Constructs an {@code Overflow} enum constant with the given class name and value.
     *
     * @param className the CSS utility class name (e.g., "overflow-auto")
     * @param value     the CSS value (e.g., "auto")
     * @throws IllegalArgumentException if any parameter is null, empty, or invalid
     */
    Overflow(String className, String value) {
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
        if (!isValidOverflowValue(value)) {
            throw new IllegalArgumentException("Invalid overflow value: " + value);
        }
    }

    /**
     * Checks if a value is a valid {@code overflow} value per CSS spec.
     *
     * @param value the value to check
     * @return true if valid, false otherwise
     */
    private boolean isValidOverflowValue(String value) {
        return switch (value) {
            case "auto", "hidden", "scroll" -> true;
            default -> false;
        };
    }

    /**
     * Returns the CSS utility class name associated with this enum value.
     *
     * @return the class name (e.g., "overflow-auto")
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the CSS value associated with this enum.
     *
     * @return the value (e.g., "auto")
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the CSS property name, which is always "overflow" for this enum.
     *
     * @return the property name
     */
    public static String getPropertyName() {
        return PROPERTY_NAME;
    }

    /**
     * Converts this enum value to a ph-css {@link CSSDeclaration} object.
     * Useful for direct integration with {@link Style} or other ph-css components.
     *
     * @return a {@link CSSDeclaration} representing this overflow setting
     */
    public CSSDeclaration toCSSDeclaration() {
        return new CSSDeclaration(PROPERTY_NAME, CSSExpression.createSimple(value));
    }

    /**
     * Looks up an {@code Overflow} enum value by its class name.
     *
     * @param className the class name to look up (e.g., "overflow-auto")
     * @return the matching {@code Overflow} enum, or null if not found
     */
    public static Overflow fromClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            LOGGER.warn("Attempted to lookup Overflow with null or empty className");
            return null;
        }
        Overflow result = CLASS_NAME_MAP.get(className);
        if (result == null) {
            LOGGER.debug("No Overflow enum found for className: {}", className);
        }
        return result;
    }

    /**
     * Returns an immutable map of all {@code Overflow} values keyed by their class names.
     * Useful for generating CSS utilities or documentation in enterprise systems.
     *
     * @return an unmodifiable map of class names to enum values
     */
    public static Map<String, Overflow> getAllByClassName() {
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