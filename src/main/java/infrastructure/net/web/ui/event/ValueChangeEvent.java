package infrastructure.net.web.ui.event;

import infrastructure.event.Event;
import infrastructure.net.web.ui.Component;

public class ValueChangeEvent extends Event {

    private final Component component;
    private final String oldValue;
    private final String newValue;

    public ValueChangeEvent(Component component, String oldValue, String newValue) {
        this.component = component;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Component getComponent() {
        return component;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }
}
