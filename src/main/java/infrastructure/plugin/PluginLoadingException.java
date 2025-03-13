package infrastructure.plugin;

/**
 * Exception thrown when plugin loading fails due to an unrecoverable error.
 */
public class PluginLoadingException extends RuntimeException {
    /**
     * Constructs a new {@code PluginLoadingException} with the specified message and cause.
     *
     * @param message The error message.
     * @param cause The underlying cause of the failure.
     */
    public PluginLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}