package infrastructure.net.web.ui;

import infrastructure.event.EventListener;
import infrastructure.net.web.ui.components.Button;
import infrastructure.net.web.ui.components.TextField;
import infrastructure.net.web.ui.event.ClickEvent;
import infrastructure.net.web.ui.event.ValueChangeEvent;

public record Designer(Component component) {

    public Designer clickListener(EventListener<ClickEvent> listener) {
        this.component.addClickListener(listener);
        return this;
    }

    public Designer valueChangeListener(EventListener<ValueChangeEvent> listener) {
        this.component.addValueChangeListener(listener);
        return this;
    }

    public static Designer button(String text) {
        return new Designer(new Button(text));
    }

    public static Designer textField() {
        return new Designer(new TextField());
    }

    public static Designer textField(String placeholder) {
        return new Designer(new TextField(placeholder));
    }


}
