package infrastructure.web.ui;

/**
 * Represents all possible types of DOM updates that can be dispatched
 * from the server to the client to mutate the DOM or perform actions.
 */
public enum DOMUpdateType {

    // Creates a DOM element
    CREATE(0),

    // Basic content and property updates
    SET_TEXT(1),               // Sets textContent
    SET_HTML(2),               // Sets innerHTML
    SET_ATTRIBUTE(3),          // Sets an attribute
    SET_PROPERTY(4),           // Sets a DOM property
    SET_CLASS(5),              // Sets className
    SET_STYLE(6),              // Sets inline CSS style
    SET_VALUE(7),              // Sets value for input-like elements

    // DOM manipulation
    APPEND_CHILD(8),           // Appends a child element
    REMOVE(9),                 // Removes the current element
    INSERT_BEFORE(10),         // Inserts before this element
    INSERT_AFTER(11),          // Inserts after this element
    REPLACE(12),               // Replaces this element
    CLEAR_CHILDREN(13),        // Removes all children (e.g. innerHTML = "")

    // Events
    ADD_EVENT_LISTENER(14),    // Adds an event listener
    REMOVE_EVENT_LISTENER(15), // Removes an event listener (future support)
    TRIGGER_EVENT(16),         // Triggers a custom event

    // Advanced
    TOGGLE_CLASS(17),          // Toggles a class in classList
    SET_DATASET(18),           // Sets a data-* attribute
    FOCUS(19),                 // Focuses the element
    BLUR(20),                  // Unfocuses the element
    SCROLL_TO(21);             // Scrolls the element into view

    private final byte code;

    DOMUpdateType(int code) {
        this.code = (byte) code;
    }

    public static DOMUpdateType fromCode(int code) {
        for (DOMUpdateType type : values())
            if (type.code == code) return type;
        throw new IllegalArgumentException("Unknown DOMUpdateType code: " + code);
    }

    public int getCode() {
        return code;
    }
}
