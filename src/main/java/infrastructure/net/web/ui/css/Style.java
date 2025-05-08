package infrastructure.net.web.ui.css;

import com.helger.css.ECSSVersion;
import com.helger.css.decl.*;
import com.helger.css.reader.CSSReader;
import com.helger.css.writer.CSSWriter;
import com.helger.css.writer.CSSWriterSettings;
import infrastructure.event.EventListener;
import infrastructure.event.EventPublisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * This class generates CSS stylesheet rules
 * tied to a specific selector, supporting flexbox, alignment, spacing, sizing, typography, visibility,
 * positioning, and miscellaneous properties. It provides robust input validation, immutability
 * options, and extensibility for large-scale styling needs.
 * <p>
 * Key features include:
 * <ul>
 *     <li>Comprehensive input validation to prevent invalid CSS</li>
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

    /**
     * Pre-configured settings for minified CSS output.
     */
    private static final CSSWriterSettings MINIFIED_SETTINGS = new CSSWriterSettings(ECSSVersion.CSS30, true).setRemoveUnnecessaryCode(true);

    /**
     * Pre-configured settings for formatted (readable) CSS output.
     */
    private static final CSSWriterSettings FORMATTED_SETTINGS = new CSSWriterSettings(ECSSVersion.CSS30, false);

    /**
     * The underlying stylesheet object holding all CSS rules.
     */
    private final CascadingStyleSheet css;

    /**
     * The single style rule for the selector, containing all declarations.
     */
    private final CSSStyleRule styleRule;

    /**
     * The CSS selector (e.g., ".my-component") to which styles are applied.
     */
    private final String selector;

    /**
     * Unique identifier for this Style instance, aiding traceability in logs.
     */
    private final String instanceID;

    /**
     * List of custom media rules for extensibility (e.g., responsive design).
     */
    private final List<CSSMediaRule> mediaRules = new ArrayList<>();
    /**
     * EventPublisher used for style-changing events
     **/
    private final EventPublisher publisher = new EventPublisher();
    /**
     * Flag indicating if the style is locked (immutable).
     */
    private boolean isLocked;

    /**
     * Constructs a new {@code Style} instance tied to a specific CSS selector.
     * Initializes a {@link CascadingStyleSheet} and creates a {@link CSSStyleRule} with the given selector.
     *
     * @param selector the CSS selector (e.g., ".my-component", "#id"); must be non-null and non-empty
     * @throws IllegalArgumentException if the selector is null, empty, or invalid
     */
    public Style(EventListener<StyleChangeEvent> listener, String selector) {
        validateSelector(selector);
        this.selector = selector;
        this.instanceID = UUID.randomUUID().toString();
        this.css = new CascadingStyleSheet();
        this.styleRule = new CSSStyleRule();

        CSSSelector cssSelector = new CSSSelector();
        cssSelector.addMember(new CSSSelectorSimpleMember(selector));
        this.styleRule.addSelector(cssSelector);
        css.addRule(styleRule);

        this.publisher.register(StyleChangeEvent.class, listener);
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
     * Validates a property-value pair.
     *
     * @param property the CSS property
     * @param value    the value to set
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if property or value is invalid
     */
    private void validate(String property, String value) {
        if (isLocked) {
            throw new IllegalStateException("Cannot modify locked Style instance [" + instanceID + "]");
        }
        if (property == null || property.trim().isEmpty()) {
            throw new IllegalArgumentException("Property must not be null or empty");
        }
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Value for property '" + property + "' must not be null or empty");
        }
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
        validate(property, value);
        styleRule.addDeclaration(new CSSDeclaration(property, CSSExpression.createSimple(value)));

        publisher.publish(new StyleChangeEvent(property, value));
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
        validate(Display.getPropertyName(), display.getValue());
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
        validate(FlexDirection.getPropertyName(), flexDirection.getValue());
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
        validate(FlexWrap.getPropertyName(), flexWrap.getValue());
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
        validate(FlexGrow.getPropertyName(), flexGrow.getValue());
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
        validate(FlexShrink.getPropertyName(), flexShrink.getValue());
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
        validate(Flex.getPropertyName(), flex.getValue());
        set(Flex.getPropertyName(), flex.getValue());
        return this;
    }

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
        validate(AlignItems.getPropertyName(), alignItems.getValue());
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
        validate(AlignSelf.getPropertyName(), alignSelf.getValue());
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
        validate(JustifyContent.getPropertyName(), justifyContent.getValue());
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
        validate(AlignContent.getPropertyName(), alignContent.getValue());
        set(AlignContent.getPropertyName(), alignContent.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code margin} property with a custom value.
     *
     * @param value the margin value (e.g., "10px", "0 auto")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style margin(String value) {
        validate("margin", value);
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
        validate("padding", value);
        set("padding", value);
        return this;
    }

    /**
     * Sets the CSS {@code width} property with a custom value.
     *
     * @param value the width value (e.g., "100%", "200px")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style width(String value) {
        validate("width", value);
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
        validate("height", value);
        set("height", value);
        return this;
    }

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
        validate(FontWeight.getPropertyName(), fontWeight.getValue());
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
        validate(TextAlign.getPropertyName(), textAlign.getValue());
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
        validate(Visibility.getPropertyName(), visibility.getValue());
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
        validate(Position.getPropertyName(), position.getValue());
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
        validate(Overflow.getPropertyName(), overflow.getValue());
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
        validate("border-radius", value);
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
        validate("border", value);
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
            throw new IllegalStateException("Cannot clear locked Style instance [" + instanceID + "]");
        }
        styleRule.removeAllDeclarations();
        return this;
    }

    /**
     * Locks the style, making it immutable to prevent further modifications.
     *
     * @return this {@code Style} instance for method chaining
     */
    public Style lock() {
        this.isLocked = true;
        return this;
    }

    /**
     * Sets the CSS {@code gap} property to define spacing between items in grids and flex containers.
     *
     * @param value the gap size (e.g., "10px", "1rem", "0.5em")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style gap(String value) {
        validate("gap", value);
        set("gap", value);
        return this;
    }

    /**
     * Sets the CSS {@code column-gap} property to define spacing between columns in grid layouts.
     *
     * @param value the column gap size (e.g., "20px", "1rem")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style columnGap(String value) {
        validate("column-gap", value);
        set("column-gap", value);
        return this;
    }

    /**
     * Sets the CSS {@code row-gap} property to define spacing between rows in grid layouts.
     *
     * @param value the row gap size (e.g., "15px", "0.5rem")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style rowGap(String value) {
        validate("row-gap", value);
        set("row-gap", value);
        return this;
    }

    /**
     * Sets the CSS {@code grid-template-columns} property to define column structure in grid layouts.
     *
     * @param definition the grid column template (e.g., "1fr 2fr 1fr", "repeat(3, 1fr)")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if definition is null or empty
     */
    public Style gridTemplateColumns(String definition) {
        validate("grid-template-columns", definition);
        set("grid-template-columns", definition);
        return this;
    }

    /**
     * Sets the CSS {@code grid-template-rows} property to define row structure in grid layouts.
     *
     * @param definition the grid row template (e.g., "auto 1fr", "repeat(2, 100px)")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if definition is null or empty
     */
    public Style gridTemplateRows(String definition) {
        validate("grid-template-rows", definition);
        set("grid-template-rows", definition);
        return this;
    }

    /**
     * Sets the CSS {@code grid-auto-flow} property to control auto-placement in grid layouts.
     *
     * @param value the auto-flow behavior (e.g., "row", "column", "dense")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style gridAutoFlow(String value) {
        validate("grid-auto-flow", value);
        set("grid-auto-flow", value);
        return this;
    }

    /**
     * Sets the CSS {@code min-width} property to define the minimum width of an element.
     *
     * @param value the minimum width (e.g., "200px", "50%")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style minWidth(String value) {
        validate("min-width", value);
        set("min-width", value);
        return this;
    }

    /**
     * Sets the CSS {@code max-width} property to define the maximum width of an element.
     *
     * @param value the maximum width (e.g., "500px", "100%")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style maxWidth(String value) {
        validate("max-width", value);
        set("max-width", value);
        return this;
    }

    /**
     * Sets the CSS {@code min-height} property to define the minimum height of an element.
     *
     * @param value the minimum height (e.g., "100px", "20vh")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style minHeight(String value) {
        validate("min-height", value);
        set("min-height", value);
        return this;
    }

    /**
     * Sets the CSS {@code max-height} property to define the maximum height of an element.
     *
     * @param value the maximum height (e.g., "400px", "80vh")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style maxHeight(String value) {
        validate("max-height", value);
        set("max-height", value);
        return this;
    }

    /**
     * Sets the CSS {@code box-sizing} property to control box model behavior.
     *
     * @param value the box sizing model (e.g., "border-box", "content-box")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style boxSizing(String value) {
        validate("box-sizing", value);
        set("box-sizing", value);
        return this;
    }

    /**
     * Sets the CSS {@code cursor} property to control the mouse cursor appearance.
     *
     * @param value the cursor type (e.g., "pointer", "move", "text")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style cursor(String value) {
        validate("cursor", value);
        set("cursor", value);
        return this;
    }

    /**
     * Sets the CSS {@code opacity} property to control element transparency.
     *
     * @param value the opacity level (e.g., "0.5", "1")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style opacity(String value) {
        validate("opacity", value);
        set("opacity", value);
        return this;
    }

    /**
     * Sets the CSS {@code z-index} property to control stacking order.
     *
     * @param value the z-index value (e.g., "10", "1000")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style zIndex(String value) {
        validate("z-index", value);
        set("z-index", value);
        return this;
    }

    /**
     * Sets the CSS {@code top} property for positioned elements.
     *
     * @param value the top offset (e.g., "0", "10px")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style top(String value) {
        validate("top", value);
        set("top", value);
        return this;
    }

    /**
     * Sets the CSS {@code right} property for positioned elements.
     *
     * @param value the right offset (e.g., "0", "10px")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style right(String value) {
        validate("right", value);
        set("right", value);
        return this;
    }


    /**
     * Sets the CSS {@code outline} property to define an outline around an element.
     *
     * @param value the outline value (e.g., "1px solid red")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style outline(String value) {
        validate("outline", value);
        set("outline", value);
        return this;
    }

    /**
     * Sets the CSS {@code outline-offset} property to offset the outline from an element's border.
     *
     * @param value the outline offset (e.g., "5px")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style outlineOffset(String value) {
        validate("outline-offset", value);
        set("outline-offset", value);
        return this;
    }

    /**
     * Sets the CSS {@code box-shadow} property to add shadow effects around an element's frame.
     *
     * @param value the box-shadow value (e.g., "0 4px 6px rgba(0, 0, 0, 0.1)")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style boxShadow(String value) {
        validate("box-shadow", value);
        set("box-shadow", value);
        return this;
    }

    /**
     * Sets the CSS {@code background} shorthand property for background styling.
     *
     * @param value the background shorthand (e.g., "#fff url('img.png') no-repeat center/cover")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style background(String value) {
        validate("background", value);
        set("background", value);
        return this;
    }

    /**
     * Sets the CSS {@code background-color} property.
     *
     * @param value the background color (e.g., "#e0e0e0", "rgba(255, 0, 0, 0.5)")
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if value is null or empty
     */
    public Style backgroundColor(String value) {
        validate("background-color", value);
        set("background-color", value);
        return this;
    }

    /**
     * Adds styles for a pseudo-class (e.g., :hover) or pseudo-element (e.g., ::before).
     *
     * @param name            the pseudo-class or pseudo-element name (e.g., "hover", "before")
     * @param isElement       true for pseudo-elements (uses ::), false for pseudo-classes (uses :)
     * @param styleConfigurer a consumer to configure the nested style
     * @return this {@code Style} instance for method chaining
     * @throws IllegalStateException    if the style is locked
     * @throws IllegalArgumentException if name is invalid
     */
    public Style pseudo(String name, boolean isElement, Consumer<Style> styleConfigurer) {
        if (isLocked)
            throw new IllegalStateException("Cannot add pseudo rule to locked Style instance [" + instanceID + "]");

        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Pseudo name must not be null or empty");

        String pseudoSelector = selector + (isElement ? "::" : ":") + name;
        Style pseudoStyle = new Style(null, pseudoSelector);
        styleConfigurer.accept(pseudoStyle);
        css.addRule(pseudoStyle.styleRule);
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
        return writer.getCSSAsString(css);
    }

    /**
     * Exports the CSS to a file.
     *
     * @param file   the target file to write to
     * @param minify if true, writes minified CSS
     * @throws IOException if file writing fails
     */
    public void exportToFile(File file, boolean minify) throws IOException {
        Files.writeString(file.toPath(), toCSS(minify));
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
        if (isLocked)
            throw new IllegalStateException("Cannot add media rule to locked Style instance [" + instanceID + "]");

        CSSMediaRule mediaRule = new CSSMediaRule();
        mediaRule.addMediaQuery(new CSSMediaQuery(mediaQuery));
        mediaRule.addRule(nestedStyle.styleRule);
        css.addRule(mediaRule);
        mediaRules.add(mediaRule);
        return this;
    }

    /**
     * Converts the style rule's declarations into an inline CSS string suitable for a HTML element's style attribute.
     * For example, if the style has properties like `width: 100%` and `display: flex`, this returns
     * `width: 100%; display: flex;`. Ignores pseudo-classes and media rules, as they cannot be applied inline.
     *
     * @return a string containing all declarations formatted for inline CSS (e.g., `property: value;`)
     */
    public String inline() {
        StringBuilder inline = new StringBuilder();
        for (CSSDeclaration decl : styleRule.getAllDeclarations()) {
            String property = decl.getProperty();
            String value = decl.getExpression().getAsCSSString();
            inline.append(property).append(": ").append(value).append("; ");
        }
        return inline.toString().trim();
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

    /**
     * @return The selector of this Style.
     */
    public String getSelector() {
        return selector;
    }

    /**
     * Parses the given CSS content and returns a new Style based on the first rule found.
     *
     * @param listener   an event listener for style changes (must be non-null)
     * @param cssContent raw CSS (e.g. ".foo { color: red; }")
     * @return a Style instance pre-populated with selector and declarations from the first rule
     * @throws NullPointerException     if the listener is null
     * @throws IllegalArgumentException if cssContent is null/empty, cannot be parsed, or contains no style rules
     */
    public static Style fromString(EventListener<StyleChangeEvent> listener, String cssContent) {
        if (listener == null)
            throw new NullPointerException("EventListener cannot be null");
        if (cssContent == null || cssContent.trim().isEmpty())
            throw new IllegalArgumentException("CSS content must not be null or empty");

        CascadingStyleSheet sheet = CSSReader.readFromString(cssContent, ECSSVersion.CSS30);
        if (sheet == null)
            throw new IllegalArgumentException("Failed to parse CSS content");  // :contentReference[oaicite:0]{index=0}

        for (ICSSTopLevelRule rule : sheet.getAllRules()) {
            if (rule instanceof CSSStyleRule cssRule) {
                String sel = Objects.requireNonNull(cssRule.getSelectorAtIndex(0)).getAsCSSString();
                Style style = new Style(listener, sel);

                for (CSSDeclaration decl : cssRule.getAllDeclarations()) {
                    style.set(decl.getProperty(), decl.getExpression().getAsCSSString());
                }
                return style;
            }
        }

        throw new IllegalArgumentException("No style rule found in CSS content");
    }

    /**
     * Reads the entire CSS file and returns a new Style based on its first rule.
     *
     * @param listener an event listener for style changes (must be non-null)
     * @param file     the CSS file to read
     * @return a Style instance pre-populated with selector and declarations from the first rule
     * @throws IOException              if reading the file fails
     * @throws NullPointerException     if the listener is null
     * @throws IllegalArgumentException if the file is null or no style rule is found
     */
    public static Style fromFile(EventListener<StyleChangeEvent> listener, File file) throws IOException {
        if (listener == null)
            throw new NullPointerException("EventListener cannot be null");
        if (file == null)
            throw new IllegalArgumentException("File must not be null");

        String content = Files.readString(file.toPath());
        return fromString(listener, content);
    }
}