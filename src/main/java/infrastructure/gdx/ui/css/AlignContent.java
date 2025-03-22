package infrastructure.gdx.ui.css;

import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration representing CSS {@code align-content} property values, designed for production use in
 * large-scale enterprise applications like those at Amazon. This enum maps to CSS utility class names
 * and their corresponding {@code align-content} values as defined in the CSS Flexible Box Layout Module
 * Level 1 specification. It provides robust validation, logging, and integration with ph-css for generating
 * CSS declarations.
 * <p>
 * The {@code align-content} property aligns a flex container’s lines when there’s extra space in the
 * cross-axis, similar to how {@code justify-content} aligns items along the main-axis. This enum is
 * intended for single-threaded contexts, such as UI styling or static CSS generation.
 * <p>
 * Key features:
 * <ul>
 *     <li>Input validation for class names, properties, and values</li>
 *     <li>Logging for enum instantiation and usage</li>
 *     <li>Direct conversion to ph-css {@link CSSDeclaration}</li>
 *     <li>Utility methods for lookup and mapping</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * AlignContent align = AlignContent.CENTER;
 * System.out.println(align.getClassName()); // "content-center"
 * System.out.println(align.toCSSDeclaration()); // CSSDeclaration{align-content: center}
 * </pre>
 *
 * @author Albert Beaupre
 * @version 1.0
 * @see <a href="https://www.w3.org/TR/css-flexbox-1/#align-content-property">CSS Flexbox Spec</a>
 * @since March 15th, 2025
 */
public enum AlignContent {
    /**
     * Aligns flex lines to the start of the cross-axis (e.g., top or left in a column layout).
     * CSS: {@code align-content: flex-start}
     */
    START("content-start", "flex-start"),

    /**
     * Aligns flex lines to the end of the cross-axis (e.g., bottom or right in a column layout).
     * CSS: {@code align-content: flex-end}
     */
    END("content-end", "flex-end"),

    /**
     * Centers flex lines along the cross-axis.
     * CSS: {@code align-content: center}
     */
    CENTER("content-center", "center"),

    /**
     * Distributes flex lines with equal space between them, no space at the start or end.
     * CSS: {@code align-content: space-between}
     */
    BETWEEN("content-between", "space-between"),

    /**
     * Distributes flex lines with equal space around them, including at the start and end.
     * CSS: {@code align-content: space-around}
     */
    AROUND("content-around", "space-around"),

    /**
     * Stretches flex lines to fill the cross-axis (default behavior).
     * CSS: {@code align-content: stretch}
     */
    STRETCH("content-stretch", "stretch");

    /**
     * Logger instance for debugging and auditing enum operations.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AlignContent.class);

    /**
     * The CSS property name constant for {@code align-content}.
     */
    private static final String PROPERTY_NAME = "align-content";

    /**
     * Immutable map of all enum values by class name for efficient lookup.
     */
    private static final Map<String, AlignContent> CLASS_NAME_MAP;

    static {
        Map<String, AlignContent> tempMap = new HashMap<>();
        for (AlignContent value : values()) {
            tempMap.put(value.className, value);
        }
        CLASS_NAME_MAP = Collections.unmodifiableMap(tempMap);
    }

    /**
     * The CSS utility class name (e.g., "content-start").
     */
    private final String className;


    /**
     * The CSS value (e.g., "flex-start").
     */
    private final String value;

    /**
     * Constructs an {@code AlignContent} enum constant with the given class name, property, and value.
     *
     * @param className the CSS utility class name (e.g., "content-start")
     * @param value     the CSS value (e.g., "flex-start")
     * @throws IllegalArgumentException if any parameter is null, empty, or invalid
     */
    AlignContent(String className, String value) {
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
        // Basic CSS value validation (could be expanded)
        if (!isValidAlignContentValue(value)) {
            throw new IllegalArgumentException("Invalid align-content value: " + value);
        }
    }

    /**
     * Checks if a value is a valid {@code align-content} value per CSS spec.
     *
     * @param value the value to check
     * @return true if valid, false otherwise
     */
    private boolean isValidAlignContentValue(String value) {
        return switch (value) {
            case "flex-start", "flex-end", "center", "space-between", "space-around", "stretch" -> true;
            default -> false;
        };
    }

    /**
     * Returns the CSS utility class name associated with this enum value.
     *
     * @return the class name (e.g., "content-start")
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
     * Returns the CSS property name, which is always "align-content" for this enum.
     *
     * @return the property name
     */
    public static String getPropertyName() {
        return PROPERTY_NAME;
    }

    /**
     * Converts this enum value to a ph-css {@link CSSDeclaration} object.
     * Useful for direct integration with {@link infrastructure.gdx.ui.css.Style} or other ph-css components.
     *
     * @return a {@link CSSDeclaration} representing this align-content setting
     */
    public CSSDeclaration toCSSDeclaration() {
        return new CSSDeclaration(PROPERTY_NAME, CSSExpression.createSimple(value));
    }

    /**
     * Looks up an {@code AlignContent} enum value by its class name.
     *
     * @param className the class name to look up (e.g., "content-center")
     * @return the matching {@code AlignContent} enum, or null if not found
     */
    public static AlignContent fromClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            LOGGER.warn("Attempted to lookup AlignContent with null or empty className");
            return null;
        }
        AlignContent result = CLASS_NAME_MAP.get(className);
        if (result == null) {
            LOGGER.debug("No AlignContent enum found for className: {}", className);
        }
        return result;
    }

    /**
     * Returns an immutable map of all {@code AlignContent} values keyed by their class names.
     * Useful for generating CSS utilities or documentation in enterprise systems.
     *
     * @return an unmodifiable map of class names to enum values
     */
    public static Map<String, AlignContent> getAllByClassName() {
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