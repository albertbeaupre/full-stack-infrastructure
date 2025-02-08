package infrastructure.authentication;

/**
 * The {@code AuthenticationResult} record represents the outcome of an authentication
 * process. It encapsulates the result of an authentication attempt, including whether
 * the attempt was successful and a message providing additional details or context.
 *
 * <p>This record is used to convey the result of authentication checks performed by
 * the {@link Authenticator} and {@link Requirement} classes. It provides a straightforward
 * way to communicate success or failure, along with an optional message that can be used
 * to describe the outcome or provide error information.</p>
 *
 * @author Albert Beaupre
 * @since September 4th, 2024
 */
public record AuthenticationResult(boolean success, String message) { }