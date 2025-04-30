package infrastructure.web.ui.event;

import infrastructure.event.Event;
import infrastructure.web.ui.Component;

public class DetachEvent extends Event {

    private final Component component;

    public DetachEvent(Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }
}
