package infrastructure.web;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The {@code Scripts} class provides utility methods for loading and accessing JavaScript
 * source files stored in a predefined directory. Scripts are loaded into memory and
 * can be retrieved by their base filename (excluding the ".js" extension).
 *
 * <p>This utility supports use cases such as injecting client-side logic into
 * dynamically generated HTML or preparing JavaScript content for template substitution.
 *
 * <p>Scripts are optionally evaluated using the Nashorn JavaScript engine if available,
 * allowing for embedded expression expansion (e.g., template literals).
 *
 * @author Albert Beaupre
 * @version 1.0
 * @since April 19th, 2025
 */
public final class Scripts {

    private static final Logger LOGGER = Logger.getLogger(Scripts.class.getName());

    /**
     * The JavaScript engine used to optionally evaluate script content.
     */
    private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("nashorn");

    /**
     * A map of script contents loaded from disk, keyed by filename without the ".js" extension.
     */
    private static final HashMap<String, String> LOADED_SCRIPTS = new HashMap<>();

    /**
     * The path to the directory where JavaScript files are stored.
     */
    private static final Path DIRECTORY = Paths.get("src/main/resources/js");

    /**
     * Inaccessible
     */
    private Scripts() {}

    /**
     * Recursively loads all JavaScript (.js) files from the {@code DIRECTORY} path.
     *
     * <p>For each JavaScript file found, this method:
     * <ul>
     *     <li>Reads its contents as a UTF-8 string</li>
     *     <li>Evaluates the script content if a JS engine is available</li>
     *     <li>Extracts the filename without extension as the map key</li>
     *     <li>Stores the (optionally evaluated) content in the {@code LOADED_SCRIPTS} map</li>
     * </ul>
     *
     * <p>This method must be called before attempting to retrieve any scripts.
     *
     * @throws RuntimeException if an I/O error occurs during traversal or file reading
     */
    public static void load() {
        int count = 0;
        try (Stream<Path> paths = Files.walk(DIRECTORY)) {
            for (Path path : (Iterable<Path>) paths::iterator) {
                if (Files.isRegularFile(path) && path.toString().endsWith(".js")) {
                    try {
                        String content = Files.readString(path);
                        String filename = path.getFileName().toString();
                        String key = filename.substring(0, filename.lastIndexOf('.'));
                        LOADED_SCRIPTS.put(key, evaluate(content));
                        count++;
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to load script: " + path, e);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to traverse JS script directory", e);
        }

        LOGGER.info(String.format("Loaded %d JavaScript files from %s", count, DIRECTORY));
    }

    /**
     * Evaluates JavaScript content using the {@code SCRIPT_ENGINE} if available.
     *
     * <p>This allows template literals and inline expressions to be resolved.
     *
     * @param script the original script string
     * @return the evaluated result or the original script if no engine is available
     * @throws RuntimeException if evaluation fails
     */
    private static String evaluate(String script) {
        try {
            if (SCRIPT_ENGINE != null) {
                return SCRIPT_ENGINE.eval(script).toString();
            } else {
                return script; // fallback
            }
        } catch (ScriptException e) {
            throw new RuntimeException("Failed to evaluate JS script", e);
        }
    }

    /**
     * Retrieves the content of a loaded JavaScript file by its key and performs named placeholder replacement.
     *
     * <p>Placeholders in the script should be wrapped in curly braces, like {htmlID}, {eventName}, etc.
     * This method safely replaces only those placeholders using a regex pattern.
     *
     * @param key    the name of the script file without the ".js" extension
     * @param params a map of placeholder names to replacement values
     * @return the script with placeholders replaced, or {@code null} if not found
     */
    public static String get(String key, Map<String, String> params) {
        String script = LOADED_SCRIPTS.get(key);
        if (script == null) return null;

        Pattern pattern = Pattern.compile("\\{(\\w+)}"); // matches {placeholderName}
        Matcher matcher = pattern.matcher(script);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = params.getOrDefault(placeholder, matcher.group(0));
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }
}