package infrastructure.net.web.ui;

/**
 * The {@code JSKey} enum defines a comprehensive set of string constants
 * corresponding to values returned by JavaScript's {@code event.key} property
 * in {@code KeyboardEvent} handlers.
 *
 * <p>This enum can be used to map keypresses from the browser to named constants
 * in Java, making it easier to interpret and respond to keyboard input consistently
 * across your application.
 *
 * <p>Each constant maps directly to the {@code String} representation returned by
 * the browser when a specific key is pressed. For example, pressing the "a" key
 * returns {@code "a"}, and pressing the left arrow key returns {@code "ArrowLeft"}.
 *
 * <p>Use the {@link #fromName(String)} method to convert a raw key string from
 * JavaScript into a corresponding enum constant, and {@link #getKey()} to get
 * the string form of an enum constant.
 *
 * @author Albert Beaupre
 * @version 1.0
 * @since April 19, 2025
 */
public enum Key {

    // --- Alphanumeric keys ---
    a("a"), b("b"), c("c"), d("d"), e("e"), f("f"), g("g"), h("h"), i("i"), j("j"),
    k("k"), l("l"), m("m"), n("n"), o("o"), p("p"), q("q"), r("r"), s("s"), t("t"),
    u("u"), v("v"), w("w"), x("x"), y("y"), z("z"),
    A("A"), B("B"), C("C"), D("D"), E("E"), F("F"), G("G"), H("H"), I("I"), J("J"),
    K("K"), L("L"), M("M"), N("N"), O("O"), P("P"), Q("Q"), R("R"), S("S"), T("T"),
    U("U"), V("V"), W("W"), X("X"), Y("Y"), Z("Z"),
    ZERO("0"), ONE("1"), TWO("2"), THREE("3"), FOUR("4"),
    FIVE("5"), SIX("6"), SEVEN("7"), EIGHT("8"), NINE("9"),

    // --- Whitespace and control ---
    SPACE(" "), TAB("Tab"), ENTER("Enter"), BACKSPACE("Backspace"),
    DELETE("Delete"), INSERT("Insert"), HOME("Home"), END("End"),
    PAGE_UP("PageUp"), PAGE_DOWN("PageDown"),

    // --- Modifier keys ---
    SHIFT("Shift"), CONTROL("Control"), ALT("Alt"), META("Meta"),
    CAPS_LOCK("CapsLock"), ESCAPE("Escape"),

    // --- Arrow keys ---
    ARROW_LEFT("ArrowLeft"), ARROW_UP("ArrowUp"),
    ARROW_RIGHT("ArrowRight"), ARROW_DOWN("ArrowDown"),

    // --- Function keys ---
    F1("F1"), F2("F2"), F3("F3"), F4("F4"), F5("F5"),
    F6("F6"), F7("F7"), F8("F8"), F9("F9"), F10("F10"),
    F11("F11"), F12("F12"), PRINT_SCREEN("PrintScreen"),
    SCROLL_LOCK("ScrollLock"), PAUSE("Pause"),

    // --- Punctuation and symbols ---
    EXCLAMATION("!"), AT("@"), HASH("#"), DOLLAR("$"), PERCENT("%"),
    CARET("^"), AMPERSAND("&"), ASTERISK("*"), LEFT_PAREN("("), RIGHT_PAREN(")"),
    DASH("-"), UNDERSCORE("_"), EQUALS("="), PLUS("+"),
    LEFT_BRACKET("["), RIGHT_BRACKET("]"), LEFT_BRACE("{"), RIGHT_BRACE("}"),
    SEMICOLON(";"), COLON(":"), SINGLE_QUOTE("'"), DOUBLE_QUOTE("\""),
    COMMA(","), PERIOD("."), LESS_THAN("<"), GREATER_THAN(">"),
    SLASH("/"), QUESTION("?"), BACKSLASH("\\"), PIPE("|"),
    BACKTICK("`"), TILDE("~"),

    // --- Numpad keys ---
    NUMPAD_0("0"), NUMPAD_1("1"), NUMPAD_2("2"), NUMPAD_3("3"), NUMPAD_4("4"),
    NUMPAD_5("5"), NUMPAD_6("6"), NUMPAD_7("7"), NUMPAD_8("8"), NUMPAD_9("9"),
    NUMPAD_DECIMAL("."), NUMPAD_ADD("+"), NUMPAD_SUBTRACT("-"),
    NUMPAD_MULTIPLY("*"), NUMPAD_DIVIDE("/"), NUMPAD_ENTER("Enter"), NUM_LOCK("NumLock"),

    // --- Other special keys ---
    CONTEXT_MENU("ContextMenu"), UNIDENTIFIED("Unidentified");

    /**
     * The string representation of the JavaScript key, as returned by {@code event.key}.
     */
    private final String key;

    /**
     * Constructs a new {@code JSKey} enum with the specified string representation.
     *
     * @param key the exact string value returned by {@code event.key} in JavaScript
     */
    Key(String key) {
        this.key = key;
    }

    /**
     * Returns the string value associated with this key, which corresponds to
     * what JavaScript would return from {@code event.key}.
     *
     * @return the string representation of the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Resolves a {@code JSKey} from a raw string key value typically
     * returned by {@code KeyboardEvent.key} in the browser.
     *
     * <p>This is useful when mapping JavaScript keyboard input to
     * corresponding enum values in server-side or Java-based code.
     *
     * @param key the raw {@code event.key} string
     * @return the matching {@code JSKey} constant, or {@code null} if none found
     */
    public static Key fromName(String key) {
        for (Key jsKey : values()) {
            if (jsKey.key.equals(key)) {
                return jsKey;
            }
        }
        return null;
    }
}
