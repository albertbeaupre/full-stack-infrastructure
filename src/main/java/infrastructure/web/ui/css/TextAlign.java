package infrastructure.web.ui.css;

import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration representing CSS {@code text-align} property values, designed for production use in
 * large-scale enterprise applications like those at Amazon. This enum maps to CSS utility class names
 * and their corresponding {@code text-align} values as defined in the CSS Text Module Level 3
 * specification. It provides robust validation, logging, and integration with ph-css for generating
 * CSS declarations.
 * <p>
 * The {@code text-align} property specifies the horizontal alignment of text within an element. This enum
 * is intended for single-threaded contexts, such as UI styling or static CSS generation.
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
 * TextAlign align = TextAlign.CENTER;
 * System.out.println(align.getClassName()); // "text-center"
 * System.out.println(align.toCSSDeclaration()); // CSSDeclaration{text-align: center}
 * </pre>
 *
 * @author Albert Beaupre
 * @version 1.0
 * @see <a href="https://www.w3.org/TR/css-text-3/#text-align-property">CSS Text Spec</a>
 * @since March 15th, 2025
 */
public enum TextAlign {
    /**
     * Aligns text to the left edge of the element.
     * CSS: {@code text-align: left}
     */
    LEFT("text-left", "left"),

    /**
     * Centers text horizontally within the element.
     * CSS: {@code text-align: center}
     */
    CENTER("text-center", "center"),

    /**
     * Aligns text to the right edge of the element.
     * CSS: {@code text-align: right}
     */
    RIGHT("text-right", "right"),

    /**
     * Justifies text, spreading it evenly between the left and right edges.
     * CSS: {@code text-align: justify}
     */
    JUSTIFY("text-justify", "justify");

    /**
     * Logger instance for debugging and auditing enum operations.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TextAlign.class);

    /**
     * The CSS property name constant for {@code text-align}.
     */
    private static final String PROPERTY_NAME = "text-align";

    /**
     * Immutable map of all enum values by class name for efficient lookup.
     */
    private static final Map<String, TextAlign> CLASS_NAME_MAP;

    static {
        Map<String, TextAlign> tempMap = new HashMap<>();
        for (TextAlign value : values()) {
            tempMap.put(value.className, value);
        }
        CLASS_NAME_MAP = Collections.unmodifiableMap(tempMap);
    }

    /**
     * The CSS utility class name (e.g., "text-left").
     */
    private final String className;

    /**
     * The CSS value (e.g., "left").
     */
    private final String value;

    /**
     * Constructs a {@code TextAlign} enum constant with the given class name and value.
     *
     * @param className the CSS utility class name (e.g., "text-left")
     * @param value     the CSS value (e.g., "left")
     * @throws IllegalArgumentException if any parameter is null, empty, or invalid
     */
    TextAlign(String className, String value) {
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
        if (!isValidTextAlignValue(value)) {
            throw new IllegalArgumentException("Invalid text-align value: " + value);
        }
    }

    /**
     * Checks if a value is a valid {@code text-align} value per CSS spec.
     *
     * @param value the value to check
     * @return true if valid, false otherwise
     */
    private boolean isValidTextAlignValue(String value) {
        return switch (value) {
            case "left", "center", "right", "justify" -> true;
            default -> false;
        };
    }

    /**
     * Returns the CSS utility class name associated with this enum value.
     *
     * @return the class name (e.g., "text-left")
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the CSS value associated with this enum.
     *
     * @return the value (e.g., "left")
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the CSS property name, which is always "text-align" for this enum.
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
     * @return a {@link CSSDeclaration} representing this text-align setting
     */
    public CSSDeclaration toCSSDeclaration() {
        return new CSSDeclaration(PROPERTY_NAME, CSSExpression.createSimple(value));
    }

    /**
     * Looks up a {@code TextAlign} enum value by its class name.
     *
     * @param className the class name to look up (e.g., "text-center")
     * @return the matching {@code TextAlign} enum, or null if not found
     */
    public static TextAlign fromClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            LOGGER.warn("Attempted to lookup TextAlign with null or empty className");
            return null;
        }
        TextAlign result = CLASS_NAME_MAP.get(className);
        if (result == null) {
            LOGGER.debug("No TextAlign enum found for className: {}", className);
        }
        return result;
    }

    /**
     * Returns an immutable map of all {@code TextAlign} values keyed by their class names.
     * Useful for generating CSS utilities or documentation in enterprise systems.
     *
     * @return an unmodifiable map of class names to enum values
     */
    public static Map<String, TextAlign> getAllByClassName() {
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