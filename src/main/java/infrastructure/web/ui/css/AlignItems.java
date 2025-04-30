package infrastructure.web.ui.css;

import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration representing CSS {@code align-items} property values, designed for production use in
 * large-scale enterprise applications like those at Amazon. This enum maps to CSS utility class names
 * and their corresponding {@code align-items} values as defined in the CSS Flexible Box Layout Module
 * Level 1 specification. It provides robust validation, logging, and integration with ph-css for generating
 * CSS declarations in single-threaded contexts, such as UI styling or static CSS generation.
 * <p>
 * The {@code align-items} property aligns flex items along the cross-axis of a flex container, affecting
 * their positioning perpendicular to the main axis. This enum ensures type-safe usage and enterprise-grade
 * reliability.
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
 * AlignItems align = AlignItems.CENTER;
 * System.out.println(align.getClassName()); // "items-center"
 * System.out.println(align.toCSSDeclaration()); // CSSDeclaration{align-items: center}
 * </pre>
 *
 * @author Albert Beaupre
 * @version 1.0
 * @since March 15th, 2025
 * @see <a href="https://www.w3.org/TR/css-flexbox-1/#align-items-property">CSS Flexbox Spec</a>
 */
public enum AlignItems {
    /**
     * Aligns flex items to the start of the cross-axis (e.g., top in a row layout, left in a column layout).
     * CSS: {@code align-items: flex-start}
     */
    FLEX_START("items-start", "flex-start"),

    /**
     * Aligns flex items to the end of the cross-axis (e.g., bottom in a row layout, right in a column layout).
     * CSS: {@code align-items: flex-end}
     */
    FLEX_END("items-end", "flex-end"), // Fixed typo from "align header-items"

    /**
     * Centers flex items along the cross-axis.
     * CSS: {@code align-items: center}
     */
    CENTER("items-center", "center"),

    /**
     * Aligns flex items along their baseline (useful for text alignment within items).
     * CSS: {@code align-items: baseline}
     */
    BASELINE("items-baseline", "baseline"),

    /**
     * Stretches flex items to fill the cross-axis (default behavior).
     * CSS: {@code align-items: stretch}
     */
    STRETCH("items-stretch", "stretch");

    /** Logger instance for debugging and auditing enum operations. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AlignItems.class);

    /** The CSS property name constant for {@code align-items}. */
    private static final String PROPERTY_NAME = "align-items";

    /** Immutable map of all enum values by class name for efficient lookup. */
    private static final Map<String, AlignItems> CLASS_NAME_MAP;

    static {
        Map<String, AlignItems> tempMap = new HashMap<>();
        for (AlignItems value : values()) {
            tempMap.put(value.className, value);
        }
        CLASS_NAME_MAP = Collections.unmodifiableMap(tempMap);
    }

    /** The CSS utility class name (e.g., "items-start"). */
    private final String className;

    /** The CSS value (e.g., "flex-start"). */
    private final String value;

    /**
     * Constructs an {@code AlignItems} enum constant with the given class name, property, and value.
     *
     * @param className the CSS utility class name (e.g., "items-start")
     * @param value the CSS value (e.g., "flex-start")
     * @throws IllegalArgumentException if any parameter is null, empty, or invalid
     */
    AlignItems(String className, String value) {
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
        // Validate against CSS spec for align-items
        if (!isValidAlignItemsValue(value)) {
            throw new IllegalArgumentException("Invalid align-items value: " + value);
        }
    }

    /**
     * Checks if a value is a valid {@code align-items} value per CSS spec.
     *
     * @param value the value to check
     * @return true if valid, false otherwise
     */
    private boolean isValidAlignItemsValue(String value) {
        return switch (value) {
            case "flex-start", "flex-end", "center", "baseline", "stretch" -> true;
            default -> false;
        };
    }

    /**
     * Returns the CSS utility class name associated with this enum value.
     *
     * @return the class name (e.g., "items-start")
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
     * Returns the CSS property name, which is always "align-items" for this enum.
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
     * @return a {@link CSSDeclaration} representing this align-items setting
     */
    public CSSDeclaration toCSSDeclaration() {
        return new CSSDeclaration(PROPERTY_NAME, CSSExpression.createSimple(value));
    }

    /**
     * Looks up an {@code AlignItems} enum value by its class name.
     *
     * @param className the class name to look up (e.g., "items-center")
     * @return the matching {@code AlignItems} enum, or null if not found
     */
    public static AlignItems fromClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            LOGGER.warn("Attempted to lookup AlignItems with null or empty className");
            return null;
        }
        AlignItems result = CLASS_NAME_MAP.get(className);
        if (result == null) {
            LOGGER.debug("No AlignItems enum found for className: {}", className);
        }
        return result;
    }

    /**
     * Returns an immutable map of all {@code AlignItems} values keyed by their class names.
     * Useful for generating CSS utilities or documentation in enterprise systems.
     *
     * @return an unmodifiable map of class names to enum values
     */
    public static Map<String, AlignItems> getAllByClassName() {
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