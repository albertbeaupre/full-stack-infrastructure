package infrastructure.gdx.ui.css;

import com.helger.css.ECSSVersion;
import com.helger.css.decl.*;
import com.helger.css.writer.CSSWriter;
import com.helger.css.writer.CSSWriterSettings;
import infrastructure.event.EventListener;
import infrastructure.event.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A production-ready utility class for building CSS styles using ph-css 7.0.4, designed for single-threaded
 * enterprise applications such as those at Amazon-scale businesses. This class generates CSS stylesheet rules
 * tied to a specific selector, supporting flexbox, alignment, spacing, sizing, typography, visibility,
 * positioning, and miscellaneous properties. It provides robust input validation, logging, immutability
 * options, and extensibility for large-scale styling needs.
 * <p>
 * Key features include:
 * <ul>
 *     <li>Comprehensive input validation to prevent invalid CSS</li>
 *     <li>Logging for debugging and auditing</li>
 *     <li>Immutable style option via {@link #lock()}</li>
 *     <li>Optimized CSS output with minification support</li>
 *     <li>File export capability for integration with build systems</li>
 *     <li>Extensibility for custom properties and media queries</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * Style style = new Style(".my-component")
 *     .display(Display.FLEX)
 *     .width("100%")
 *     .lock(); // Make immutable
 * style.exportToFile(new File("styles.css"), true); // Minified output
 * </pre>
 *
 * @author Albert Beaupre
 * @version 1.0
 * @since March 15th, 2025
 */
public class Style {
    /** Logger instance for debugging and auditing style operations. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Style.class);

    /** Pre-configured settings for minified CSS output. */
    private static final CSSWriterSettings MINIFIED_SETTINGS = new CSSWriterSettings(ECSSVersion.CSS30, true);

    /** Pre-configured settings for formatted (readable) CSS output. */
    private static final CSSWriterSettings FORMATTED_SETTINGS = new CSSWriterSettings(ECSSVersion.CSS30, false);

    /** The underlying stylesheet object holding all CSS rules. */
    private final CascadingStyleSheet css;

    /** The single style rule for the selector, containing all declarations. */
    private final CSSStyleRule styleRule;

    /** The CSS selector (e.g., ".my-component") to which styles are applied. */
    private final String selector;

    /** Unique identifier for this Style instance, aiding traceability in logs. */
    private final String instanceId;

    /** Flag indicating if the style is locked (immutable). */
    private boolean isLocked = false;

    /** List of custom media rules for extensibility (e.g., responsive design). */
    private final List<CSSMediaRule> mediaRules = new ArrayList<>();

    /** EventPublisher used for style changing events **/
    private EventPublisher publisher = new EventPublisher();

    /**
     * Constructs a new {@code Style} instance tied to a specific CSS selector.
     * Initializes a {@link CascadingStyleSheet} and creates a {@link CSSStyleRule} with the given selector.
     *
     * @param selector the CSS selector (e.g., ".my-component", "#id"); must be non-null and non-empty
     * @throws IllegalArgumentException if the selector is null, empty, or invalid
     */
    public Style(EventListener<PropertyChangeEvent> listener, String selector) {
        validateSelector(selector);
        this.selector = selector;
        this.instanceId = UUID.randomUUID().toString();
        this.css = new CascadingStyleSheet();
        this.styleRule = new CSSStyleRule();

        CSSSelector cssSelector = new CSSSelector();
        cssSelector.addMember(new CSSSelectorSimpleMember(selector));
        this.styleRule.addSelector(cssSelector);
        css.addRule(styleRule);
        LOGGER.debug("Created Style instance [{}] for selector: {}", instanceId, selector);
    }

    /**
     * Validates the selector to ensure it’s suitable for CSS.
     *
     * @param selector the selector to validate
     * @throws IllegalArgumentException if invalid
     */
    private void validateSelector(String selector) {
        if (selector == null || selector.trim().isEmpty()) {
            throw new IllegalArgumentException("Selector must not be null or empty");
        }
        if (selector.contains(" ") || selector.contains("{")) {
            throw new IllegalArgumentException("Selector must be a single, valid CSS identifier (no spaces or braces)");
        }
    }

    /**
     * Validates a property-value pair and logs the operation.
     *
     * @param property the CSS property
     * @param value    the value to set
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if property or value is invalid
     */
    private void validateAndLog(String property, String value) {
        if (isLocked) {
            throw new IllegalStateException("Cannot modify locked Style instance [" + instanceId + "]");
        }
        if (property == null || property.trim().isEmpty()) {
            throw new IllegalArgumentException("Property must not be null or empty");
        }
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Value for property '" + property + "' must not be null or empty");
        }
        LOGGER.trace("Style [{}] - Setting property '{}: {}' for selector: {}", instanceId, property, value, selector);
    }

    /**
     * Adds a generic CSS property-value pair to the style rule.
     *
     * @param property the CSS property name (e.g., "color")
     * @param value    the value for the property (e.g., "red")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if property or value is invalid
     */
    public Style set(String property, String value) {
        validateAndLog(property, value);
        styleRule.addDeclaration(new CSSDeclaration(property, CSSExpression.createSimple(value)));

        publisher.publish(new PropertyChangeEvent(property, value));
        return this;
    }

    /**
     * Sets the CSS {@code display} property using a predefined {@link Display} enum value.
     *
     * @param display the display type (e.g., {@code Display.FLEX})
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     * @throws NullPointerException  if display is null
     * @see Display
     */
    public Style display(Display display) {
        if (display == null) throw new NullPointerException("Display enum cannot be null");
        validateAndLog(Display.getPropertyName(), display.getValue());
        set(Display.getPropertyName(), display.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code flex-direction} property using a {@link FlexDirection} enum value.
     *
     * @param flexDirection the flex direction (e.g., {@code FlexDirection.ROW})
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     * @throws NullPointerException  if flexDirection is null
     * @see FlexDirection
     */
    public Style flexDirection(FlexDirection flexDirection) {
        if (flexDirection == null) throw new NullPointerException("FlexDirection enum cannot be null");
        validateAndLog(FlexDirection.getPropertyName(), flexDirection.getValue());
        set(FlexDirection.getPropertyName(), flexDirection.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code flex-wrap} property using a {@link FlexWrap} enum value.
     *
     * @param flexWrap the flex wrap behavior (e.g., {@code FlexWrap.WRAP})
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     * @throws NullPointerException  if flexWrap is null
     * @see FlexWrap
     */
    public Style flexWrap(FlexWrap flexWrap) {
        if (flexWrap == null) throw new NullPointerException("FlexWrap enum cannot be null");
        validateAndLog(FlexWrap.getPropertyName(), flexWrap.getValue());
        set(FlexWrap.getPropertyName(), flexWrap.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code flex-grow} property using a {@link FlexGrow} enum value.
     *
     * @param flexGrow the flex grow factor (e.g., {@code FlexGrow.GROW_0})
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     * @throws NullPointerException  if flexGrow is null
     * @see FlexGrow
     */
    public Style flexGrow(FlexGrow flexGrow) {
        if (flexGrow == null) throw new NullPointerException("FlexGrow enum cannot be null");
        validateAndLog(FlexGrow.getPropertyName(), flexGrow.getValue());
        set(FlexGrow.getPropertyName(), flexGrow.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code flex-shrink} property using a {@link FlexShrink} enum value.
     *
     * @param flexShrink the flex shrink factor (e.g., {@code FlexShrink.SHRINK_0})
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     * @throws NullPointerException  if flexShrink is null
     * @see FlexShrink
     */
    public Style flexShrink(FlexShrink flexShrink) {
        if (flexShrink == null) throw new NullPointerException("FlexShrink enum cannot be null");
        validateAndLog(FlexShrink.getPropertyName(), flexShrink.getValue());
        set(FlexShrink.getPropertyName(), flexShrink.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code flex} shorthand property using a {@link Flex} enum value.
     *
     * @param flex the flex shorthand value (e.g., {@code Flex.FLEX_1})
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     * @throws NullPointerException  if flex is null
     * @see Flex
     */
    public Style flex(Flex flex) {
        if (flex == null) throw new NullPointerException("Flex enum cannot be null");
        validateAndLog(Flex.getPropertyName(), flex.getValue());
        set(Flex.getPropertyName(), flex.getValue());
        return this;
    }

    // 2. Alignment Classes

    /**
     * Sets the CSS {@code align-items} property using a {@link AlignItems} enum value.
     *
     * @param alignItems the alignment (e.g., {@code AlignItems.CENTER})
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     * @throws NullPointerException  if alignItems is null
     * @see AlignItems
     */
    public Style alignItems(AlignItems alignItems) {
        if (alignItems == null) throw new NullPointerException("AlignItems enum cannot be null");
        validateAndLog(AlignItems.getPropertyName(), alignItems.getValue());
        set(AlignItems.getPropertyName(), alignItems.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code align-self} property using a {@link AlignSelf} enum value.
     *
     * @param alignSelf the self-alignment (e.g., {@code AlignSelf.CENTER})
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     * @throws NullPointerException  if alignSelf is null
     * @see AlignSelf
     */
    public Style alignSelf(AlignSelf alignSelf) {
        if (alignSelf == null) throw new NullPointerException("AlignSelf enum cannot be null");
        validateAndLog(AlignSelf.getPropertyName(), alignSelf.getValue());
        set(AlignSelf.getPropertyName(), alignSelf.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code justify-content} property using a {@link JustifyContent} enum value.
     *
     * @param justifyContent the justification (e.g., {@code JustifyContent.BETWEEN})
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     * @throws NullPointerException  if justifyContent is null
     * @see JustifyContent
     */
    public Style justifyContent(JustifyContent justifyContent) {
        if (justifyContent == null) throw new NullPointerException("JustifyContent enum cannot be null");
        validateAndLog(JustifyContent.getPropertyName(), justifyContent.getValue());
        set(JustifyContent.getPropertyName(), justifyContent.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code align-content} property using a {@link AlignContent} enum value.
     *
     * @param alignContent the content alignment (e.g., {@code AlignContent.STRETCH})
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     * @throws NullPointerException  if alignContent is null
     * @see AlignContent
     */
    public Style alignContent(AlignContent alignContent) {
        if (alignContent == null) throw new NullPointerException("AlignContent enum cannot be null");
        validateAndLog(AlignContent.getPropertyName(), alignContent.getValue());
        set(AlignContent.getPropertyName(), alignContent.getValue());
        return this;
    }

    // 3. Spacing Classes

    /**
     * Sets the CSS {@code margin} property with a custom value.
     *
     * @param value the margin value (e.g., "10px", "0 auto")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style margin(String value) {
        validateAndLog("margin", value);
        set("margin", value);
        return this;
    }

    /**
     * Sets the CSS {@code padding} property with a custom value.
     *
     * @param value the padding value (e.g., "5px", "1rem 2rem")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style padding(String value) {
        validateAndLog("padding", value);
        set("padding", value);
        return this;
    }

    // 4. Sizing Classes

    /**
     * Sets the CSS {@code width} property with a custom value.
     *
     * @param value the width value (e.g., "100%", "200px")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style width(String value) {
        validateAndLog("width", value);
        set("width", value);
        return this;
    }

    /**
     * Sets the CSS {@code height} property with a custom value.
     *
     * @param value the height value (e.g., "50%", "100px")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style height(String value) {
        validateAndLog("height", value);
        set("height", value);
        return this;
    }

    // 5. Typography Classes

    /**
     * Sets the CSS {@code font-weight} property using a {@link FontWeight} enum value.
     *
     * @param fontWeight the font weight (e.g., {@code FontWeight.BOLD})
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     * @throws NullPointerException  if fontWeight is null
     * @see FontWeight
     */
    public Style fontWeight(FontWeight fontWeight) {
        if (fontWeight == null) throw new NullPointerException("FontWeight enum cannot be null");
        validateAndLog(FontWeight.getPropertyName(), fontWeight.getValue());
        set(FontWeight.getPropertyName(), fontWeight.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code text-align} property using a {@link TextAlign} enum value.
     *
     * @param textAlign the text alignment (e.g., {@code TextAlign.CENTER})
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     * @throws NullPointerException  if textAlign is null
     * @see TextAlign
     */
    public Style textAlign(TextAlign textAlign) {
        if (textAlign == null) throw new NullPointerException("TextAlign enum cannot be null");
        validateAndLog(TextAlign.getPropertyName(), textAlign.getValue());
        set(TextAlign.getPropertyName(), textAlign.getValue());
        return this;
    }

    // 6. Visibility and Positioning

    /**
     * Sets the CSS {@code visibility} property using a {@link Visibility} enum value.
     *
     * @param visibility the visibility state (e.g., {@code Visibility.VISIBLE})
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     * @throws NullPointerException  if visibility is null
     * @see Visibility
     */
    public Style visibility(Visibility visibility) {
        if (visibility == null) throw new NullPointerException("Visibility enum cannot be null");
        validateAndLog(Visibility.getPropertyName(), visibility.getValue());
        set(Visibility.getPropertyName(), visibility.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code position} property using a {@link Position} enum value.
     *
     * @param position the position type (e.g., {@code Position.RELATIVE})
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     * @throws NullPointerException  if position is null
     * @see Position
     */
    public Style position(Position position) {
        if (position == null) throw new NullPointerException("Position enum cannot be null");
        validateAndLog(Position.getPropertyName(), position.getValue());
        set(Position.getPropertyName(), position.getValue());
        return this;
    }

    // 7. Miscellaneous

    /**
     * Sets the CSS {@code overflow} property using a {@link Overflow} enum value.
     *
     * @param overflow the overflow behavior (e.g., {@code Overflow.HIDDEN})
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     * @throws NullPointerException  if overflow is null
     * @see Overflow
     */
    public Style overflow(Overflow overflow) {
        if (overflow == null) throw new NullPointerException("Overflow enum cannot be null");
        validateAndLog(Overflow.getPropertyName(), overflow.getValue());
        set(Overflow.getPropertyName(), overflow.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code border-radius} property with a custom value.
     *
     * @param value the border-radius value (e.g., "1px solid black")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style borderRadius(String value) {
        validateAndLog("border-radius", value);
        set("border-radius", value);
        return this;
    }

    /**
     * Sets the CSS {@code border} property with a custom value.
     *
     * @param value the border value (e.g., "1px solid black")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style border(String value) {
        validateAndLog("border", value);
        set("border", value);
        return this;
    }

    /**
     * Retrieves the value of a specific CSS property from the style rule.
     *
     * @param property the CSS property to retrieve (e.g., "display")
     * @return the value of the property, or null if not set
     */
    public String get(String property) {
        if (property == null || property.trim().isEmpty()) {
            LOGGER.warn("Style [{}] - Attempted to get value for null or empty property", instanceId);
            return null;
        }
        for (CSSDeclaration decl : styleRule.getAllDeclarations()) {
            if (decl.getProperty().equals(property)) {
                return decl.getExpression().getAsCSSString();
            }
        }
        return null;
    }

    /**
     * Removes all declarations from the style rule, resetting it.
     *
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     */
    public Style clear() {
        if (isLocked) {
            throw new IllegalStateException("Cannot clear locked Style instance [" + instanceId + "]");
        }
        styleRule.removeAllDeclarations();
        LOGGER.debug("Style [{}] - Cleared all declarations for selector: {}", instanceId, selector);
        return this;
    }

    /**
     * Locks the style, making it immutable to prevent further modifications.
     *
     * @return this {@code Style} instance for method chaining
     */
    public Style lock() {
        this.isLocked = true;
        LOGGER.info("Style [{}] - Locked for selector: {}", instanceId, selector);
        return this;
    }

    /**
     * Adds styles for a pseudo-class (e.g., :hover) or pseudo-element (e.g., ::before).
     *
     * @param name the pseudo-class or pseudo-element name (e.g., "hover", "before")
     * @param isElement  true for pseudo-elements (uses ::), false for pseudo-classes (uses :)
     * @param styleConfigurer a consumer to configure the nested style
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     * @throws IllegalArgumentException if name is invalid
     */
    public Style pseudo(String name, boolean isElement, Consumer<Style> styleConfigurer) {
        if (isLocked)
            throw new IllegalStateException("Cannot add pseudo rule to locked Style instance [" + instanceId + "]");

        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Pseudo name must not be null or empty");

        String pseudoSelector = selector + (isElement ? "::" : ":") + name;
        Style pseudoStyle = new Style(null, pseudoSelector);
        styleConfigurer.accept(pseudoStyle);
        css.addRule(pseudoStyle.styleRule);
        LOGGER.debug("Style [{}] - Added pseudo rule for '{}'", instanceId, pseudoSelector);
        return this;
    }

    /**
     * Checks if the style is locked (immutable).
     *
     * @return true if locked, false otherwise
     */
    public boolean isLocked() {
        return isLocked;
    }

    /**
     * Converts the internal {@link CascadingStyleSheet} to a CSS string.
     *
     * @param minify if true, generates minified CSS; otherwise, formatted CSS
     * @return the CSS string representation
     */
    public String toCSS(boolean minify) {
        CSSWriter writer = new CSSWriter(minify ? MINIFIED_SETTINGS : FORMATTED_SETTINGS);
        String cssString = writer.getCSSAsString(css);
        LOGGER.debug("Style [{}] - Generated {} CSS: {}", instanceId, minify ? "minified" : "formatted", cssString);
        return cssString;
    }

    /**
     * Exports the CSS to a file.
     *
     * @param file   the target file to write to
     * @param minify if true, writes minified CSS
     * @throws IOException if file writing fails
     */
    public void exportToFile(File file, boolean minify) throws IOException {
        String cssString = toCSS(minify);
        Files.writeString(file.toPath(), cssString);
        LOGGER.info("Style [{}] - Exported {} CSS to file: {}", instanceId, minify ? "minified" : "formatted", file.getAbsolutePath());
    }

    /**
     * Adds a media query rule for responsive design.
     *
     * @param mediaQuery  the media query (e.g., "screen and (max-width: 600px)")
     * @param nestedStyle the style to apply within the media query
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException if the style is locked
     */
    public Style addMediaRule(String mediaQuery, Style nestedStyle) {
        if (isLocked) {
            throw new IllegalStateException("Cannot add media rule to locked Style instance [" + instanceId + "]");
        }
        CSSMediaRule mediaRule = new CSSMediaRule();
        mediaRule.addMediaQuery(new CSSMediaQuery(mediaQuery));
        mediaRule.addRule(nestedStyle.styleRule);
        css.addRule(mediaRule);
        mediaRules.add(mediaRule);
        LOGGER.debug("Style [{}] - Added media rule for '{}'", instanceId, mediaQuery);
        return this;
    }

    /**
     * Returns the CSS string representation of the style rule.
     *
     * @return the CSS string, equivalent to {@link #toCSS(boolean)} with formatted output
     */
    @Override
    public String toString() {
        return toCSS(false);
    }

    /**
     * Provides access to the underlying {@link CascadingStyleSheet} object.
     *
     * @return the {@link CascadingStyleSheet} instance
     */
    public CascadingStyleSheet getCascadingStyleSheet() {
        return css;
    }
}