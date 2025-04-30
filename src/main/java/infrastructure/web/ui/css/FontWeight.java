package infrastructure.web.ui.css;

import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration representing CSS {@code font-weight} property values, designed for production use in
 * large-scale enterprise applications like those at Amazon. This enum maps to CSS utility class names
 * and their corresponding {@code font-weight} values as defined in the CSS Fonts Module Level 4
 * specification. It provides robust input validation, logging, and integration with ph-css for generating
 * CSS declarations in single-threaded contexts, such as UI styling or static CSS generation.
 * <p>
 * The {@code font-weight} property specifies the weight (or boldness) of the font, using numeric values
 * that correspond to common weight names (e.g., "light", "normal", "bold"). This enum ensures type-safe
 * usage and enterprise-grade reliability with features tailored for maintainability and scalability.
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
 * FontWeight weight = FontWeight.BOLD;
 * System.out.println(weight.getClassName()); // "font-bold"
 * System.out.println(weight.getValue());     // "700"
 * System.out.println(weight.toCSSDeclaration().getAsCSSString()); // "font-weight: 700"
 * FontWeight lookedUp = FontWeight.fromClassName("font-normal");
 * System.out.println(lookedUp); // "[NORMAL] font-normal -> font-weight: 400"
 * </pre>
 *
 * @author Albert Beaupre
 * @version 1.0
 * @see <a href="https://www.w3.org/TR/css-fonts-4/#font-weight-prop">CSS Fonts Spec</a>
 * @since March 15th, 2025
 */
public enum FontWeight {
    /**
     * Sets the font weight to a light appearance.
     * CSS: {@code font-weight: 300}
     * <p>
     * Represents a lighter-than-normal weight, often used for subtle text emphasis.
     */
    LIGHT("font-light", "300"),

    /**
     * Sets the font weight to a normal appearance.
     * CSS: {@code font-weight: 400}
     * <p>
     * The default weight for most fonts, equivalent to the keyword "normal".
     */
    NORMAL("font-normal", "400"),

    /**
     * Sets the font weight to a bold appearance.
     * CSS: {@code font-weight: 700}
     * <p>
     * Represents a heavier weight, equivalent to the keyword "bold", used for strong emphasis.
     */
    BOLD("font-bold", "700");

    /**
     * Logger instance for debugging and auditing enum operations.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FontWeight.class);

    /**
     * The CSS property name constant for {@code font-weight}.
     */
    private static final String PROPERTY_NAME = "font-weight";

    /**
     * Immutable map of all enum values by class name for efficient lookup.
     */
    private static final Map<String, FontWeight> CLASS_NAME_MAP;

    static {
        Map<String, FontWeight> tempMap = new HashMap<>();
        for (FontWeight value : values()) {
            tempMap.put(value.className, value);
        }
        CLASS_NAME_MAP = Collections.unmodifiableMap(tempMap);
        LOGGER.debug("Initialized CLASS_NAME_MAP with {} FontWeight enum values", CLASS_NAME_MAP.size());
    }

    /**
     * The CSS utility class name (e.g., "font-light").
     */
    private final String className;

    /**
     * The CSS value (e.g., "300").
     */
    private final String value;

    /**
     * Constructs a {@code FontWeight} enum constant with the given class name, property, and value.
     * Validates inputs to ensure they conform to expected CSS standards for the {@code font-weight} property.
     *
     * @param className the CSS utility class name (e.g., "font-light")
     * @param value     the CSS value (e.g., "300")
     * @throws IllegalArgumentException if any parameter is null, empty, or invalid
     */
    FontWeight(String className, String value) {
        validateInputs(className, value);
        this.className = className;
        this.value = value;
    }

    /**
     * Validates the inputs for an enum constant to ensure they are valid for the {@code font-weight} property.
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
        // Validate against common font-weight numeric values (expandable for full range if needed)
        if (!isValidFontWeightValue(value)) {
            throw new IllegalArgumentException("Invalid font-weight value: " + value);
        }
    }

    /**
     * Checks if a value is a valid {@code font-weight} value for this enum’s scope.
     * This enum focuses on common numeric values (300, 400, 700); the full CSS spec allows 100-900 and keywords.
     *
     * @param value the value to check
     * @return true if valid, false otherwise
     */
    private boolean isValidFontWeightValue(String value) {
        return switch (value) {
            case "300", "400", "700" -> true;
            default -> false;
        };
    }

    /**
     * Returns the CSS utility class name associated with this enum value.
     *
     * @return the class name (e.g., "font-light")
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the CSS value associated with this enum.
     *
     * @return the value (e.g., "300")
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the CSS property name, which is always "font-weight" for this enum.
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
     * @return a {@link CSSDeclaration} representing this font-weight setting
     */
    public CSSDeclaration toCSSDeclaration() {
        return new CSSDeclaration(PROPERTY_NAME, CSSExpression.createSimple(value));
    }

    /**
     * Looks up a {@code FontWeight} enum value by its class name.
     * Returns null if the class name is not found, logging a debug message to aid troubleshooting.
     *
     * @param className the class name to look up (e.g., "font-bold")
     * @return the matching {@code FontWeight} enum, or null if not found
     */
    public static FontWeight fromClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            LOGGER.warn("Attempted to lookup FontWeight with null or empty className");
            return null;
        }
        FontWeight result = CLASS_NAME_MAP.get(className);
        if (result == null) {
            LOGGER.debug("No FontWeight enum found for className: {}", className);
        }
        return result;
    }

    /**
     * Returns an immutable map of all {@code FontWeight} values keyed by their class names.
     * This is useful for generating CSS utilities, documentation, or validation tables in enterprise systems.
     *
     * @return an unmodifiable map of class names to enum values
     */
    public static Map<String, FontWeight> getAllByClassName() {
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