package infrastructure.plugin;

/**
 * Interface that all plugins must implement.
 */
public interface Plugin {
    /**
     * Initializes the plugin after instantiation.
     * Implementations should perform any necessary setup here.
     */
    void initialize();
}