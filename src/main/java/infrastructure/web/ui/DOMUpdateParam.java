package infrastructure.web.ui;

/**
 * Represents all known DOM update parameter keys with associated compact binary IDs.
 * Used to optimize DOM update protocol over WebSocket.
 */
public enum DOMUpdateParam {

    TEXT(0),               // el.textContent
    HTML(1),               // el.innerHTML
    KEY(2),                // el.setAttribute(key, value)
    VALUE(3),              // el.value = value
    PROPERTY(4),           // el[property] = value
    CLASS_NAME(5),         // el.className = value
    STYLE_PROPERTY(6),     // el.style[property] = value
    STYLE_VALUE(7),        // value of style[property]
    EVENT_NAME(8),         // event type name (click, input, etc.)
    CLASS_TOGGLE(9),       // class name to toggle
    FORCE(10),             // force true/false toggle
    DATASET_KEY(11),       // data-* key
    DATASET_VALUE(12),     // value for dataset key
    SCROLL_TOP(13),        // scroll position
    SCROLL_LEFT(14),       // scroll position
    SCROLL_BEHAVIOR(15),   // smooth / auto
    IDENTIFIER(16);         // used for CREATE update

    private final byte ID;

    DOMUpdateParam(int ID) {
        this.ID = (byte) ID;
    }

    public static DOMUpdateParam fromId(int id) {
        for (DOMUpdateParam param : values()) {
            if (param.ID == id) return param;
        }
        throw new IllegalArgumentException("Unknown DOMUpdateParam id: " + id);
    }

    public int ID() {
        return ID;
    }
}