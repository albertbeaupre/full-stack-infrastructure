/**
 * runtime.js
 *
 * Client-side runtime that:
 *  - Opens a WebSocket connection to the server (/ws endpoint).
 *  - Serializes user interactions (navigation, clicks, key events, form inputs, file uploads) into binary packets.
 *  - Sends those packets over the socket.
 *  - Receives binary packets from the server to update:
 *      • document title
 *      • cookies
 *      • history/navigation
 *      • arbitrary DOM changes (text, HTML, attributes, events, etc.).
 */
(() => {
    /**
     * Outgoing packet opcodes.
     * These codes identify the type of user action sent from client → server.
     */
    const SEND_NAVIGATE = 0;  // Send current path on load/popstate/link click
    const SEND_CLICK = 1;  // Send mouse click details (button, coords, modifiers)
    const SEND_KEY_UP = 2;  // Send key-up event (key, repeat, modifiers)
    const SEND_KEY_DOWN = 3;  // Send key-down event (key, repeat, modifiers)
    const SEND_VALUE_CHANGE = 4;  // Send input/change value (e.g., text, checkbox)
    const SEND_SUBMIT = 5;  // Send form submit event
    const SEND_FILE = 6;  // Send file contents (filename + binary data)

    /**
     * Incoming packet opcodes.
     * These codes identify the type of update sent from server → client.
     */
    const RECEIVE_NAVIGATE = 0;  // Instruct client to pushState() and update URL
    const RECEIVE_DOM_UPDATE = 1;  // Apply one or more dynamic DOM mutations
    const RECEIVE_COOKIE = 2;  // Set or update document.cookie
    const RECEIVE_TITLE = 3;  // Update document.title

    /** Replace the document’s title */
    const SET_TITLE = 0;
    /** Update an element’s textContent */
    const SET_TEXT = 1;
    /** Update an element’s innerHTML */
    const SET_HTML = 2;
    /** Set an attribute on an element */
    const SET_ATTRIBUTE = 3;
    /** Set a property on an element (e.g., checked, value) */
    const SET_PROPERTY = 4;
    /** Toggle an element’s className */
    const SET_CLASS = 5;
    /** Set an inline style property (name:value) */
    const SET_STYLE = 6;
    /** Update an element’s value (e.g., input value) */
    const SET_VALUE = 7;
    /** Append a newly created child node */
    const APPEND_CHILD = 8;
    /** Remove an existing node */
    const REMOVE = 9;
    /** Insert a node before another */
    const INSERT_BEFORE = 10;
    /** Insert a node after another */
    const INSERT_AFTER = 11;
    /** Replace one node with another */
    const REPLACE = 12;
    /** Remove all children from an element */
    const CLEAR_CHILDREN = 13;
    /** Add an event listener to an element */
    const ADD_EVENT = 14;
    /** Remove an event listener from an element */
    const REMOVE_EVENT = 15;
    /** Programmatically trigger an event on an element */
    const TRIGGER_EVENT = 16;
    /** Toggle a specific class on an element */
    const TOGGLE_CLASS = 17;
    /** Set a data-* attribute key */
    const SET_DATASET = 18;
    /** Focus an element */
    const FOCUS = 19;
    /** Blur (unfocus) an element */
    const BLUR = 20;
    /** Scroll an element into view (parameters: top, left, behavior) */
    const SCROLL_TO = 21;
    /** Add a class to an element’s classList */
    const ADD_CLASS = 22;
    /** Remove a class from an element’s classList */
    const REMOVE_CLASS = 23;
    /** Change an element’s type property (e.g., input type) */
    const SET_TYPE = 24;

    /** params[0] holds text values for mutations */
    const TEXT = 0;
    /** params[1] holds HTML strings for mutations */
    const HTML = 1;
    /** params[2] holds attribute/property keys */
    const KEY = 2;
    /** params[3] holds attribute/property values */
    const VALUE = 3;
    /** params[4] holds property names (for SET_PROPERTY) */
    const PROPERTY = 4;
    /** params[5] holds class names (for SET_CLASS, ADD_CLASS, REMOVE_CLASS) */
    const CLASS_NAME = 5;
    /** params[6] holds style property names */
    const STYLE_PROP = 6;
    /** params[7] holds style values */
    const STYLE_VAL = 7;
    /** params[8] holds event names (for ADD_EVENT, REMOVE_EVENT, TRIGGER_EVENT) */
    const EVENT_NAME = 8;
    /** params[9] holds boolean toggle flags */
    const CLASS_TOGGLE = 9;
    /** params[10] holds boolean “force” flags (for classList methods) */
    const FORCE = 10;
    /** params[11] holds data-* attribute keys */
    const DATASET_KEY = 11;
    /** params[12] holds data-* attribute values */
    const DATASET_VAL = 12;
    /** params[13] holds scroll-to “top” coordinate */
    const TOP = 13;
    /** params[14] holds scroll-to “left” coordinate */
    const LEFT = 14;
    /** params[15] holds scroll behavior (e.g., "smooth" or "auto") */
    const BEHAVIOR = 15;
    /** params[16] holds element identifiers (IDs) for certain ops */
    const IDENTIFIER = 16;
    /** params[17] holds generic “type” values */
    const TYPE = 17;

    /** cookieParams[0] = name */
    const COOKIE_NAME = 0;
    /** cookieParams[1] = value */
    const COOKIE_VALUE = 1;
    /** cookieParams[2] = path */
    const COOKIE_PATH = 2;
    /** cookieParams[3] = max-age (in seconds) */
    const COOKIE_MAX_AGE = 3;
    /** cookieParams[4] = expires (Date string) */
    const COOKIE_EXPIRES = 4;
    /** cookieParams[5] = domain */
    const COOKIE_DOMAIN = 5;
    /** cookieParams[6] = secure flag */
    const COOKIE_SECURE = 6;
    /** cookieParams[7] = HttpOnly flag */
    const COOKIE_HTTP_ONLY = 7;
    /** cookieParams[8] = SameSite policy ("Strict", "Lax", "None") */
    const COOKIE_SAME_SITE = 8;

    /** Set of boolean HTML attributes (no string value) */
    const BOOLEAN_PROPERTIES = new Set([
        "checked", "disabled", "selected",
        "readonly", "required", "autofocus", "multiple"
    ]);

    /** UTF-8 encoder for outgoing text */
    const enc = new TextEncoder();
    /** UTF-8 decoder for incoming text */
    const dec = new TextDecoder();
    /** Registry of functions that serialize outgoing packets */
    const packets_out = [];
    /** Registry of functions that parse incoming packets */
    const packets_in = [];


    /**
     * Serialize a navigation packet.
     * @param {string} path - The URL or route to navigate to.
     * @returns {Uint8Array|null} Packet: [opcode, lengthHigh, lengthLow, UTF-8 bytes...] or null if invalid.
     */
    packets_out[SEND_NAVIGATE] = (path) => {
        if (typeof path !== "string") return null;
        const bytes = enc.encode(path);
        const len = bytes.length;
        const buf = new Uint8Array(3 + len);
        buf[0] = SEND_NAVIGATE;
        buf[1] = (len >> 8) & 0xFF;
        buf[2] = len & 0xFF;
        buf.set(bytes, 3);
        return buf;
    };

    /**
     * Serialize a mouse-click packet.
     * @param {number} id       - Element identifier.
     * @param {number} btn      - Button (0=left,1=middle,2=right).
     * @param {number} cx       - Click X coordinate relative to element.
     * @param {number} cy       - Click Y coordinate relative to element.
     * @param {number} px       - Page X coordinate.
     * @param {number} py       - Page Y coordinate.
     * @param {number} sx       - Screen X coordinate.
     * @param {number} sy       - Screen Y coordinate.
     * @param {boolean} ctrl    - Ctrl key pressed.
     * @param {boolean} shift   - Shift key pressed.
     * @param {boolean} alt     - Alt key pressed.
     * @param {boolean} meta    - Meta key pressed.
     * @returns {Uint8Array} Packet: [opcode, id(4B), btn, cx(2B), cy(2B), px(2B), py(2B), sx(2B), sy(2B), modifiers].
     */
    packets_out[SEND_CLICK] = (id, btn, cx, cy, px, py, sx, sy, ctrl, shift, alt, meta) => {
        const buf = new Uint8Array(20);
        const dv = new DataView(buf.buffer);
        buf[0] = SEND_CLICK;
        dv.setUint32(1, id);
        buf[5] = btn;
        dv.setInt16(6, cx);
        dv.setInt16(8, cy);
        dv.setInt16(10, px);
        dv.setInt16(12, py);
        dv.setInt16(14, sx);
        dv.setInt16(16, sy);
        buf[18] = (ctrl ? 1 : 0) | (shift ? 2 : 0) | (alt ? 4 : 0) | (meta ? 8 : 0);
        return buf;
    };

    /**
     * Serialize a key-up packet.
     * @param {number} id       - Element identifier.
     * @param {string} key      - Key value (e.g., "Enter").
     * @param {boolean} repeat  - Whether key is repeating.
     * @param {boolean} ctrl    - Ctrl key pressed.
     * @param {boolean} shift   - Shift key pressed.
     * @param {boolean} alt     - Alt key pressed.
     * @param {boolean} meta    - Meta key pressed.
     * @returns {Uint8Array} Packet: [opcode, id(4B), repeatFlag, modifiers, keyLengthHigh, keyLengthLow, UTF-8 key bytes...].
     */
    packets_out[SEND_KEY_UP] = (id, key, repeat, ctrl, shift, alt, meta) => {
        const keyBytes = enc.encode(key);
        const len = keyBytes.length;
        const buf = new Uint8Array(9 + len);
        const dv = new DataView(buf.buffer);
        buf[0] = SEND_KEY_UP;
        dv.setUint32(1, id);
        buf[5] = repeat ? 1 : 0;
        buf[6] = (ctrl ? 1 : 0) | (shift ? 2 : 0) | (alt ? 4 : 0) | (meta ? 8 : 0);
        buf[7] = (len >> 8) & 0xFF;
        buf[8] = len & 0xFF;
        buf.set(keyBytes, 9);
        return buf;
    };

    /** Serialize a key-down packet (identical to key-up). */
    packets_out[SEND_KEY_DOWN] = packets_out[SEND_KEY_UP];

    /**
     * Serialize an input/change-value packet.
     * @param {number} id       - Element identifier.
     * @param {string} value    - New input value.
     * @returns {Uint8Array} Packet: [opcode, id(4B), valueLenHigh, valueLenLow, UTF-8 value bytes...].
     */
    packets_out[SEND_VALUE_CHANGE] = (id, value) => {
        const valueBytes = enc.encode(value);
        const len = valueBytes.length;
        const buf = new Uint8Array(7 + len);
        const dv = new DataView(buf.buffer);
        buf[0] = SEND_VALUE_CHANGE;
        dv.setUint32(1, id);
        dv.setUint16(5, len);
        buf.set(valueBytes, 7);
        return buf;
    };

    /**
     * Serialize a form-submit packet.
     * @param {number} id - Form element identifier.
     * @returns {Uint8Array} Packet: [opcode, id(4B)].
     */
    packets_out[SEND_SUBMIT] = (id) => {
        const buf = new Uint8Array(5);
        const dv = new DataView(buf.buffer);
        buf[0] = SEND_SUBMIT;
        dv.setUint32(1, id);
        return buf;
    };

    /**
     * Serialize a file-upload packet.
     * @param {number} id            - Input element identifier.
     * @param {string} fileName      - Name of the file.
     * @param {ArrayBuffer} arrayBuffer - File data.
     * @returns {Uint8Array} Packet: [opcode, id(4B), nameLenHigh, nameLenLow, fileLenHigh(4B)..., nameBytes..., fileBytes...].
     */
    packets_out[SEND_FILE] = (id, fileName, arrayBuffer) => {
        const nameBytes = enc.encode(fileName);
        const nameLen = nameBytes.length;
        const fileBytes = new Uint8Array(arrayBuffer);
        const fileLen = fileBytes.length;
        const buf = new Uint8Array(11 + nameLen + fileLen);
        const dv = new DataView(buf.buffer);
        buf[0] = SEND_FILE;
        dv.setUint32(1, id);
        dv.setUint16(5, nameLen);
        dv.setUint32(7, fileLen);
        buf.set(nameBytes, 11);
        buf.set(fileBytes, 11 + nameLen);
        return buf;
    };
    /**
     * Handle server‐sent title updates.
     * Packet format: [opcode=RECEIVE_TITLE][lenHigh][lenLow][UTF-8 title…]
     * @param {DataView} view - DataView over the received ArrayBuffer.
     */
    packets_in[RECEIVE_TITLE] = (view) => {
        const len = view.getUint16(1);
        document.title = dec.decode(new Uint8Array(view.buffer, view.byteOffset + 3, len));
    };

    /**
     * Handle server‐sent cookie instructions.
     * Packet format: [opcode=RECEIVE_COOKIE][paramCount][key][len][value]… repeated
     * @param {DataView} view
     */
    packets_in[RECEIVE_COOKIE] = (view) => {
        let offset = 1;
        const count = view.getUint8(offset++);
        const cookieArr = [];
        for (let i = 0; i < count; i++) {
            const key = view.getUint8(offset++);
            const len = view.getUint16(offset);
            offset += 2;
            const val = dec.decode(new Uint8Array(view.buffer, offset, len));
            offset += len;
            cookieArr[key] = val;
        }
        // Build cookie string: name=value; otherAttrs...
        let str = `${cookieArr[COOKIE_NAME]}=${cookieArr[COOKIE_VALUE]}`;
        if (cookieArr[COOKIE_PATH]) str += `; path=${cookieArr[COOKIE_PATH]}`;
        if (cookieArr[COOKIE_DOMAIN]) str += `; domain=${cookieArr[COOKIE_DOMAIN]}`;
        if (cookieArr[COOKIE_MAX_AGE]) str += `; max-age=${cookieArr[COOKIE_MAX_AGE]}`;
        if (cookieArr[COOKIE_EXPIRES]) str += `; expires=${cookieArr[COOKIE_EXPIRES]}`;
        if (cookieArr[COOKIE_SECURE]) str += "; secure";
        if (cookieArr[COOKIE_HTTP_ONLY]) str += "; HttpOnly";
        if (cookieArr[COOKIE_SAME_SITE]) str += `; SameSite=${cookieArr[COOKIE_SAME_SITE]}`;
        document.cookie = str;
    };

    /**
     * Handle server‐sent navigation commands.
     * Packet format: [opcode=RECEIVE_NAVIGATE][lenHigh][lenLow][UTF-8 path…]
     * @param {DataView} view
     */
    packets_in[RECEIVE_NAVIGATE] = (view) => {
        const len = view.getUint16(1);
        const path = dec.decode(new Uint8Array(view.buffer, view.byteOffset + 3, len));
        history.pushState({}, "", path);
    };

    /**
     * Handle server‐sent DOM mutations.
     * Packet format: [opcode=RECEIVE_DOM_UPDATE][countHigh][countLow]
     *   then repeated per mutation:
     *   [type][id(4B)][paramCount][key][lenHigh][lenLow][UTF-8 val…]…
     * @param {DataView} view
     */
    packets_in[RECEIVE_DOM_UPDATE] = (view) => {
        let offset = 1;
        const count = view.getUint16(offset);
        offset += 2;
        for (let i = 0; i < count; i++) {
            const type = view.getUint8(offset++);
            const id = view.getInt32(offset);
            offset += 4;
            const paramCount = view.getUint8(offset++);
            const params = {};
            for (let j = 0; j < paramCount; j++) {
                const key = view.getUint8(offset++);
                const len = view.getUint16(offset);
                offset += 2;
                params[key] = dec.decode(new Uint8Array(view.buffer, offset, len));
                offset += len;
            }
            updateDOM(type, id, params);
        }
    };

    /**
     * Apply a single DOM mutation based on type, element ID, and params.
     * @param {number} type - Mutation opcode (SET_TITLE, SET_TEXT, etc.).
     * @param {number} id   - Element’s DOM id attribute.
     * @param {Object} params - Parameters indexed by constants (TEXT, HTML, KEY, etc.).
     */
    function updateDOM(type, id, params) {
        const element = document.getElementById(id);
        console.info("type=" + type, "id=" + id, "params=" + JSON.stringify(params));
        if (!element) return;

        switch (type) {
            case SET_TITLE:
                document.title = params[TEXT];
                break;
            case SET_TEXT:
                element.textContent = params[TEXT];
                break;
            case SET_HTML:
                element.innerHTML = params[HTML];
                break;
            case SET_ATTRIBUTE:
                element.setAttribute(params[KEY], params[VALUE]);
                break;
            case SET_PROPERTY:
                const prop = params[PROPERTY];
                const value = params[VALUE];

                if (BOOLEAN_PROPERTIES.has(prop)) {
                    element[prop] = value === "true";
                } else {
                    element[prop] = value;
                }
                break;
            case SET_CLASS:
                element.className = params[CLASS_NAME];
                break;
            case SET_STYLE:
                element.style[params[STYLE_PROP]] = params[STYLE_VAL];
                break;
            case SET_VALUE:
                element.value = params[VALUE];
                break;
            case APPEND_CHILD:
            case INSERT_BEFORE:
            case INSERT_AFTER:
            case REPLACE: {
                const child = document.createElement(params[HTML]);
                child.id = params[IDENTIFIER];
                const parent = element.parentNode;
                if (!parent) return;
                if (type === APPEND_CHILD) element.appendChild(child); else if (type === INSERT_BEFORE) parent.insertBefore(child, element); else if (type === INSERT_AFTER) parent.insertBefore(child, element.nextSibling); else if (type === REPLACE) parent.replaceChild(child, element);
                break;
            }
            case REMOVE:
                element.remove();
                break;
            case CLEAR_CHILDREN:
                element.innerHTML = "";
                break;
            case ADD_EVENT: {
                const evt = params[EVENT_NAME];
                element.addEventListener(evt, (e) => {
                    const cid = parseInt(element.id);

                    switch (evt) {
                        case "click":
                            sendPacket(SEND_CLICK, cid, e.button, e.clientX, e.clientY, e.pageX, e.pageY, e.screenX, e.screenY, e.ctrlKey, e.shiftKey, e.altKey, e.metaKey);
                            break;
                        case "keyup":
                            sendPacket(SEND_KEY_UP, cid, e.key, e.repeat, e.ctrlKey, e.shiftKey, e.altKey, e.metaKey);
                            break;
                        case "keydown":
                            sendPacket(SEND_KEY_DOWN, cid, e.key, e.repeat, e.ctrlKey, e.shiftKey, e.altKey, e.metaKey);
                            break;
                        case "input": {
                            const isCheckbox = e.target.type === "checkbox";
                            const value = isCheckbox ? e.target.checked.toString() : e.target.value;
                            sendPacket(SEND_VALUE_CHANGE, cid, value);
                            break;
                        }
                        case "submit":
                            e.preventDefault();
                            sendPacket(SEND_SUBMIT, cid);
                            break;
                        case "change": {
                            console.log("CHANGING!")
                            if (element.type.toLowerCase() === "file" && element.files.length > 0) {
                                Array.from(element.files).forEach(file => {
                                    const reader = new FileReader();
                                    reader.onload = () => sendPacket(SEND_FILE, cid, file.name, reader.result);
                                    reader.readAsArrayBuffer(file);
                                });
                            }
                            break;
                        }

                    }

                });
                break;
            }
            case REMOVE_EVENT:
                element.removeEventListener(params[EVENT_NAME], null);
                break;
            case TRIGGER_EVENT:
                element.dispatchEvent(new Event(params[EVENT_NAME], {bubbles: true}));
                break;
            case TOGGLE_CLASS:
                element.classList.toggle(params[CLASS_TOGGLE], params[FORCE] === "1");
                break;
            case SET_DATASET:
                element.dataset[params[DATASET_KEY]] = params[DATASET_VAL];
                break;
            case FOCUS:
                element.focus();
                break;
            case BLUR:
                element.blur();
                break;
            case SCROLL_TO:
                element.scrollTo({
                    top: parseInt(params[TOP]), left: parseInt(params[LEFT]), behavior: params[BEHAVIOR] || 'auto'
                });
                break;
            case ADD_CLASS:
                element.classList.add(params[CLASS_NAME]);
                break;
            case REMOVE_CLASS:
                element.classList.remove(params[CLASS_NAME]);
                break;
            case SET_TYPE:
                element.type = params[TYPE];
                break;
        }
    }

    /**
     * Send a serialized packet to the server if the WebSocket is open.
     * @param {number} op       - Opcode of the packet to send (e.g., SEND_CLICK).
     * @param {...*} args       - Arguments for the corresponding packets_out[op] serializer.
     */
    function sendPacket(op, ...args) {
        const s = window.domSocket;
        if (s?.readyState === WebSocket.OPEN) {
            const fn = packets_out[op];
            if (fn) s.send(fn(...args));
        }
    }

    /**
     * Set up the WebSocket connection and global event hooks once the DOM is ready.
     */
    window.addEventListener("DOMContentLoaded", () => {
        // Open a binary‐mode WebSocket to the server’s /ws endpoint
        const socket = new WebSocket(`ws://${location.host}/ws`);
        socket.binaryType = "arraybuffer";
        window.domSocket = socket;

        /**
         * When connection opens, notify server of current path.
         */
        socket.onopen = () => {
            sendPacket(SEND_NAVIGATE, location.pathname);
        };

        /**
         * Dispatch incoming ArrayBuffer messages to the appropriate parser.
         * First byte is opcode, rest is payload.
         */
        socket.onmessage = (event) => {
            const view = new DataView(event.data);
            const opcode = view.getUint8(0);
            const handler = packets_in[opcode];
            if (typeof handler === "function") {
                handler(view);
            }
        };

        /**
         * Intercept in‐page link clicks to drive client‐side routing.
         * Only handle same‐origin <a> tags.
         */
        document.body.addEventListener("click", (e) => {
            const link = e.target.closest("a[href]");
            if (link && link.origin === location.origin) {
                e.preventDefault();
                history.pushState({}, "", link.pathname);
                sendPacket(SEND_NAVIGATE, link.pathname);
            }
        });

        /**
         * Listen for browser back/forward navigation and notify the server.
         */
        window.addEventListener("popstate", () => {
            sendPacket(SEND_NAVIGATE, location.pathname);
        });
    });
})();