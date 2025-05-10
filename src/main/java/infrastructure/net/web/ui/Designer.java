package infrastructure.net.web.ui;

import infrastructure.event.EventListener;
import infrastructure.net.web.ui.components.*;
import infrastructure.net.web.ui.css.*;
import infrastructure.net.web.ui.event.ClickEvent;
import infrastructure.net.web.ui.event.ValueChangeEvent;

public class Designer {

    private Component ui;
    private Component component;

    public Designer(Component ui) {
        this.ui = ui;
        this.component = ui;
    }

    public Designer onClick(EventListener<ClickEvent> listener) {
        this.component.addClickListener(listener);
        return this;
    }

    public Designer onValueChange(EventListener<ValueChangeEvent> listener) {
        this.component.addValueChangeListener(listener);
        return this;
    }

    public Designer asParent() {
        this.ui = this.component;
        return this;
    }

    public Designer div() {
        this.setComponent(new Div());
        return this;
    }

    public Designer label(String text) {
        this.setComponent(new Label(text));
        return this;
    }

    public Designer h1(String text) {
        this.setComponent(new H1(text));
        return this;
    }

    public Designer checkbox(boolean checked) {
        this.setComponent(new Checkbox(checked));
        return this;
    }

    public Designer checkbox() {
        this.setComponent(new Checkbox());
        return this;
    }

    public Designer button(String text) {
        this.setComponent(new Button(text));
        return this;
    }

    public Designer password() {
        this.setComponent(new PasswordField());
        return this;
    }

    public Designer password(String placeholder) {
        this.setComponent(new PasswordField(placeholder));
        return this;
    }

    public Designer textField() {
        this.setComponent(new TextField());
        return this;
    }

    public Designer textField(String placeholder) {
        this.setComponent(new TextField(placeholder));
        return this;
    }

    public Designer textField(String placeholder, String value) {
        this.setComponent(new TextField(placeholder, value));
        return this;
    }

    public <T> Designer value(T value) {
        if (component instanceof ValueComponent component)
            component.setValue(value);
        else
            throw new UnsupportedOperationException("setValue() not supported for component: " + component.getClass().getSimpleName());
        return this;
    }

    public Designer display(Display display) {
        this.component.getStyle().set("display", display.getValue());
        return this;
    }

    public Designer flexDirection(FlexDirection flexDirection) {
        this.component.getStyle().set("flex-direction", flexDirection.getValue());
        return this;
    }

    public Designer flexWrap(FlexWrap flexWrap) {
        this.component.getStyle().set("flex-wrap", flexWrap.getValue());
        return this;
    }

    public Designer flexGrow(FlexGrow flexGrow) {
        this.component.getStyle().set("flex-grow", flexGrow.getValue());
        return this;
    }

    public Designer flexShrink(FlexShrink flexShrink) {
        this.component.getStyle().set("flex-shrink", flexShrink.getValue());
        return this;
    }

    public Designer flex(Flex flex) {
        this.component.getStyle().set("flex", flex.getValue());
        return this;
    }

    public Designer alignItems(AlignItems alignItems) {
        this.component.getStyle().set("align-items", alignItems.getValue());
        return this;
    }

    public Designer alignSelf(AlignSelf alignSelf) {
        this.component.getStyle().set("align-self", alignSelf.getValue());
        return this;
    }

    public Designer justifyContent(JustifyContent justifyContent) {
        this.component.getStyle().set("justify-content", justifyContent.getValue());
        return this;
    }

    public Designer alignContent(AlignContent alignContent) {
        this.component.getStyle().set("align-content", alignContent.getValue());
        return this;
    }

    public Designer margin(String value) {
        this.component.getStyle().set("margin", value);
        return this;
    }

    public Designer padding(String value) {
        this.component.getStyle().set("padding", value);
        return this;
    }

    public Designer width(String value) {
        this.component.getStyle().set("width", value);
        return this;
    }

    public Designer height(String value) {
        this.component.getStyle().set("height", value);
        return this;
    }

    public Designer fontWeight(FontWeight fontWeight) {
        this.component.getStyle().set("font-weight", fontWeight.getValue());
        return this;
    }

    public Designer textAlign(TextAlign textAlign) {
        this.component.getStyle().set("text-align", textAlign.getValue());
        return this;
    }

    public Designer visibility(Visibility visibility) {
        this.component.getStyle().set("visibility", visibility.getValue());
        return this;
    }

    public Designer position(Position position) {
        this.component.getStyle().set("position", position.getValue());
        return this;
    }

    public Designer overflow(Overflow overflow) {
        this.component.getStyle().set("overflow", overflow.getValue());
        return this;
    }

    public Designer borderRadius(String value) {
        this.component.getStyle().set("border-radius", value);
        return this;
    }

    public Designer border(String value) {
        this.component.getStyle().set("border", value);
        return this;
    }

    public Designer gap(String value) {
        this.component.getStyle().set("gap", value);
        return this;
    }

    public Designer columnGap(String value) {
        this.component.getStyle().set("column-gap", value);
        return this;
    }

    public Designer rowGap(String value) {
        this.component.getStyle().set("row-gap", value);
        return this;
    }

    public Designer gridTemplateColumns(String definition) {
        this.component.getStyle().set("grid-template-columns", definition);
        return this;
    }

    public Designer gridTemplateRows(String definition) {
        this.component.getStyle().set("grid-template-rows", definition);
        return this;
    }

    public Designer gridAutoFlow(String value) {
        this.component.getStyle().set("grid-auto-flow", value);
        return this;
    }

    public Designer minWidth(String value) {
        this.component.getStyle().set("min-width", value);
        return this;
    }

    public Designer maxWidth(String value) {
        this.component.getStyle().set("max-width", value);
        return this;
    }

    public Designer minHeight(String value) {
        this.component.getStyle().set("min-height", value);
        return this;
    }

    public Designer maxHeight(String value) {
        this.component.getStyle().set("max-height", value);
        return this;
    }

    public Designer boxSizing(String value) {
        this.component.getStyle().set("box-sizing", value);
        return this;
    }

    public Designer cursor(String value) {
        this.component.getStyle().set("cursor", value);
        return this;
    }

    public Designer opacity(String value) {
        this.component.getStyle().set("opacity", value);
        return this;
    }

    public Designer zIndex(String value) {
        this.component.getStyle().set("z-index", value);
        return this;
    }

    public Designer top(String value) {
        this.component.getStyle().set("top", value);
        return this;
    }

    public Designer right(String value) {
        this.component.getStyle().set("right", value);
        return this;
    }

    public Designer outline(String value) {
        this.component.getStyle().set("outline", value);
        return this;
    }

    public Designer outlineOffset(String value) {
        this.component.getStyle().set("outline-offset", value);
        return this;
    }

    public Designer boxShadow(String value) {
        this.component.getStyle().set("box-shadow", value);
        return this;
    }

    public Designer background(String value) {
        this.component.getStyle().set("background", value);
        return this;
    }

    public Designer backgroundColor(String value) {
        this.component.getStyle().set("background-color", value);
        return this;
    }


    private void setComponent(Component component) {
        this.component = component;
        ui.add(component);
    }

    public static Designer begin(Component parent) {
        return new Designer(parent);
    }
}
