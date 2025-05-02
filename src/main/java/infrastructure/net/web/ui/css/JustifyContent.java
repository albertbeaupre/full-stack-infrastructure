package infrastructure.net.web.ui.css;

import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration representing CSS {@code justify-content} property values, designed for production use in
 * large-scale enterprise applications like those at Amazon. This enum maps to CSS utility class names
 * and their corresponding {@code justify-content} values as defined in the CSS Flexible Box Layout Module
 * Level 1 specification. It provides robust validation, logging, and integration with ph-css for generating
 * CSS declarations.
 * <p>
 * The {@code justify-content} property aligns flex items along the main axis of a flex container when there
 * is extra space, controlling their distribution and positioning. This enum is intended for single-threaded
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
 * JustifyContent justify = JustifyContent.CENTER;
 * System.out.println(justify.getClassName()); // "justify-center"
 * System.out.println(justify.toCSSDeclaration()); // CSSDeclaration{justify-content: center}
 * </pre>
 *
 * @author Albert Beaupre
 * @version 1.0
 * @see <a href="https://www.w3.org/TR/css-flexbox-1/#justify-content-property">CSS Flexbox Spec</a>
 * @since March 15th, 2025
 */
public enum JustifyContent {
    /**
     * Aligns flex items to the start of the main axis (e.g., left in a row, top in a column).
     * CSS: {@code justify-content: flex-start}
     */
    FLEX_START("justify-start", "flex-start"),

    /**
     * Aligns flex items to the end of the main axis (e.g., right in a row, bottom in a column).
     * CSS: {@code justify-content: flex-end}
     */
    FLEX_END("justify-end", "flex-end"),

    /**
     * Centers flex items along the main axis.
     * CSS: {@code justify-content: center}
     */
    CENTER("justify-center", "center"),

    /**
     * Distributes flex items with equal space between them, no space at the start or end.
     * CSS: {@code justify-content: space-between}
     */
    SPACE_BETWEEN("justify-between", "space-between"),

    /**
     * Distributes flex items with equal space around them, including at the start and end.
     * CSS: {@code justify-content: space-around}
     */
    SPACE_AROUND("justify-around", "space-around"),

    /**
     * Distributes flex items with equal space between and around them, including full-size spaces at the start and end.
     * CSS: {@code justify-content: space-evenly}
     */
    EVENLY("justify-evenly", "space-evenly");

    /**
     * Logger instance for debugging and auditing enum operations.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JustifyContent.class);

    /**
     * The CSS property name constant for {@code justify-content}.
     */
    private static final String PROPERTY_NAME = "justify-content";

    /**
     * Immutable map of all enum values by class name for efficient lookup.
     */
    private static final Map<String, JustifyContent> CLASS_NAME_MAP;

    static {
        Map<String, JustifyContent> tempMap = new HashMap<>();
        for (JustifyContent value : values()) {
            tempMap.put(value.className, value);
        }
        CLASS_NAME_MAP = Collections.unmodifiableMap(tempMap);
    }

    /**
     * The CSS utility class name (e.g., "justify-start").
     */
    private final String className;

    /**
     * The CSS value (e.g., "flex-start").
     */
    private final String value;

    /**
     * Constructs a {@code JustifyContent} enum constant with the given class name and value.
     *
     * @param className the CSS utility class name (e.g., "justify-start")
     * @param value     the CSS value (e.g., "flex-start")
     * @throws IllegalArgumentException if any parameter is null, empty, or invalid
     */
    JustifyContent(String className, String value) {
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
        if (!isValidJustifyContentValue(value)) {
            throw new IllegalArgumentException("Invalid justify-content value: " + value);
        }
    }

    /**
     * Checks if a value is a valid {@code justify-content} value per CSS spec.
     *
     * @param value the value to check
     * @return true if valid, false otherwise
     */
    private boolean isValidJustifyContentValue(String value) {
        return switch (value) {
            case "flex-start", "flex-end", "center", "space-between", "space-around", "space-evenly" -> true;
            default -> false;
        };
    }

    /**
     * Returns the CSS utility class name associated with this enum value.
     *
     * @return the class name (e.g., "justify-start")
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the CSS value associated with this enum.
     *
     * @return the value (e.g., "flex-start")
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the CSS property name, which is always "justify-content" for this enum.
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
     * @return a {@link CSSDeclaration} representing this justify-content setting
     */
    public CSSDeclaration toCSSDeclaration() {
        return new CSSDeclaration(PROPERTY_NAME, CSSExpression.createSimple(value));
    }

    /**
     * Looks up a {@code JustifyContent} enum value by its class name.
     *
     * @param className the class name to look up (e.g., "justify-center")
     * @return the matching {@code JustifyContent} enum, or null if not found
     */
    public static JustifyContent fromClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            LOGGER.warn("Attempted to lookup JustifyContent with null or empty className");
            return null;
        }
        JustifyContent result = CLASS_NAME_MAP.get(className);
        if (result == null) {
            LOGGER.debug("No JustifyContent enum found for className: {}", className);
        }
        return result;
    }

    /**
     * Returns an immutable map of all {@code JustifyContent} values keyed by their class names.
     * Useful for generating CSS utilities or documentation in enterprise systems.
     *
     * @return an unmodifiable map of class names to enum values
     */
    public static Map<String, JustifyContent> getAllByClassName() {
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