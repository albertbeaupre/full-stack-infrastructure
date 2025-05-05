package infrastructure.net.web;

import infrastructure.net.web.ui.UI;

/**
 * Represents an application route that defines a specific path and its associated behavior.
 * Routes are used to handle navigation and attach content to the provided {@link UI}.
 *
 * @author Albert Beaupre
 */
public interface Route {

    /**
     * Loads the given UI by applying the operations defined in the current route.
     *
     * @param ui the {@link UI} instance to load. Represents the root container for
     *           all UI components within the session. The method's implementation
     *           determines how the UI is updated or set up based on the route.
     */
    void load(UI ui);

    /**
     * Retrieves the path associated with this route.
     * The path is a defined string
     */
    String getPath();

}
