package infrastructure.net.web.ui;

/**
 * Enumerates the various parameters used to update DOM elements in a
 * compact and efficient manner. Each enum value represents a distinct type
 * of DOM update operation or attribute that can be modified.
 *
 * The associated integer ID for each enum value allows these parameters
 * to be encoded for efficient transmission or used in mapping operations.
 */
public enum DOMUpdateParam {

    TEXT(0),
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
    IDENTIFIER(16),
    TYPE(17);         // used for setting the element type update

    private final byte code;

    DOMUpdateParam(int code) {
        this.code = (byte) code;
    }

    public static DOMUpdateParam fromID(int id) {
        for (DOMUpdateParam param : values()) {
            if (param.code == id) return param;
        }
        throw new IllegalArgumentException("Unknown DOMUpdateParam id: " + id);
    }

    public int getCode() {
        return code;
    }
}