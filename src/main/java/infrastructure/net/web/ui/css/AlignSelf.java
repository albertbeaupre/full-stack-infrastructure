package infrastructure.net.web.ui.css;

import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration representing CSS {@code align-self} property values, designed for production use in
 * large-scale enterprise applications like those at Amazon. This enum maps to CSS utility class names
 * and their corresponding {@code align-self} values as defined in the CSS Flexible Box Layout Module
 * Level 1 specification. It provides robust validation, logging, and integration with ph-css for generating
 * CSS declarations in single-threaded contexts, such as UI styling or static CSS generation.
 * <p>
 * The {@code align-self} property overrides a flex container's {@code align-items} setting for an individual
 * flex item, controlling its alignment along the cross-axis. This enum ensures type-safe usage and
 * enterprise-grade reliability.
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
 * AlignSelf align = AlignSelf.CENTER;
 * System.out.println(align.getClassName()); // "self-center"
 * System.out.println(align.toCSSDeclaration()); // CSSDeclaration{align-self: center}
 * </pre>
 *
 * @author Albert Beaupre
 * @version 1.0
 * @see <a href="https://www.w3.org/TR/css-flexbox-1/#align-self-property">CSS Flexbox Spec</a>
 * @since March 15th, 2025
 */
public enum AlignSelf {
    /**
     * Aligns the flex item to the start of the cross-axis (e.g., top in a row layout, left in a column layout).
     * CSS: {@code align-self: flex-start}
     */
    START("self-start", "flex-start"),

    /**
     * Aligns the flex item to the end of the cross-axis (e.g., bottom in a row layout, right in a column layout).
     * CSS: {@code align-self: flex-end}
     */
    END("self-end", "flex-end"),

    /**
     * Centers the flex item along the cross-axis.
     * CSS: {@code align-self: center}
     */
    CENTER("self-center", "center"),

    /**
     * Aligns the flex item along its baseline (useful for text alignment).
     * CSS: {@code align-self: baseline}
     */
    BASELINE("self-baseline", "baseline"),

    /**
     * Stretches the flex item to fill the cross-axis (default behavior if not overridden).
     * CSS: {@code align-self: stretch}
     */
    STRETCH("self-stretch", "stretch");

    /**
     * Logger instance for debugging and auditing enum operations.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AlignSelf.class);

    /**
     * The CSS property name constant for {@code align-self}.
     */
    private static final String PROPERTY_NAME = "align-self";

    /**
     * Immutable map of all enum values by class name for efficient lookup.
     */
    private static final Map<String, AlignSelf> CLASS_NAME_MAP;

    static {
        Map<String, AlignSelf> tempMap = new HashMap<>();
        for (AlignSelf value : values()) {
            tempMap.put(value.className, value);
        }
        CLASS_NAME_MAP = Collections.unmodifiableMap(tempMap);
    }

    /**
     * The CSS utility class name (e.g., "self-start").
     */
    private final String className;

    /**
     * The CSS value (e.g., "flex-start").
     */
    private final String value;

    /**
     * Constructs an {@code AlignSelf} enum constant with the given class name, property, and value.
     *
     * @param className the CSS utility class name (e.g., "self-start")
     * @param value     the CSS value (e.g., "flex-start")
     * @throws IllegalArgumentException if any parameter is null, empty, or invalid
     */
    AlignSelf(String className, String value) {
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
        // Validate against CSS spec for align-self
        if (!isValidAlignSelfValue(value)) {
            throw new IllegalArgumentException("Invalid align-self value: " + value);
        }
    }

    /**
     * Checks if a value is a valid {@code align-self} value per CSS spec.
     *
     * @param value the value to check
     * @return true if valid, false otherwise
     */
    private boolean isValidAlignSelfValue(String value) {
        return switch (value) {
            case "flex-start", "flex-end", "center", "baseline", "stretch" -> true;
            default -> false;
        };
    }

    /**
     * Returns the CSS utility class name associated with this enum value.
     *
     * @return the class name (e.g., "self-start")
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
     * Returns the CSS property name, which is always "align-self" for this enum.
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
     * @return a {@link CSSDeclaration} representing this align-self setting
     */
    public CSSDeclaration toCSSDeclaration() {
        return new CSSDeclaration(PROPERTY_NAME, CSSExpression.createSimple(value));
    }

    /**
     * Looks up an {@code AlignSelf} enum value by its class name.
     *
     * @param className the class name to look up (e.g., "self-center")
     * @return the matching {@code AlignSelf} enum, or null if not found
     */
    public static AlignSelf fromClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            LOGGER.warn("Attempted to lookup AlignSelf with null or empty className");
            return null;
        }
        AlignSelf result = CLASS_NAME_MAP.get(className);
        if (result == null) {
            LOGGER.debug("No AlignSelf enum found for className: {}", className);
        }
        return result;
    }

    /**
     * Returns an immutable map of all {@code AlignSelf} values keyed by their class names.
     * Useful for generating CSS utilities or documentation in enterprise systems.
     *
     * @return an unmodifiable map of class names to enum values
     */
    public static Map<String, AlignSelf> getAllByClassName() {
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