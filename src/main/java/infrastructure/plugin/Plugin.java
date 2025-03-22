package infrastructure.plugin;

/**
 * Interface that all plugins must implement.
 *
 * @author Albert Beaupre
 * @since March 13th, 2025
 */
public interface Plugin {
    /**
     * Initializes the plugin after instantiation.
     * Implementations should perform any necessary setup here.
     */
    void initialize();
}