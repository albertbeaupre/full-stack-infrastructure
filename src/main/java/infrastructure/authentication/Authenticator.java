package infrastructure.authentication;

/**
 * The {@code Authenticator} class provides mechanisms to authenticate a {@link User}
 * based on a series of {@link Requirement} checks. It evaluates whether a user meets
 * all specified requirements to determine if authentication is successful.
 *
 * <p>This class is designed to be flexible and can be used to define custom
 * authentication flows by specifying different {@code Requirement} instances. Each
 * {@code Requirement} represents a condition that must be satisfied for authentication
 * to succeed. The class sequentially checks each requirement and returns the result
 * of the first failing requirement, or a success message if all requirements are met.</p>
 *
 * <pre>{@code
 * User user = new User();
 * user.attribute("Username", "JohnSmith");
 * user.attribute("Password", "Password123");
 * Requirement passwordRequirement = new PasswordRequirement();
 * Requirement emailRequirement = new EmailRequirement();
 * Authenticator authenticator = new Authenticator(passwordRequirement, emailRequirement);
 * AuthenticationResult result = authenticator.authenticate(user);
 * if (result.success())
 *      handleSuccessfulAuthentication();
 * }</pre>
 *
 * @see Requirement
 * @see AuthenticationResult
 * @see User
 * @since September 4th, 2024
 */
public class Authenticator {

    private final Requirement[] requirements;

    /**
     * Constructs an {@code Authenticator} with the specified {@link Requirement} objects.
     *
     * <p>This constructor allows the creation of an {@code Authenticator} that checks
     * multiple conditions for user authentication. The order of the requirements
     * determines the sequence in which they are checked.</p>
     *
     * @param requirements an array of {@link Requirement} objects representing the
     *                      authentication conditions.
     */
    public Authenticator(Requirement... requirements) {
        this.requirements = requirements;
    }

    /**
     * Authenticates the specified {@link User} by checking against all the defined
     * {@link Requirement} objects.
     *
     * <p>This method iterates over the array of requirements and applies each one to
     * the provided {@link User}. If any requirement is not satisfied, the method
     * returns the result of that failed requirement. If all requirements are met,
     * it returns a success result.</p>
     *
     * @param user the {@link User} object to be authenticated.
     * @return an {@link AuthenticationResult} indicating the outcome of the authentication
     *         process.
     */
    public AuthenticationResult authenticate(User user) {
        for (Requirement req : requirements) {
            AuthenticationResult result = req.satisfied(user);

            if (!result.success())
                return result;
        }
        return new AuthenticationResult(true, "Authentication Successful");
    }

}