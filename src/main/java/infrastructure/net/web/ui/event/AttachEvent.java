package infrastructure.net.web.ui.event;

import infrastructure.event.Event;
import infrastructure.net.web.ui.Component;

public class AttachEvent extends Event {

    private final Component component;

    public AttachEvent(Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }
}
