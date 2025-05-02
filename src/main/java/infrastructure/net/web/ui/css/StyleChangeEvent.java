package infrastructure.net.web.ui.css;

import infrastructure.event.Event;

public class StyleChangeEvent extends Event {

    private final String property;
    private final String value;

    public StyleChangeEvent(String property, String value) {
        this.property = property;
        this.value = value;
    }

    public String getProperty() {
        return property;
    }

    public String getValue() {
        return value;
    }
}
