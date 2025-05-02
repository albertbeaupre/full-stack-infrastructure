package infrastructure.net.web.ui;

/**
 * The {@code EventType} enum defines a set of standard DOM-style UI events,
 * each associated with a unique integer identifier and a human-readable string
 * name (e.g., "click", "focus").
 *
 * <p>This enum is designed for systems that transmit UI event data efficiently.
 * The integer ID can be used for serialization, routing, or server-side event handling.
 */
public enum EventType {

    /** Represents a standard {@code click} event. */
    CLICK(1, "click"),

    /** Represents a {@code mouseover} or {@code hover} event. */
    HOVER(2, "hover"),

    /** Represents a {@code focus} event when an input receives focus. */
    FOCUS(3, "focus"),

    /** Represents a {@code blur} event when an input loses focus. */
    BLUR(4, "blur"),

    /** Represents a {@code change} event, typically on inputs or selects. */
    CHANGE(5, "change"),

    /** Represents an {@code input} event, triggered as the user types. */
    INPUT(6, "input"),

    /** Represents a {@code submit} event, usually from a form element. */
    SUBMIT(7, "submit"),

    /** Represents a {@code keydown} event when a key is pressed. */
    KEYDOWN(8, "keydown"),

    /** Represents a {@code keyup} event when a key is released. */
    KEYUP(9, "keyup"),

    /** Represents a {@code scroll} event, often on scrollable containers. */
    SCROLL(10, "scroll");

    /** Unique integer ID for the event type. */
    private final byte value;

    /** The human-readable event name (e.g., "click", "hover"). */
    private final String eventName;

    /**
     * Constructs an {@code EventType} with the given identifier and name.
     *
     * @param value       an integer ID representing the event type
     * @param eventName   a string name describing the event (used for logging or JS)
     */
    EventType(int value, String eventName) {
        this.value = (byte) value;
        this.eventName = eventName;
    }

    /**
     * Returns the integer ID for this event type.
     *
     * @return the numeric ID of the event
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the string event name for this type (e.g., "click").
     *
     * @return the human-readable event name
     */
    public String getName() {
        return eventName;
    }

    /**
     * Resolves an {@code EventType} from its integer ID.
     *
     * @param value the event ID
     * @return the matching {@code EventType}, or {@code null} if unknown
     */
    public static EventType fromValue(int value) {
        for (EventType e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        return null;
    }
}