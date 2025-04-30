package infrastructure.web.ui;

import infrastructure.event.Event;
import infrastructure.event.EventListener;
import infrastructure.event.EventPublisher;
import infrastructure.web.ui.css.Style;
import infrastructure.web.ui.css.StyleChangeEvent;
import infrastructure.web.ui.event.*;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Base class representing a DOM component similar to Vaadin's Component abstraction.
 */
public abstract class Component implements EventListener<StyleChangeEvent> {

    private Element element;
    private final EventPublisher publisher = new EventPublisher();
    private final Style style;
    private UI ui;

    public Component() {
        this(new Element("div"));
    }

    public Component(Element element) {
        if (element == null) throw new IllegalArgumentException("Element must not be null");
        if (element.getComponent().isPresent())
            throw new IllegalStateException("Element already has a component bound");

        this.element = element;
        element.setComponent(this);
        this.style = new Style(this, "component-pending");
    }

    protected abstract void create();
    protected abstract void destroy();

    public void add(Component... components) {
        for (Component child : components) {
            this.getElement().appendChild(child.getElement());
        }
    }

    public void remove(Component... components) {
        for (Component child : components) {
            this.getElement().removeChild(child.getElement());
        }
    }

    public void removeFromParent() {
        this.getElement().removeFromParent();
    }

    public Optional<Component> getParent() {
        return this.getElement().getParent().flatMap(Element::getComponent);
    }

    public Stream<Component> getChildren() {
        return this.getElement().getChildren().stream()
                .map(el -> el.getComponent().orElse(null))
                .filter(Objects::nonNull);
    }

    public Optional<UI> getUI() {
        Element current = this.getElement();
        while (current != null) {
            Optional<Component> c = current.getComponent();
            if (c.isPresent() && c.get() instanceof UI) {
                return Optional.of((UI) c.get());
            }
            current = current.getParent().orElse(null);
        }
        return Optional.empty();
    }

    @Override
    public void handle(StyleChangeEvent event) {
        this.dispatch(
                new DOMUpdate(DOMUpdateType.SET_STYLE, getComponentID())
                        .param(DOMUpdateParam.STYLE_PROPERTY, event.getProperty())
                        .param(DOMUpdateParam.STYLE_VALUE, event.getValue())
        );
        push();
    }

    public void publish(Event event) {
        this.publisher.publish(event);
    }

    public void addKeyUpListener(EventListener<KeyUpEvent> listener) {
        this.publisher.register(KeyUpEvent.class, listener);
        dispatch(new DOMUpdate(DOMUpdateType.ADD_EVENT_LISTENER, getComponentID())
                .param(DOMUpdateParam.EVENT_NAME, "keyup"));
    }

    public void addKeyDownListener(EventListener<KeyDownEvent> listener) {
        this.publisher.register(KeyDownEvent.class, listener);
        dispatch(new DOMUpdate(DOMUpdateType.ADD_EVENT_LISTENER, getComponentID())
                .param(DOMUpdateParam.EVENT_NAME, "keydown"));
    }

    public void addClickListener(EventListener<ClickEvent> listener) {
        this.publisher.register(ClickEvent.class, listener);
        dispatch(new DOMUpdate(DOMUpdateType.ADD_EVENT_LISTENER, getComponentID())
                .param(DOMUpdateParam.EVENT_NAME, "click"));
    }

    protected void dispatch(DOMUpdate update) {
        getUI().ifPresent(ui -> ui.getDispatcher().queue(update));
    }

    protected void push() {
        getUI().ifPresent(UI::push);
    }

    public boolean isAttached() {
        return getElement().isAttached();
    }

    public int getComponentID() {
        return getElement().getNodeId();
    }

    public Style getStyle() {
        return style;
    }

    public Element getElement() {
        return element;
    }

    public abstract String tag();

    protected void onAttach(AttachEvent attachEvent) {
    }

    protected void onDetach(DetachEvent detachEvent) {
    }
}
