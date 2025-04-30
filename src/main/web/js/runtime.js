(() => {
    const SEND_NAVIGATE = 0, SEND_CLICK = 1, SEND_KEY_UP = 2, SEND_KEY_DOWN = 3, SEND_SUBMIT = 4;
    const RECEIVE_NAVIGATE = 0, RECEIVE_DOM_UPDATE = 1;
    const SET_TEXT = 1, SET_HTML = 2, SET_ATTRIBUTE = 3, SET_PROPERTY = 4, SET_CLASS = 5,
        SET_STYLE = 6, SET_VALUE = 7, APPEND_CHILD = 8, REMOVE = 9,
        INSERT_BEFORE = 10, INSERT_AFTER = 11, REPLACE = 12, CLEAR_CHILDREN = 13,
        ADD_EVENT = 14, REMOVE_EVENT = 15, TRIGGER_EVENT = 16, TOGGLE_CLASS = 17,
        SET_DATASET = 18, FOCUS = 19, BLUR = 20, SCROLL_TO = 21;
    const TEXT = 0, HTML = 1, KEY = 2, VALUE = 3, PROPERTY = 4, CLASS_NAME = 5,
        STYLE_PROP = 6, STYLE_VAL = 7, EVENT_NAME = 8, CLASS_TOGGLE = 9, FORCE = 10,
        DATASET_KEY = 11, DATASET_VAL = 12, TOP = 13, LEFT = 14, BEHAVIOR = 15, IDENTIFIER = 16;
    const enc = new TextEncoder(), dec = new TextDecoder(), packets_out = [], packets_in = [];

    packets_out[SEND_NAVIGATE] = (path) => {
        if (typeof path !== "string") return;
        const bytes = enc.encode(path), len = bytes.length;
        const buf = new Uint8Array(3 + len);
        buf[0] = SEND_NAVIGATE;
        buf[1] = (len >> 8) & 0xFF;
        buf[2] = len & 0xFF;
        buf.set(bytes, 3);
        return buf;
    };

    packets_out[SEND_CLICK] = (id, btn, cx, cy, px, py, sx, sy, ctrl, shift, alt, meta) => {
        const buf = new Uint8Array(20), dv = new DataView(buf.buffer);
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

    packets_out[SEND_KEY_UP] = (id, key, repeat, ctrl, shift, alt, meta) => {
        const keyBytes = enc.encode(key), len = keyBytes.length;
        const buf = new Uint8Array(10 + len);
        buf[0] = SEND_KEY_UP;
        buf[1] = (id >> 24) & 0xFF;
        buf[2] = (id >> 16) & 0xFF;
        buf[3] = (id >> 8) & 0xFF;
        buf[4] = id & 0xFF;
        buf[5] = repeat ? 1 : 0;
        buf[6] = (ctrl ? 1 : 0) | (shift ? 2 : 0) | (alt ? 4 : 0) | (meta ? 8 : 0);
        buf[7] = len >> 8;
        buf[8] = len & 0xFF;
        buf.set(keyBytes, 9);
        return buf;
    };

    packets_out[SEND_KEY_DOWN] = (id, key, repeat, ctrl, shift, alt, meta) => {
        const keyBytes = enc.encode(key), len = keyBytes.length;
        const buf = new Uint8Array(10 + len);
        buf[0] = SEND_KEY_DOWN;
        buf[1] = (id >> 24) & 0xFF;
        buf[2] = (id >> 16) & 0xFF;
        buf[3] = (id >> 8) & 0xFF;
        buf[4] = id & 0xFF;
        buf[5] = repeat ? 1 : 0;
        buf[6] = (ctrl ? 1 : 0) | (shift ? 2 : 0) | (alt ? 4 : 0) | (meta ? 8 : 0);
        buf[7] = len >> 8;
        buf[8] = len & 0xFF;
        buf.set(keyBytes, 9);
        return buf;
    };

    packets_in[RECEIVE_NAVIGATE] = (view) => {
        const len = view.getUint16(1);
        const path = dec.decode(new Uint8Array(view.buffer, view.byteOffset + 3, len));
        history.pushState({}, "", path);
    };

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

    // === DOM Manipulation Logic (minimal allocations) ===
    function updateDOM(type, id, params) {
        const el = document.getElementById(id);
        if (!el) return;

        switch (type) {
            case SET_TEXT:
                el.textContent = params[TEXT];
                break;
            case SET_HTML:
                el.innerHTML = params[HTML];
                break;
            case SET_ATTRIBUTE:
                el.setAttribute(params[KEY], params[VALUE]);
                break;
            case SET_PROPERTY:
                el[params[PROPERTY]] = params[VALUE];
                break;
            case SET_CLASS:
                el.className = params[CLASS_NAME];
                break;
            case SET_STYLE:
                el.style[params[STYLE_PROP]] = params[STYLE_VAL];
                break;
            case SET_VALUE:
                el.value = params[VALUE];
                break;
            case APPEND_CHILD:
            case INSERT_BEFORE:
            case INSERT_AFTER:
            case REPLACE: {
                const child = document.createElement(params[HTML]);
                child.id = params[IDENTIFIER];
                const parent = el.parentNode;
                if (!parent) return;
                if (type === APPEND_CHILD) el.appendChild(child);
                else if (type === INSERT_BEFORE) parent.insertBefore(child, el);
                else if (type === INSERT_AFTER) parent.insertBefore(child, el.nextSibling);
                else if (type === REPLACE) parent.replaceChild(child, el);
                break;
            }
            case REMOVE:
                el.remove();
                break;
            case CLEAR_CHILDREN:
                el.innerHTML = "";
                break;
            case ADD_EVENT: {
                const evt = params[EVENT_NAME];
                el.addEventListener(evt, (e) => {
                    const cid = parseInt(el.id);
                    if (evt === "click") sendPacket(SEND_CLICK, cid, e.button, e.clientX, e.clientY, e.pageX, e.pageY, e.screenX, e.screenY, e.ctrlKey, e.shiftKey, e.altKey, e.metaKey);
                    if (evt === "keyup") sendPacket(SEND_KEY_UP, cid, e.key, e.repeat, e.ctrlKey, e.shiftKey, e.altKey, e.metaKey);
                    if (evt === "keydown") sendPacket(SEND_KEY_DOWN, cid, e.key, e.repeat, e.ctrlKey, e.shiftKey, e.altKey, e.metaKey);
                });
                break;
            }
            case TRIGGER_EVENT:
                el.dispatchEvent(new Event(params[EVENT_NAME], {bubbles: true}));
                break;
            case TOGGLE_CLASS:
                el.classList.toggle(params[CLASS_TOGGLE], params[FORCE] === "1");
                break;
            case SET_DATASET:
                el.dataset[params[DATASET_KEY]] = params[DATASET_VAL];
                break;
            case FOCUS:
                el.focus();
                break;
            case BLUR:
                el.blur();
                break;
            case SCROLL_TO:
                el.scrollTo({
                    top: parseInt(params[TOP]),
                    left: parseInt(params[LEFT]),
                    behavior: params[BEHAVIOR] || 'auto'
                });
                break;
        }
    }

    // === Packet Sending (single entrypoint) ===
    function sendPacket(op, ...args) {
        const s = window.domSocket;
        if (s?.readyState === WebSocket.OPEN) {
            const fn = packets_out[op];
            if (fn) s.send(fn(...args));
        }
    }

    // === Initialize WebSocket and Routing ===
    window.addEventListener("DOMContentLoaded", () => {
        const socket = new WebSocket(`ws://${location.host}/ws`);
        socket.binaryType = "arraybuffer";
        window.domSocket = socket;

        socket.onopen = () => sendPacket(SEND_NAVIGATE, location.pathname);
        socket.onmessage = (e) => {
            const view = new DataView(e.data);
            const opcode = view.getUint8(0);
            const handler = packets_in[opcode];
            if (handler) handler(view);
        };

        document.body.addEventListener("click", e => {
            const link = e.target.closest("a[href]");
            if (link && link.origin === location.origin) {
                e.preventDefault();
                history.pushState({}, "", link.pathname);
                sendPacket(SEND_NAVIGATE, link.pathname);
            }
        });

        window.addEventListener("popstate", () => sendPacket(SEND_NAVIGATE, location.pathname));
    });
})();