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
 * style.exportToFile(new File("dark.css"), true); // Minified output
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
        if (isLocked)
            throw new IllegalStateException("Cannot modify locked Style instance [" + instanceID + "]");
        if (property == null || property.trim().isEmpty())
            throw new IllegalArgumentException("Property must not be null or empty");
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException("Value for property '" + property + "' must not be null or empty");
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
        if (property == null || property.trim().isEmpty())
            return null;
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
        if (isLocked)
            throw new IllegalStateException("Cannot clear locked Style instance [" + instanceID + "]");

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
            throw new IllegalArgumentException("Failed to parse CSS content");

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
     * Sets the CSS {@code bottom} property.
     *
     * @param value the CSS bottom offset (e.g., "0", "10px")
     * @return this {@code Style} instance for chaining
     */
    public Style bottom(String value) {
        set("bottom", value);
        return this;
    }

    /**
     * Sets the CSS {@code left} property.
     *
     * @param value the CSS left offset (e.g., "0", "5px")
     * @return this {@code Style} instance for chaining
     */
    public Style left(String value) {
        set("left", value);
        return this;
    }

    /**
     * Sets the CSS {@code margin-top} property.
     *
     * @param value the margin value for the top (e.g., "1rem", "10px")
     * @return this {@code Style} instance for chaining
     */
    public Style marginTop(String value) {
        set("margin-top", value);
        return this;
    }

    /**
     * Sets the CSS {@code margin-right} property.
     *
     * @param value the margin value for the right (e.g., "1rem")
     * @return this {@code Style} instance for chaining
     */
    public Style marginRight(String value) {
        set("margin-right", value);
        return this;
    }

    /**
     * Sets the CSS {@code margin-bottom} property.
     *
     * @param value the margin value for the bottom (e.g., "10px")
     * @return this {@code Style} instance for chaining
     */
    public Style marginBottom(String value) {
        set("margin-bottom", value);
        return this;
    }

    /**
     * Sets the CSS {@code margin-left} property.
     *
     * @param value the margin value for the left (e.g., "2em")
     * @return this {@code Style} instance for chaining
     */
    public Style marginLeft(String value) {
        set("margin-left", value);
        return this;
    }

    /**
     * Sets the CSS {@code padding-top} property.
     *
     * @param value the top padding value (e.g., "1rem")
     * @return this {@code Style} instance for chaining
     */
    public Style paddingTop(String value) {
        set("padding-top", value);
        return this;
    }

    /**
     * Sets the CSS {@code padding-right} property.
     *
     * @param value the right padding value (e.g., "0.5em")
     * @return this {@code Style} instance for chaining
     */
    public Style paddingRight(String value) {
        set("padding-right", value);
        return this;
    }

    /**
     * Sets the CSS {@code padding-bottom} property.
     *
     * @param value the bottom padding value (e.g., "10px")
     * @return this {@code Style} instance for chaining
     */
    public Style paddingBottom(String value) {
        set("padding-bottom", value);
        return this;
    }

    /**
     * Sets the CSS {@code padding-left} property.
     *
     * @param value the left padding value (e.g., "1rem")
     * @return this {@code Style} instance for chaining
     */
    public Style paddingLeft(String value) {
        set("padding-left", value);
        return this;
    }

    /**
     * Sets the CSS {@code border-top} property.
     *
     * @param value the border value for the top (e.g., "1px solid black")
     * @return this {@code Style} instance for chaining
     */
    public Style borderTop(String value) {
        set("border-top", value);
        return this;
    }

    /**
     * Sets the CSS {@code border-right} property.
     *
     * @param value the border value for the right
     * @return this {@code Style} instance for chaining
     */
    public Style borderRight(String value) {
        set("border-right", value);
        return this;
    }

    /**
     * Sets the CSS {@code border-bottom} property.
     *
     * @param value the border value for the bottom
     * @return this {@code Style} instance for chaining
     */
    public Style borderBottom(String value) {
        set("border-bottom", value);
        return this;
    }

    /**
     * Sets the CSS {@code border-left} property.
     *
     * @param value the border value for the left
     * @return this {@code Style} instance for chaining
     */
    public Style borderLeft(String value) {
        set("border-left", value);
        return this;
    }

    /**
     * Sets the CSS {@code border-width} property.
     *
     * @param value the width of the border (e.g., "2px")
     * @return this {@code Style} instance for chaining
     */
    public Style borderWidth(String value) {
        set("border-width", value);
        return this;
    }

    /**
     * Sets the CSS {@code border-style} property.
     *
     * @param value the style of the border (e.g., "solid", "dashed")
     * @return this {@code Style} instance for chaining
     */
    public Style borderStyle(String value) {
        set("border-style", value);
        return this;
    }

    /**
     * Sets the CSS {@code border-color} property.
     *
     * @param value the color of the border (e.g., "red", "#ccc")
     * @return this {@code Style} instance for chaining
     */
    public Style borderColor(String value) {
        set("border-color", value);
        return this;
    }

    /**
     * Sets the CSS {@code font-size} property.
     *
     * @param value the font size (e.g., "16px", "1.25rem")
     * @return this {@code Style} instance for chaining
     */
    public Style fontSize(String value) {
        set("font-size", value);
        return this;
    }

    /**
     * Sets the CSS {@code font-family} property.
     *
     * @param value the font family (e.g., "Arial, sans-serif")
     * @return this {@code Style} instance for chaining
     */
    public Style fontFamily(String value) {
        set("font-family", value);
        return this;
    }

    /**
     * Sets the CSS {@code font-style} property.
     *
     * @param style the font style (e.g., {@link FontStyle#ITALIC})
     * @return this {@code Style} instance for chaining
     */
    public Style fontStyle(FontStyle style) {
        set(FontStyle.getPropertyName(), style.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code line-height} property.
     *
     * @param value the line height (e.g., "1.5", "24px")
     * @return this {@code Style} instance for chaining
     */
    public Style lineHeight(String value) {
        set("line-height", value);
        return this;
    }

    /**
     * Sets the CSS {@code letter-spacing} property.
     *
     * @param value the spacing between letters (e.g., "0.05em", "1px")
     * @return this {@code Style} instance for chaining
     */
    public Style letterSpacing(String value) {
        set("letter-spacing", value);
        return this;
    }

    /**
     * Sets the CSS {@code word-spacing} property.
     *
     * @param value the spacing between words (e.g., "2px")
     * @return this {@code Style} instance for chaining
     */
    public Style wordSpacing(String value) {
        set("word-spacing", value);
        return this;
    }

    /**
     * Sets the CSS {@code text-decoration} property using a {@link TextDecoration} enum.
     *
     * @param deco the text decoration (e.g., {@link TextDecoration#UNDERLINE})
     * @return this {@code Style} instance for chaining
     */
    public Style textDecoration(TextDecoration deco) {
        set(TextDecoration.getPropertyName(), deco.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code text-transform} property using a {@link TextTransform} enum.
     *
     * @param trans the text transformation (e.g., {@link TextTransform#UPPERCASE})
     * @return this {@code Style} instance for chaining
     */
    public Style textTransform(TextTransform trans) {
        set(TextTransform.getPropertyName(), trans.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code white-space} property using a {@link WhiteSpace} enum.
     *
     * @param ws the white-space behavior (e.g., {@link WhiteSpace#NOWRAP})
     * @return this {@code Style} instance for chaining
     */
    public Style whiteSpace(WhiteSpace ws) {
        set(WhiteSpace.getPropertyName(), ws.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code text-overflow} property.
     *
     * @param value the text overflow behavior (e.g., "ellipsis")
     * @return this {@code Style} instance for chaining
     */
    public Style textOverflow(String value) {
        set("text-overflow", value);
        return this;
    }

    /**
     * Sets the CSS {@code background-image} property.
     *
     * @param value the background image (e.g., "url('img.png')")
     * @return this {@code Style} instance for chaining
     */
    public Style backgroundImage(String value) {
        set("background-image", value);
        return this;
    }

    /**
     * Sets the CSS {@code background-position} property.
     *
     * @param value the background position (e.g., "center center", "top left")
     * @return this {@code Style} instance for chaining
     */
    public Style backgroundPosition(String value) {
        set("background-position", value);
        return this;
    }

    /**
     * Sets the CSS {@code background-size} property.
     *
     * @param value the background size (e.g., "cover", "contain")
     * @return this {@code Style} instance for chaining
     */
    public Style backgroundSize(String value) {
        set("background-size", value);
        return this;
    }

    /**
     * Sets the CSS {@code background-repeat} property using a {@link BackgroundRepeat} enum.
     *
     * @param rep the repeat value (e.g., {@link BackgroundRepeat#NO_REPEAT})
     * @return this {@code Style} instance for chaining
     */
    public Style backgroundRepeat(BackgroundRepeat rep) {
        set(BackgroundRepeat.getPropertyName(), rep.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code background-attachment} property using a {@link BackgroundAttachment} enum.
     *
     * @param att the attachment behavior (e.g., {@link BackgroundAttachment#FIXED})
     * @return this {@code Style} instance for chaining
     */
    public Style backgroundAttachment(BackgroundAttachment att) {
        set(BackgroundAttachment.getPropertyName(), att.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code grid-auto-rows} property.
     *
     * @param value the row sizing behavior (e.g., "min-content", "100px")
     * @return this {@code Style} instance for chaining
     */
    public Style gridAutoRows(String value) {
        set("grid-auto-rows", value);
        return this;
    }

    /**
     * Sets the CSS {@code grid-auto-columns} property.
     *
     * @param value the column sizing behavior (e.g., "1fr", "200px")
     * @return this {@code Style} instance for chaining
     */
    public Style gridAutoColumns(String value) {
        set("grid-auto-columns", value);
        return this;
    }

    /**
     * Sets the CSS {@code grid-row} property.
     *
     * @param value the grid row placement (e.g., "1 / span 2")
     * @return this {@code Style} instance for chaining
     */
    public Style gridRow(String value) {
        set("grid-row", value);
        return this;
    }

    /**
     * Sets the CSS {@code grid-column} property.
     *
     * @param value the grid column placement (e.g., "2 / 4")
     * @return this {@code Style} instance for chaining
     */
    public Style gridColumn(String value) {
        set("grid-column", value);
        return this;
    }

    /**
     * Sets the CSS {@code grid-area} property.
     *
     * @param value the named grid area or shorthand (e.g., "header", "1 / 1 / 2 / 3")
     * @return this {@code Style} instance for chaining
     */
    public Style gridArea(String value) {
        set("grid-area", value);
        return this;
    }

    /**
     * Sets the CSS {@code justify-items} property using a {@link JustifyItems} enum.
     *
     * @param ji the justification value (e.g., {@link JustifyItems#CENTER})
     * @return this {@code Style} instance for chaining
     */
    public Style justifyItems(JustifyItems ji) {
        set(JustifyItems.getPropertyName(), ji.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code justify-self} property using a {@link JustifySelf} enum.
     *
     * @param js the justification value (e.g., {@link JustifySelf#END})
     * @return this {@code Style} instance for chaining
     */
    public Style justifySelf(JustifySelf js) {
        set(JustifySelf.getPropertyName(), js.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code transform} property.
     *
     * @param value the transform function(s) (e.g., "scale(1.2) rotate(45deg)")
     * @return this {@code Style} instance for chaining
     */
    public Style transform(String value) {
        set("transform", value);
        return this;
    }

    /**
     * Sets the CSS {@code transform-origin} property.
     *
     * @param value the transform origin point (e.g., "center", "top left")
     * @return this {@code Style} instance for chaining
     */
    public Style transformOrigin(String value) {
        set("transform-origin", value);
        return this;
    }

    /**
     * Sets the CSS {@code transition} property.
     *
     * @param value the transition specification (e.g., "all 0.3s ease")
     * @return this {@code Style} instance for chaining
     */
    public Style transition(String value) {
        set("transition", value);
        return this;
    }

    /**
     * Sets the CSS {@code animation} property.
     *
     * @param value the animation shorthand (e.g., "fade-in 1s ease-in-out")
     * @return this {@code Style} instance for chaining
     */
    public Style animation(String value) {
        set("animation", value);
        return this;
    }

    /**
     * Sets the CSS {@code filter} property.
     *
     * @param value the filter function (e.g., "blur(5px)")
     * @return this {@code Style} instance for chaining
     */
    public Style filter(String value) {
        set("filter", value);
        return this;
    }

    /**
     * Sets the CSS {@code backdrop-filter} property.
     *
     * @param value the backdrop filter function (e.g., "brightness(0.8)")
     * @return this {@code Style} instance for chaining
     */
    public Style backdropFilter(String value) {
        set("backdrop-filter", value);
        return this;
    }

    /**
     * Sets the CSS {@code mix-blend-mode} property using a {@link MixBlendMode} enum.
     *
     * @param mbm the blend mode (e.g., {@link MixBlendMode#MULTIPLY})
     * @return this {@code Style} instance for chaining
     */
    public Style mixBlendMode(MixBlendMode mbm) {
        set(MixBlendMode.getPropertyName(), mbm.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code overflow-x} property using a {@link Overflow} enum.
     *
     * @param overflow the overflow behavior for the x-axis
     * @return this {@code Style} instance for chaining
     */
    public Style overflowX(Overflow overflow) {
        set("overflow-x", overflow.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code overflow-y} property using a {@link Overflow} enum.
     *
     * @param overflow the overflow behavior for the y-axis
     * @return this {@code Style} instance for chaining
     */
    public Style overflowY(Overflow overflow) {
        set("overflow-y", overflow.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code scroll-behavior} property using a {@link ScrollBehavior} enum.
     *
     * @param sb the scroll behavior (e.g., {@link ScrollBehavior#SMOOTH})
     * @return this {@code Style} instance for chaining
     */
    public Style scrollBehavior(ScrollBehavior sb) {
        set(ScrollBehavior.getPropertyName(), sb.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code scroll-snap-type} property.
     *
     * @param value the snap type (e.g., "x mandatory")
     * @return this {@code Style} instance for chaining
     */
    public Style scrollSnapType(String value) {
        set("scroll-snap-type", value);
        return this;
    }

    /**
     * Sets the CSS {@code scroll-snap-align} property.
     *
     * @param value the alignment (e.g., "start center")
     * @return this {@code Style} instance for chaining
     */
    public Style scrollSnapAlign(String value) {
        set("scroll-snap-align", value);
        return this;
    }

    /**
     * Sets the CSS {@code pointer-events} property using a {@link PointerEvents} enum.
     *
     * @param pe the pointer event behavior (e.g., {@link PointerEvents#NONE})
     * @return this {@code Style} instance for chaining
     */
    public Style pointerEvents(PointerEvents pe) {
        set(PointerEvents.getPropertyName(), pe.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code user-select} property using a {@link UserSelect} enum.
     *
     * @param us the user select behavior (e.g., {@link UserSelect#NONE})
     * @return this {@code Style} instance for chaining
     */
    public Style userSelect(UserSelect us) {
        set(UserSelect.getPropertyName(), us.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code column-count} property.
     *
     * @param count the number of columns
     * @return this {@code Style} instance for chaining
     */
    public Style columnCount(int count) {
        set("column-count", String.valueOf(count));
        return this;
    }

    /**
     * Sets the CSS {@code column-width} property.
     *
     * @param value the column width (e.g., "200px", "10em")
     * @return this {@code Style} instance for chaining
     */
    public Style columnWidth(String value) {
        set("column-width", value);
        return this;
    }

    /**
     * Sets the CSS {@code column-rule} property.
     *
     * @param value the column rule (e.g., "1px solid #ccc")
     * @return this {@code Style} instance for chaining
     */
    public Style columnRule(String value) {
        set("column-rule", value);
        return this;
    }

    /**
     * Sets the CSS {@code list-style-type} property using a {@link ListStyleType} enum.
     *
     * @param lst the list style type (e.g., {@link ListStyleType#CIRCLE})
     * @return this {@code Style} instance for chaining
     */
    public Style listStyleType(ListStyleType lst) {
        set(ListStyleType.getPropertyName(), lst.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code list-style-position} property using a {@link ListStylePosition} enum.
     *
     * @param lsp the list style position (e.g., {@link ListStylePosition#INSIDE})
     * @return this {@code Style} instance for chaining
     */
    public Style listStylePosition(ListStylePosition lsp) {
        set(ListStylePosition.getPropertyName(), lsp.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code list-style-image} property.
     *
     * @param value the URL or keyword for the list style image (e.g., "url('bullet.png')")
     * @return this {@code Style} instance for chaining
     */
    public Style listStyleImage(String value) {
        set("list-style-image", value);
        return this;
    }

    /**
     * Combines this style's selector with another using a CSS combinator and applies
     * all declarations to the resulting compound selector.
     * <p>
     * For example, if the base selector is {@code ".parent"} and you call
     * {@code combinator(">", ".child")}, the resulting selector will be:
     * {@code ".parent > .child"}
     *
     * @param combinator the CSS combinator to use (e.g., ">", "+", "~", " ")
     * @param otherSel   the selector to combine with (e.g., ".child", "#id")
     * @return this {@code Style} instance for chaining
     * @throws IllegalArgumentException if either selector is invalid
     */
    public Style combinator(String combinator, String otherSel) {
        validateSelector(combinator);
        validateSelector(otherSel);
        String combined = this.selector + " " + combinator + " " + otherSel;

        // Remove old rule and rebuild with new combined selector
        css.getAllRules().remove(styleRule);
        CSSSelector sel = new CSSSelector();
        sel.addMember(new CSSSelectorSimpleMember(combined));
        styleRule.getAllSelectors().clear();
        styleRule.addSelector(sel);
        css.addRule(styleRule);

        return this;
    }

    /**
     * Sets a custom CSS variable using the {@code --name: value} syntax.
     * <p>
     * Custom properties must begin with {@code --}, per CSS specification.
     *
     * @param name  the variable name (e.g., "--main-color")
     * @param value the value of the custom property (e.g., "#ff0000")
     * @return this {@code Style} instance for chaining
     * @throws IllegalArgumentException if the name does not start with "--"
     */
    public Style customProperty(String name, String value) {
        if (!name.startsWith("--"))
            throw new IllegalArgumentException("Custom property must start with '--'");
        set(name, value);
        return this;
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