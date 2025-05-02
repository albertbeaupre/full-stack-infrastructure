package infrastructure.net.web.ui;

import infrastructure.collections.queue.IntUUIDQueue;
import infrastructure.net.web.Router;
import infrastructure.net.web.SessionContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the root of a component tree and the entry point for DOM updates.
 * Matches Vaadin's UI behavior, handling DOM updates, ID management, and component registration.
 */
public class UI extends Component {

    private final Router router = new Router(this);
    private final IntUUIDQueue componentIDs = new IntUUIDQueue();
    private final Map<Integer, Component> components = new HashMap<>();

    public UI() {
        super("div");

        this.componentIDs.pop();
    }

    public Component get(int componentID) {
        return components.get(componentID);
    }

    public int nextComponentID() {
        return componentIDs.pop();
    }

    public void register(int componentID, Component component) {
        this.components.put(componentID, component);
    }

    public Component recycle(int componentID) {
        Component component = this.components.remove(componentID);
        componentIDs.push(componentID);
        return component;
    }

    public Router getRouter() {
        return router;
    }

    @Override
    protected void create() {

    }

    @Override
    protected void destroy() {

    }

    @Override
    public boolean isAttached() {
        return true;
    }
}
