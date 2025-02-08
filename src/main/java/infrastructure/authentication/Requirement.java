package infrastructure.authentication;

/**
 * The {@code Requirement} interface defines a contract for implementing authentication
 * conditions that a {@link User} must meet. Classes that implement this interface provide
 * the logic for checking whether a user satisfies specific requirements for authentication.
 *
 * <p>This interface is intended to be used in conjunction with the {@link Authenticator}
 * class to create flexible and customizable authentication processes. Implementing classes
 * should define their specific criteria for authentication and implement the {@link #satisfied(User)}
 * method to check whether these criteria are met.</p>
 *
 * @see Authenticator
 * @see AuthenticationResult
 * @see User
 * @author Albert Beaupre
 * @since September 4th, 2024
 */
public interface Requirement {

    /**
     * Evaluates whether the specified {@link User} satisfies this requirement.
     *
     * <p>This method contains the logic for determining if a user meets the criteria
     * defined by the implementing class. The result of this evaluation is returned
     * as an {@link AuthenticationResult}, which indicates whether the user satisfies
     * the requirement and provides any relevant messages or error details.</p>
     *
     * @param user the {@link User} object to be evaluated against this requirement.
     * @return an {@link AuthenticationResult} indicating whether the user satisfies
     *         this requirement and any associated messages or errors.
     */
    AuthenticationResult satisfied(User user);

}