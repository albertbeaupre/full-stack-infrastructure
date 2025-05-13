package infrastructure.utility;

import java.util.Objects;

/**
 * Utility class that provides methods for reflection-based operations.
 */
public class ReflectionUtility {

    /**
     * Checks if a method exists in the given class with the specified name and parameter types.
     *
     * @param clazz          the class to inspect
     * @param methodName     the name of the method
     * @param parameterTypes the parameter types of the method
     * @return true if the method exists, false otherwise
     */
    public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        Objects.requireNonNull(clazz, "Class cannot be null");
        Objects.requireNonNull(methodName, "Method name cannot be null");

        try {
            clazz.getDeclaredMethod(methodName, parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            // Check inherited methods
            try {
                clazz.getMethod(methodName, parameterTypes);
                return true;
            } catch (NoSuchMethodException ignored) {
                return false;
            }
        } catch (SecurityException e) {
            System.err.println("Security restriction accessing method " + methodName + " in " + clazz.getName());
            return false;
        }
    }

}
