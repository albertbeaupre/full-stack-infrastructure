package infrastructure.web.ui.css;

import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration representing CSS {@code visibility} property values, designed for production use in
 * large-scale enterprise applications like those at Amazon. This enum maps to CSS utility class names
 * and their corresponding {@code visibility} values as defined in the CSS 2.1 specification. It provides
 * robust validation, logging, and integration with ph-css for generating CSS declarations.
 * <p>
 * The {@code visibility} property controls whether an element is visible or hidden without affecting
 * its layout (unlike {@code display: none}). This enum is intended for single-threaded contexts, such
 * as UI styling or static CSS generation.
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
 * Visibility visibility = Visibility.VISIBLE;
 * System.out.println(visibility.getClassName()); // "visible"
 * System.out.println(visibility.toCSSDeclaration()); // CSSDeclaration{visibility: visible}
 * </pre>
 *
 * @author Albert Beaupre
 * @version 1.0
 * @see <a href="https://www.w3.org/TR/CSS21/visufx.html#visibility">CSS 2.1 Spec</a>
 * @since March 15th, 2025
 */
public enum Visibility {
    /**
     * Makes the element fully visible.
     * CSS: {@code visibility: visible}
     */
    VISIBLE("visible", "visible"),

    /**
     * Hides the element while preserving its space in the layout.
     * CSS: {@code visibility: hidden}
     */
    INVISIBLE("invisible", "hidden");

    /**
     * Logger instance for debugging and auditing enum operations.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Visibility.class);

    /**
     * The CSS property name constant for {@code visibility}.
     */
    private static final String PROPERTY_NAME = "visibility";

    /**
     * Immutable map of all enum values by class name for efficient lookup.
     */
    private static final Map<String, Visibility> CLASS_NAME_MAP;

    static {
        Map<String, Visibility> tempMap = new HashMap<>();
        for (Visibility value : values()) {
            tempMap.put(value.className, value);
        }
        CLASS_NAME_MAP = Collections.unmodifiableMap(tempMap);
    }

    /**
     * The CSS utility class name (e.g., "visible").
     */
    private final String className;

    /**
     * The CSS value (e.g., "visible").
     */
    private final String value;

    /**
     * Constructs a {@code Visibility} enum constant with the given class name and value.
     *
     * @param className the CSS utility class name (e.g., "visible")
     * @param value     the CSS value (e.g., "visible")
     * @throws IllegalArgumentException if any parameter is null, empty, or invalid
     */
    Visibility(String className, String value) {
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
        if (!isValidVisibilityValue(value)) {
            throw new IllegalArgumentException("Invalid visibility value: " + value);
        }
    }

    /**
     * Checks if a value is a valid {@code visibility} value per CSS spec.
     *
     * @param value the value to check
     * @return true if valid, false otherwise
     */
    private boolean isValidVisibilityValue(String value) {
        return switch (value) {
            case "visible", "hidden" -> true;
            default -> false;
        };
    }

    /**
     * Returns the CSS utility class name associated with this enum value.
     *
     * @return the class name (e.g., "visible")
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the CSS value associated with this enum.
     *
     * @return the value (e.g., "visible")
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the CSS property name, which is always "visibility" for this enum.
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
     * @return a {@link CSSDeclaration} representing this visibility setting
     */
    public CSSDeclaration toCSSDeclaration() {
        return new CSSDeclaration(PROPERTY_NAME, CSSExpression.createSimple(value));
    }

    /**
     * Looks up a {@code Visibility} enum value by its class name.
     *
     * @param className the class name to look up (e.g., "visible")
     * @return the matching {@code Visibility} enum, or null if not found
     */
    public static Visibility fromClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            LOGGER.warn("Attempted to lookup Visibility with null or empty className");
            return null;
        }
        Visibility result = CLASS_NAME_MAP.get(className);
        if (result == null) {
            LOGGER.debug("No Visibility enum found for className: {}", className);
        }
        return result;
    }

    /**
     * Returns an immutable map of all {@code Visibility} values keyed by their class names.
     * Useful for generating CSS utilities or documentation in enterprise systems.
     *
     * @return an unmodifiable map of class names to enum values
     */
    public static Map<String, Visibility> getAllByClassName() {
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