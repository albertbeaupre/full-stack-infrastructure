package infrastructure.net.web.ui;

/**
 * Enum representing different types of updates that can be applied to a DOM element.
 * It provides a lightweight encoding mechanism using integer codes for efficient processing.
 * Each constant corresponds to a specific type of update or action that modifies the DOM.
 *
 * The main use of this enum is in encoding operations related to DOM modifications, 
 * such as creating elements, updating attributes, managing events, and handling structural changes.
 */
public enum DOMUpdateType {

    TITLE(0),                // Sets the document title

    // Basic content and property updates
    SET_TEXT(1),              // Sets the textContent of an element
    SET_HTML(2),              // Sets the innerHTML of an element
    SET_ATTRIBUTE(3),         // Sets a specific attribute of an element
    SET_PROPERTY(4),          // Sets a DOM property for an element
    SET_CLASS(5),             // Sets the className of an element
    SET_STYLE(6),             // Sets an inline CSS style for an element
    SET_VALUE(7),             // Sets the value property for input-like elements

    // DOM manipulation
    APPEND_CHILD(8),          // Appends a child element to the current element
    REMOVE(9),                // Removes the current element
    INSERT_BEFORE(10),        // Inserts an element before the current element
    INSERT_AFTER(11),         // Inserts an element after the current element
    REPLACE(12),              // Replaces the current element with another element
    CLEAR_CHILDREN(13),       // Removes all children of the current element (e.g. innerHTML = "")

    // Events
    ADD_EVENT_LISTENER(14),   // Adds an event listener to an element
    REMOVE_EVENT_LISTENER(15),// Removes an event listener from an element (future support)
    TRIGGER_EVENT(16),        // Triggers a custom event on an element

    // Advanced
    TOGGLE_CLASS(17),         // Toggles a class value in the classList of an element
    SET_DATASET(18),          // Sets a data-* attribute on an element
    FOCUS(19),                // Focuses the current element
    BLUR(20),                 // Removes focus from the current element
    SCROLL_TO(21),            // Scrolls to the specified position in an element
    ADD_CLASS(22),            // Adds a new class name to the classList of an element
    REMOVE_CLASS(23),         // Removes a class name from the classList of an element
    SET_TYPE(24);             // Sets the type attribute of an element (e.g., input type)

    /**
     * The integer-based code representing the update type. This enables efficient encoding and decoding.
     */
    private final byte code;

    /**
     * Constructor to initialize the DOM update type with its corresponding code.
     *
     * @param code an integer code representing the DOM update type
     */
    DOMUpdateType(int code) {
        this.code = (byte) code;
    }

    /**
     * Retrieves the corresponding DOMUpdateType for a specified integer code.
     *
     * @param code the integer code of the desired DOMUpdateType
     * @return the matching DOMUpdateType constant
     * @throws IllegalArgumentException if no matching DOMUpdateType is found
     */
    public static DOMUpdateType fromCode(int code) {
        for (DOMUpdateType type : values())
            if (type.code == code) return type;
        throw new IllegalArgumentException("Unknown DOMUpdateType code: " + code);
    }

    /**
     * Gets the integer code of the current DOMUpdateType.
     *
     * @return the code representing this DOM update type
     */
    public int getCode() {
        return code;
    }
}
