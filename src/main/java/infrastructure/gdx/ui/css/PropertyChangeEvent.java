package infrastructure.gdx.ui.css;

import infrastructure.event.Event;

public class PropertyChangeEvent extends Event {

    private final String property;
    private final String value;

    public PropertyChangeEvent(String property, String value) {
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
