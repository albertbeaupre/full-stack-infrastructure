package infrastructure.net.web.ui;

import infrastructure.event.EventListener;
import infrastructure.net.web.ui.components.*;
import infrastructure.net.web.ui.css.*;
import infrastructure.net.web.ui.event.ClickEvent;
import infrastructure.net.web.ui.event.KeyDownEvent;
import infrastructure.net.web.ui.event.KeyUpEvent;
import infrastructure.net.web.ui.event.ValueChangeEvent;

/**
 * A fluent builder-style utility for constructing and styling web UI component trees.
 * <p>
 * This class allows declarative UI creation by chaining element definitions, event listeners,
 * and style properties in a readable, hierarchical fashion. Internally tracks a root {@link Component}
 * and a current component pointer to facilitate nesting.
 *
 * <p>Example usage:
 * <pre>{@code
 * Designer.begin(parent)
 *     .div().asParent()
 *         .label("Name:")
 *         .textField("Enter name")
 *         .button("Submit").onClick(event -> handleSubmit());
 * }</pre>
 *
 * <p>All methods return the {@code Designer} instance itself for seamless chaining.
 *
 * @author Albert Beaupre
 * @version 1.0
 * @since May 8th, 2025
 */

public class Designer {

    /**
     * The parent container to which components are added.
     */
    private Component parent;

    /**
     * The current component being operated on in the fluent chain.
     */
    private Component component;

    /**
     * Constructs a new {@code Designer} instance with a given root component.
     *
     * @param parent the root {@link Component} that serves as the parent container
     */
    public Designer(Component parent) {
        this.parent = parent;
        this.component = parent;
    }

    /**
     * Registers a key down event listener on the current component.
     *
     * @param listener the listener to invoke when a key is pressed down
     * @return this {@code Designer} instance for chaining
     */
    public Designer onKeyDown(EventListener<KeyDownEvent> listener) {
        this.component.addKeyDownListener(listener);
        return this;
    }

    /**
     * Registers a key up event listener on the current component.
     *
     * @param listener the listener to invoke when a key is released
     * @return this {@code Designer} instance for chaining
     */
    public Designer onKeyUp(EventListener<KeyUpEvent> listener) {
        this.component.addKeyUpListener(listener);
        return this;
    }

    /**
     * Registers a click event listener on the current component.
     *
     * @param listener the listener to invoke on mouse click
     * @return this {@code Designer} instance for chaining
     */
    public Designer onClick(EventListener<ClickEvent> listener) {
        this.component.addClickListener(listener);
        return this;
    }

    /**
     * Registers a value change event listener on the current component.
     *
     * @param listener the listener to invoke when the value changes
     * @return this {@code Designer} instance for chaining
     */
    public Designer onValueChange(EventListener<ValueChangeEvent> listener) {
        this.component.addValueChangeListener(listener);
        return this;
    }

    /**
     * Sets the most recently added component as the new parent for future additions.
     * Useful for nesting child elements under this component.
     *
     * @return this {@code Designer} instance for chaining
     */
    public Designer asParent() {
        this.parent = this.component;
        return this;
    }

    /**
     * Adds a new {@link Div} component to the current parent and sets it as the current component.
     *
     * @return this {@code Designer} instance for chaining
     */
    public Designer div() {
        this.setComponent(new Div());
        return this;
    }

    /**
     * Adds a new {@link Label} with the given text to the current parent and sets it as the current component.
     *
     * @param text the label text
     * @return this {@code Designer} instance for chaining
     */
    public Designer label(String text) {
        this.setComponent(new Label(text));
        return this;
    }

    /**
     * Adds a new {@link H1} heading with the given text to the current parent and sets it as the current component.
     *
     * @param text the heading text
     * @return this {@code Designer} instance for chaining
     */
    public Designer h1(String text) {
        this.setComponent(new H1(text));
        return this;
    }

    /**
     * Adds a new {@link Checkbox} with the specified checked state to the current parent.
     *
     * @param checked whether the checkbox is initially checked
     * @return this {@code Designer} instance for chaining
     */
    public Designer checkbox(boolean checked) {
        this.setComponent(new Checkbox(checked));
        return this;
    }

    /**
     * Adds an unchecked {@link Checkbox} to the current parent and sets it as the current component.
     *
     * @return this {@code Designer} instance for chaining
     */
    public Designer checkbox() {
        this.setComponent(new Checkbox());
        return this;
    }

    /**
     * Adds a new {@link Button} with the specified text to the current parent and sets it as the current component.
     *
     * @param text the button label
     * @return this {@code Designer} instance for chaining
     */
    public Designer button(String text) {
        this.setComponent(new Button(text));
        return this;
    }

    /**
     * Adds an empty {@link PasswordField} to the current parent and sets it as the current component.
     *
     * @return this {@code Designer} instance for chaining
     */
    public Designer password() {
        this.setComponent(new PasswordField());
        return this;
    }

    /**
     * Adds a {@link PasswordField} with a placeholder to the current parent and sets it as the current component.
     *
     * @param placeholder the placeholder text
     * @return this {@code Designer} instance for chaining
     */
    public Designer password(String placeholder) {
        this.setComponent(new PasswordField(placeholder));
        return this;
    }

    /**
     * Adds an empty {@link TextField} to the current parent and sets it as the current component.
     *
     * @return this {@code Designer} instance for chaining
     */
    public Designer textField() {
        this.setComponent(new TextField());
        return this;
    }

    /**
     * Adds a {@link TextField} with a placeholder to the current parent and sets it as the current component.
     *
     * @param placeholder the placeholder text
     * @return this {@code Designer} instance for chaining
     */
    public Designer textField(String placeholder) {
        this.setComponent(new TextField(placeholder));
        return this;
    }

    /**
     * Adds a {@link TextField} with a placeholder and initial value to the current parent.
     *
     * @param placeholder the placeholder text
     * @param value       the initial text value
     * @return this {@code Designer} instance for chaining
     */
    public Designer textField(String placeholder, String value) {
        this.setComponent(new TextField(placeholder, value));
        return this;
    }

    /**
     * Sets the value of the current component if it implements {@link ValueComponent}.
     *
     * @param value the value to assign
     * @param <T>   the type of the value
     * @return this {@code Designer} instance for chaining
     * @throws UnsupportedOperationException if the component does not support value setting
     */
    public <T> Designer value(T value) {
        if (component instanceof ValueComponent component)
            component.setValue(value);
        else
            throw new UnsupportedOperationException("setValue() not supported for component: " + component.getClass().getSimpleName());
        return this;
    }

    /**
     * Sets the CSS {@code display} property of the current component.
     *
     * @param display the {@link Display} value to apply (e.g. FLEX, BLOCK)
     * @return this {@code Designer} instance for chaining
     */
    public Designer display(Display display) {
        this.component.getStyle().set("display", display.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code flex-direction} property, defining the direction of flexible items.
     *
     * @param flexDirection the {@link FlexDirection} value (e.g. ROW, COLUMN)
     * @return this {@code Designer} instance for chaining
     */
    public Designer flexDirection(FlexDirection flexDirection) {
        this.component.getStyle().set("flex-direction", flexDirection.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code flex-wrap} property to control whether flex items wrap.
     *
     * @param flexWrap the {@link FlexWrap} value (e.g. WRAP, NOWRAP)
     * @return this {@code Designer} instance for chaining
     */
    public Designer flexWrap(FlexWrap flexWrap) {
        this.component.getStyle().set("flex-wrap", flexWrap.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code flex-grow} property to define how much a flex item grows.
     *
     * @param flexGrow the {@link FlexGrow} value to apply
     * @return this {@code Designer} instance for chaining
     */
    public Designer flexGrow(FlexGrow flexGrow) {
        this.component.getStyle().set("flex-grow", flexGrow.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code flex-shrink} property to define how much a flex item shrinks.
     *
     * @param flexShrink the {@link FlexShrink} value to apply
     * @return this {@code Designer} instance for chaining
     */
    public Designer flexShrink(FlexShrink flexShrink) {
        this.component.getStyle().set("flex-shrink", flexShrink.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code flex} shorthand property for grow, shrink, and basis.
     *
     * @param flex the {@link Flex} value (e.g. "1 0 auto")
     * @return this {@code Designer} instance for chaining
     */
    public Designer flex(Flex flex) {
        this.component.getStyle().set("flex", flex.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code align-items} property to align items along the cross axis.
     *
     * @param alignItems the {@link AlignItems} value (e.g. CENTER, FLEX_START)
     * @return this {@code Designer} instance for chaining
     */
    public Designer alignItems(AlignItems alignItems) {
        this.component.getStyle().set("align-items", alignItems.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code align-self} property to align the item on the cross axis independently.
     *
     * @param alignSelf the {@link AlignSelf} value (e.g. STRETCH, BASELINE)
     * @return this {@code Designer} instance for chaining
     */
    public Designer alignSelf(AlignSelf alignSelf) {
        this.component.getStyle().set("align-self", alignSelf.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code justify-content} property to align items along the main axis.
     *
     * @param justifyContent the {@link JustifyContent} value (e.g. SPACE_BETWEEN, CENTER)
     * @return this {@code Designer} instance for chaining
     */
    public Designer justifyContent(JustifyContent justifyContent) {
        this.component.getStyle().set("justify-content", justifyContent.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code align-content} property for multi-line flex containers.
     *
     * @param alignContent the {@link AlignContent} value (e.g. SPACE_AROUND, FLEX_END)
     * @return this {@code Designer} instance for chaining
     */
    public Designer alignContent(AlignContent alignContent) {
        this.component.getStyle().set("align-content", alignContent.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code margin} property for the current component.
     *
     * @param value the margin value (e.g. "10px", "1em auto", "0")
     * @return this {@code Designer} instance for chaining
     */
    public Designer margin(String value) {
        this.component.getStyle().set("margin", value);
        return this;
    }

    /**
     * Sets the CSS {@code padding} property for the current component.
     *
     * @param value the padding value (e.g. "1em", "10px 20px", "0")
     * @return this {@code Designer} instance for chaining
     */
    public Designer padding(String value) {
        this.component.getStyle().set("padding", value);
        return this;
    }

    /**
     * Sets the CSS {@code width} property for the current component.
     *
     * @param value the width value (e.g. "100%", "250px", "auto")
     * @return this {@code Designer} instance for chaining
     */
    public Designer width(String value) {
        this.component.getStyle().set("width", value);
        return this;
    }

    /**
     * Sets the CSS {@code height} property for the current component.
     *
     * @param value the height value (e.g. "100vh", "2rem", "auto")
     * @return this {@code Designer} instance for chaining
     */
    public Designer height(String value) {
        this.component.getStyle().set("height", value);
        return this;
    }

    /**
     * Sets the CSS {@code font-weight} property for the current component.
     *
     * @param fontWeight the {@link FontWeight} enum value (e.g. BOLD, NORMAL)
     * @return this {@code Designer} instance for chaining
     */
    public Designer fontWeight(FontWeight fontWeight) {
        this.component.getStyle().set("font-weight", fontWeight.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code text-align} property for the current component.
     *
     * @param textAlign the {@link TextAlign} enum value (e.g. LEFT, CENTER, RIGHT)
     * @return this {@code Designer} instance for chaining
     */
    public Designer textAlign(TextAlign textAlign) {
        this.component.getStyle().set("text-align", textAlign.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code visibility} property for the current component.
     *
     * @param visibility the {@link Visibility} enum value (e.g. VISIBLE, HIDDEN)
     * @return this {@code Designer} instance for chaining
     */
    public Designer visibility(Visibility visibility) {
        this.component.getStyle().set("visibility", visibility.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code position} property for the current component.
     *
     * @param position the {@link Position} enum value (e.g. RELATIVE, ABSOLUTE)
     * @return this {@code Designer} instance for chaining
     */
    public Designer position(Position position) {
        this.component.getStyle().set("position", position.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code overflow} property for the current component.
     *
     * @param overflow the {@link Overflow} enum value (e.g. AUTO, HIDDEN, SCROLL)
     * @return this {@code Designer} instance for chaining
     */
    public Designer overflow(Overflow overflow) {
        this.component.getStyle().set("overflow", overflow.getValue());
        return this;
    }

    /**
     * Sets the CSS {@code border-radius} property for the current component.
     *
     * @param value the border radius value (e.g. "5px", "50%", "1em")
     * @return this {@code Designer} instance for chaining
     */
    public Designer borderRadius(String value) {
        this.component.getStyle().set("border-radius", value);
        return this;
    }

    /**
     * Sets the CSS {@code border} shorthand property for the current component.
     *
     * @param value the border definition (e.g. "1px solid #ccc")
     * @return this {@code Designer} instance for chaining
     */
    public Designer border(String value) {
        this.component.getStyle().set("border", value);
        return this;
    }

    /**
     * Sets the CSS {@code gap} property for flex or grid spacing between items.
     *
     * @param value the gap value (e.g. "10px", "1rem")
     * @return this {@code Designer} instance for chaining
     */
    public Designer gap(String value) {
        this.component.getStyle().set("gap", value);
        return this;
    }

    /**
     * Sets the CSS {@code column-gap} property for the current component.
     *
     * @param value the gap between columns (e.g. "16px", "1em")
     * @return this {@code Designer} instance for chaining
     */
    public Designer columnGap(String value) {
        this.component.getStyle().set("column-gap", value);
        return this;
    }

    /**
     * Sets the CSS {@code row-gap} property for the current component.
     *
     * @param value the gap between rows (e.g. "10px", "2rem")
     * @return this {@code Designer} instance for chaining
     */
    public Designer rowGap(String value) {
        this.component.getStyle().set("row-gap", value);
        return this;
    }

    /**
     * Sets the CSS {@code grid-template-columns} property to define column structure.
     *
     * @param definition the column layout definition (e.g. "1fr 1fr", "repeat(3, 100px)")
     * @return this {@code Designer} instance for chaining
     */
    public Designer gridTemplateColumns(String definition) {
        this.component.getStyle().set("grid-template-columns", definition);
        return this;
    }

    /**
     * Sets the CSS {@code grid-template-rows} property to define row structure.
     *
     * @param definition the row layout definition (e.g. "auto 1fr", "repeat(2, 50px)")
     * @return this {@code Designer} instance for chaining
     */
    public Designer gridTemplateRows(String definition) {
        this.component.getStyle().set("grid-template-rows", definition);
        return this;
    }

    /**
     * Sets the CSS {@code grid-auto-flow} property, controlling how grid items are placed.
     *
     * @param value the auto-flow direction (e.g. "row", "column", "row dense")
     * @return this {@code Designer} instance for chaining
     */
    public Designer gridAutoFlow(String value) {
        this.component.getStyle().set("grid-auto-flow", value);
        return this;
    }

    /**
     * Sets the CSS {@code min-width} property to define the minimum width of the component.
     *
     * @param value the minimum width (e.g. "100px", "0")
     * @return this {@code Designer} instance for chaining
     */
    public Designer minWidth(String value) {
        this.component.getStyle().set("min-width", value);
        return this;
    }

    /**
     * Sets the CSS {@code max-width} property to define the maximum width of the component.
     *
     * @param value the maximum width (e.g. "600px", "100%")
     * @return this {@code Designer} instance for chaining
     */
    public Designer maxWidth(String value) {
        this.component.getStyle().set("max-width", value);
        return this;
    }

    /**
     * Sets the CSS {@code min-height} property to define the minimum height of the component.
     *
     * @param value the minimum height (e.g. "50px", "0")
     * @return this {@code Designer} instance for chaining
     */
    public Designer minHeight(String value) {
        this.component.getStyle().set("min-height", value);
        return this;
    }

    /**
     * Sets the CSS {@code max-height} property to define the maximum height of the component.
     *
     * @param value the maximum height (e.g. "300px", "100vh")
     * @return this {@code Designer} instance for chaining
     */
    public Designer maxHeight(String value) {
        this.component.getStyle().set("max-height", value);
        return this;
    }

    /**
     * Sets the CSS {@code box-sizing} property to control box model calculation.
     *
     * @param value the box sizing mode (e.g. "border-box", "content-box")
     * @return this {@code Designer} instance for chaining
     */
    public Designer boxSizing(String value) {
        this.component.getStyle().set("box-sizing", value);
        return this;
    }

    /**
     * Sets the CSS {@code cursor} property to control the mouse cursor type.
     *
     * @param value the cursor value (e.g. "pointer", "move", "not-allowed")
     * @return this {@code Designer} instance for chaining
     */
    public Designer cursor(String value) {
        this.component.getStyle().set("cursor", value);
        return this;
    }

    /**
     * Sets the CSS {@code opacity} property to define the transparency level.
     *
     * @param value the opacity value (e.g. "1", "0.5", "0")
     * @return this {@code Designer} instance for chaining
     */
    public Designer opacity(String value) {
        this.component.getStyle().set("opacity", value);
        return this;
    }

    /**
     * Sets the CSS {@code z-index} property to control stack order.
     *
     * @param value the z-index value (e.g. "1", "999", "auto")
     * @return this {@code Designer} instance for chaining
     */
    public Designer zIndex(String value) {
        this.component.getStyle().set("z-index", value);
        return this;
    }

    /**
     * Sets the CSS {@code top} property for positioning.
     *
     * @param value the top offset (e.g. "10px", "5%", "auto")
     * @return this {@code Designer} instance for chaining
     */
    public Designer top(String value) {
        this.component.getStyle().set("top", value);
        return this;
    }

    /**
     * Sets the CSS {@code right} property for positioning.
     *
     * @param value the right offset
     * @return this {@code Designer} instance for chaining
     */
    public Designer right(String value) {
        this.component.getStyle().set("right", value);
        return this;
    }

    /**
     * Sets the CSS {@code outline} property.
     *
     * @param value the outline definition (e.g. "1px solid red")
     * @return this {@code Designer} instance for chaining
     */
    public Designer outline(String value) {
        this.component.getStyle().set("outline", value);
        return this;
    }

    /**
     * Sets the CSS {@code outline-offset} property.
     *
     * @param value the outline offset value (e.g. "2px", "0.5em")
     * @return this {@code Designer} instance for chaining
     */
    public Designer outlineOffset(String value) {
        this.component.getStyle().set("outline-offset", value);
        return this;
    }

    /**
     * Sets the CSS {@code box-shadow} property to apply shadow effects.
     *
     * @param value the shadow definition (e.g. "0 2px 4px rgba(0,0,0,0.1)")
     * @return this {@code Designer} instance for chaining
     */
    public Designer boxShadow(String value) {
        this.component.getStyle().set("box-shadow", value);
        return this;
    }

    /**
     * Sets the CSS {@code background} shorthand property.
     *
     * @param value the background definition (e.g. "#f00", "url(...)", "linear-gradient(...)")
     * @return this {@code Designer} instance for chaining
     */
    public Designer background(String value) {
        this.component.getStyle().set("background", value);
        return this;
    }

    /**
     * Sets the CSS {@code background-color} property.
     *
     * @param value the background color (e.g. "#fff", "rgba(0,0,0,0.5)")
     * @return this {@code Designer} instance for chaining
     */
    public Designer backgroundColor(String value) {
        this.component.getStyle().set("background-color", value);
        return this;
    }

    /**
     * Sets the CSS {@code bottom} property for positioning.
     *
     * @param value the bottom offset
     * @return this {@code Designer} instance for chaining
     */
    public Designer bottom(String value) {
        this.component.getStyle().bottom(value);
        return this;
    }

    /**
     * Sets the CSS {@code left} property for positioning.
     *
     * @param value the left offset
     * @return this {@code Designer} instance for chaining
     */
    public Designer left(String value) {
        this.component.getStyle().left(value);
        return this;
    }

    /**
     * Sets the CSS {@code font-size} property.
     *
     * @param value the font size (e.g. "16px", "1.5em")
     * @return this {@code Designer} instance for chaining
     */
    public Designer fontSize(String value) {
        this.component.getStyle().fontSize(value);
        return this;
    }

    /**
     * Sets the CSS {@code font-family} property.
     *
     * @param value the font family name(s) (e.g. "Arial", "monospace", "'Open Sans'")
     * @return this {@code Designer} instance for chaining
     */
    public Designer fontFamily(String value) {
        this.component.getStyle().fontFamily(value);
        return this;
    }

    /**
     * Sets the CSS {@code font-style} property.
     *
     * @param fontStyle the {@link FontStyle} enum value (e.g. ITALIC, NORMAL)
     * @return this {@code Designer} instance for chaining
     */
    public Designer fontStyle(FontStyle fontStyle) {
        this.component.getStyle().fontStyle(fontStyle);
        return this;
    }

    /**
     * Sets the CSS {@code line-height} property.
     *
     * @param value the line height (e.g. "1.5", "20px")
     * @return this {@code Designer} instance for chaining
     */
    public Designer lineHeight(String value) {
        this.component.getStyle().lineHeight(value);
        return this;
    }

    /**
     * Sets the CSS {@code letter-spacing} property to adjust spacing between characters.
     *
     * @param value the spacing value (e.g. "0.05em", "2px")
     * @return this {@code Designer} instance for chaining
     */
    public Designer letterSpacing(String value) {
        this.component.getStyle().letterSpacing(value);
        return this;
    }

    /**
     * Sets the CSS {@code word-spacing} property to adjust spacing between words.
     *
     * @param value the spacing value (e.g. "0.3em", "5px")
     * @return this {@code Designer} instance for chaining
     */
    public Designer wordSpacing(String value) {
        this.component.getStyle().wordSpacing(value);
        return this;
    }

    /**
     * Sets the CSS {@code text-decoration} property to apply text decorations.
     *
     * @param decoration the {@link TextDecoration} value (e.g. UNDERLINE, NONE)
     * @return this {@code Designer} instance for chaining
     */
    public Designer textDecoration(TextDecoration decoration) {
        this.component.getStyle().textDecoration(decoration);
        return this;
    }

    /**
     * Sets the CSS {@code text-transform} property to control text capitalization.
     *
     * @param transform the {@link TextTransform} value (e.g. UPPERCASE, LOWERCASE)
     * @return this {@code Designer} instance for chaining
     */
    public Designer textTransform(TextTransform transform) {
        this.component.getStyle().textTransform(transform);
        return this;
    }

    /**
     * Sets the CSS {@code white-space} property to control whitespace and line wrapping.
     *
     * @param whiteSpace the {@link WhiteSpace} value (e.g. PRE_WRAP, NOWRAP)
     * @return this {@code Designer} instance for chaining
     */
    public Designer whiteSpace(WhiteSpace whiteSpace) {
        this.component.getStyle().whiteSpace(whiteSpace);
        return this;
    }

    /**
     * Sets the CSS {@code text-overflow} property to define overflow behavior for text.
     *
     * @param value the overflow value (e.g. "ellipsis", "clip")
     * @return this {@code Designer} instance for chaining
     */
    public Designer textOverflow(String value) {
        this.component.getStyle().textOverflow(value);
        return this;
    }

    /**
     * Sets the CSS {@code transform} property to apply transformations to the element.
     *
     * @param value the transform value (e.g. "rotate(45deg)", "scale(1.2)")
     * @return this {@code Designer} instance for chaining
     */
    public Designer transform(String value) {
        this.component.getStyle().transform(value);
        return this;
    }

    /**
     * Sets the CSS {@code transition} property to animate changes in CSS properties.
     *
     * @param value the transition value (e.g. "all 0.3s ease")
     * @return this {@code Designer} instance for chaining
     */
    public Designer transition(String value) {
        this.component.getStyle().transition(value);
        return this;
    }

    /**
     * Sets the CSS {@code animation} property to apply animations.
     *
     * @param value the animation definition (e.g. "fade-in 1s ease-in")
     * @return this {@code Designer} instance for chaining
     */
    public Designer animation(String value) {
        this.component.getStyle().animation(value);
        return this;
    }

    /**
     * Sets the CSS {@code filter} property to apply visual effects (e.g., blur, brightness).
     *
     * @param value the filter definition (e.g. "blur(5px)", "brightness(0.8)")
     * @return this {@code Designer} instance for chaining
     */
    public Designer filter(String value) {
        this.component.getStyle().filter(value);
        return this;
    }

    /**
     * Sets the CSS {@code pointer-events} property to control mouse interaction.
     *
     * @param pe the {@link PointerEvents} enum value (e.g. NONE, AUTO)
     * @return this {@code Designer} instance for chaining
     */
    public Designer pointerEvents(PointerEvents pe) {
        this.component.getStyle().pointerEvents(pe);
        return this;
    }

    /**
     * Sets the CSS {@code user-select} property to control text selection behavior.
     *
     * @param us the {@link UserSelect} enum value (e.g. NONE, TEXT)
     * @return this {@code Designer} instance for chaining
     */
    public Designer userSelect(UserSelect us) {
        this.component.getStyle().userSelect(us);
        return this;
    }

    /**
     * Sets the CSS {@code overflow-x} property to control horizontal overflow behavior.
     *
     * @param overflow the {@link Overflow} value (e.g. SCROLL, HIDDEN)
     * @return this {@code Designer} instance for chaining
     */
    public Designer overflowX(Overflow overflow) {
        this.component.getStyle().overflowX(overflow);
        return this;
    }

    /**
     * Sets the CSS {@code overflow-y} property to control vertical overflow behavior.
     *
     * @param overflow the {@link Overflow} value (e.g. SCROLL, AUTO)
     * @return this {@code Designer} instance for chaining
     */
    public Designer overflowY(Overflow overflow) {
        this.component.getStyle().overflowY(overflow);
        return this;
    }

    /**
     * Sets the CSS {@code column-count} property to specify the number of columns.
     *
     * @param count the number of columns (e.g. 2, 3)
     * @return this {@code Designer} instance for chaining
     */
    public Designer columnCount(int count) {
        this.component.getStyle().columnCount(count);
        return this;
    }

    /**
     * Sets the CSS {@code column-width} property to define the ideal column width.
     *
     * @param value the width of each column (e.g. "200px", "10em")
     * @return this {@code Designer} instance for chaining
     */
    public Designer columnWidth(String value) {
        this.component.getStyle().columnWidth(value);
        return this;
    }

    /**
     * Sets the CSS {@code column-rule} property to define a visual separator between columns.
     *
     * @param value the column rule style (e.g. "1px solid #ccc")
     * @return this {@code Designer} instance for chaining
     */
    public Designer columnRule(String value) {
        this.component.getStyle().columnRule(value);
        return this;
    }

    /**
     * Sets the CSS {@code list-style-type} property to control marker type in lists.
     *
     * @param type the {@link ListStyleType} value (e.g. DISC, DECIMAL)
     * @return this {@code Designer} instance for chaining
     */
    public Designer listStyleType(ListStyleType type) {
        this.component.getStyle().listStyleType(type);
        return this;
    }

    /**
     * Sets the CSS {@code list-style-position} property to control marker positioning in lists.
     *
     * @param position the {@link ListStylePosition} value (e.g. INSIDE, OUTSIDE)
     * @return this {@code Designer} instance for chaining
     */
    public Designer listStylePosition(ListStylePosition position) {
        this.component.getStyle().listStylePosition(position);
        return this;
    }

    /**
     * Sets the CSS {@code list-style-image} property to use an image as a list item marker.
     *
     * @param value the URL or keyword for the image (e.g. "url(icon.png)")
     * @return this {@code Designer} instance for chaining
     */
    public Designer listStyleImage(String value) {
        this.component.getStyle().listStyleImage(value);
        return this;
    }

    /**
     * Sets the CSS {@code transform-origin} property to change the point of origin for transformations.
     *
     * @param value the origin point (e.g. "center", "top left", "50% 50%")
     * @return this {@code Designer} instance for chaining
     */
    public Designer transformOrigin(String value) {
        this.component.getStyle().transformOrigin(value);
        return this;
    }

    /**
     * Sets the CSS {@code backdrop-filter} property to apply graphical effects behind the element.
     *
     * @param value the filter effect (e.g. "blur(10px)", "brightness(0.8)")
     * @return this {@code Designer} instance for chaining
     */
    public Designer backdropFilter(String value) {
        this.component.getStyle().backdropFilter(value);
        return this;
    }

    /**
     * Sets the CSS {@code mix-blend-mode} property to define how content blends with the background.
     *
     * @param mode the {@link MixBlendMode} value (e.g. MULTIPLY, SCREEN)
     * @return this {@code Designer} instance for chaining
     */
    public Designer mixBlendMode(MixBlendMode mode) {
        this.component.getStyle().mixBlendMode(mode);
        return this;
    }

    /**
     * Sets the CSS {@code scroll-behavior} property to control smooth scrolling.
     *
     * @param behavior the {@link ScrollBehavior} value (e.g. SMOOTH, AUTO)
     * @return this {@code Designer} instance for chaining
     */
    public Designer scrollBehavior(ScrollBehavior behavior) {
        this.component.getStyle().scrollBehavior(behavior);
        return this;
    }

    /**
     * Sets the CSS {@code scroll-snap-type} property to define snap behavior on scroll containers.
     *
     * @param value the snap type (e.g. "x mandatory", "y proximity")
     * @return this {@code Designer} instance for chaining
     */
    public Designer scrollSnapType(String value) {
        this.component.getStyle().scrollSnapType(value);
        return this;
    }

    /**
     * Sets the CSS {@code scroll-snap-align} property to define alignment for scroll snap items.
     *
     * @param value the alignment value (e.g. "start", "center", "end")
     * @return this {@code Designer} instance for chaining
     */
    public Designer scrollSnapAlign(String value) {
        this.component.getStyle().scrollSnapAlign(value);
        return this;
    }

    /**
     * Sets the CSS {@code background-image} property.
     *
     * @param value the image source (e.g. "url('image.png')", "none", "linear-gradient(...)")
     * @return this {@code Designer} instance for chaining
     */
    public Designer backgroundImage(String value) {
        this.component.getStyle().backgroundImage(value);
        return this;
    }

    /**
     * Sets the CSS {@code background-position} property to position the background image.
     *
     * @param value the position value (e.g. "center", "top left", "50% 50%")
     * @return this {@code Designer} instance for chaining
     */
    public Designer backgroundPosition(String value) {
        this.component.getStyle().backgroundPosition(value);
        return this;
    }

    /**
     * Sets the CSS {@code background-size} property to control background scaling.
     *
     * @param value the size (e.g. "cover", "contain", "100% 100%")
     * @return this {@code Designer} instance for chaining
     */
    public Designer backgroundSize(String value) {
        this.component.getStyle().backgroundSize(value);
        return this;
    }

    /**
     * Sets the CSS {@code background-repeat} property to define if/how background images repeat.
     *
     * @param repeat the {@link BackgroundRepeat} value (e.g. NO_REPEAT, REPEAT_X)
     * @return this {@code Designer} instance for chaining
     */
    public Designer backgroundRepeat(BackgroundRepeat repeat) {
        this.component.getStyle().backgroundRepeat(repeat);
        return this;
    }

    /**
     * Sets the CSS {@code background-attachment} property to define how background images scroll.
     *
     * @param attachment the {@link BackgroundAttachment} value (e.g. FIXED, SCROLL)
     * @return this {@code Designer} instance for chaining
     */
    public Designer backgroundAttachment(BackgroundAttachment attachment) {
        this.component.getStyle().backgroundAttachment(attachment);
        return this;
    }

    /**
     * Sets the CSS {@code grid-auto-rows} property to define the size of implicitly created rows.
     *
     * @param value the row size (e.g. "100px", "min-content")
     * @return this {@code Designer} instance for chaining
     */
    public Designer gridAutoRows(String value) {
        this.component.getStyle().gridAutoRows(value);
        return this;
    }

    /**
     * Sets the CSS {@code grid-auto-columns} property to define the size of implicitly created columns.
     *
     * @param value the column size (e.g. "1fr", "auto")
     * @return this {@code Designer} instance for chaining
     */
    public Designer gridAutoColumns(String value) {
        this.component.getStyle().gridAutoColumns(value);
        return this;
    }

    /**
     * Sets the CSS {@code grid-row} property to control row placement in a grid.
     *
     * @param value the row definition (e.g. "1 / span 2")
     * @return this {@code Designer} instance for chaining
     */
    public Designer gridRow(String value) {
        this.component.getStyle().gridRow(value);
        return this;
    }

    /**
     * Sets the CSS {@code grid-column} property to control column placement in a grid.
     *
     * @param value the column definition (e.g. "2 / span 3")
     * @return this {@code Designer} instance for chaining
     */
    public Designer gridColumn(String value) {
        this.component.getStyle().gridColumn(value);
        return this;
    }

    /**
     * Sets the CSS {@code grid-area} property to assign the component to a named area in the grid.
     *
     * @param value the grid area name or definition
     * @return this {@code Designer} instance for chaining
     */
    public Designer gridArea(String value) {
        this.component.getStyle().gridArea(value);
        return this;
    }

    /**
     * Sets the CSS {@code justify-items} property to align grid items within their cells.
     *
     * @param items the {@link JustifyItems} value (e.g. START, CENTER, STRETCH)
     * @return this {@code Designer} instance for chaining
     */
    public Designer justifyItems(JustifyItems items) {
        this.component.getStyle().justifyItems(items);
        return this;
    }

    /**
     * Sets the CSS {@code justify-self} property to align the component itself within its grid cell.
     *
     * @param self the {@link JustifySelf} value (e.g. END, AUTO, CENTER)
     * @return this {@code Designer} instance for chaining
     */
    public Designer justifySelf(JustifySelf self) {
        this.component.getStyle().justifySelf(self);
        return this;
    }

    /**
     * Replaces the current component being constructed with the given one,
     * and adds it to the root parent component.
     *
     * @param component the new component to add and operate on
     */
    private void setComponent(Component component) {
        this.component = component;
        parent.add(component);
    }

    /**
     * Creates a new {@code Designer} instance starting from the given root parent component.
     *
     * @param parent the root {@link Component} to which all other components will be added
     * @return a new {@code Designer} instance
     */
    public static Designer begin(Component parent) {
        return new Designer(parent);
    }

}
