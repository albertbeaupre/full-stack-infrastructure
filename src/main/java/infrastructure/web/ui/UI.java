package infrastructure.web.ui;

import infrastructure.collections.queue.IntUUIDQueue;
import infrastructure.web.Router;
import infrastructure.web.SessionContext;
import infrastructure.web.ui.event.AttachEvent;
import infrastructure.web.ui.event.DetachEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the root of a component tree and the entry point for DOM updates.
 * Matches Vaadin's UI behavior, handling DOM updates, ID management, and component registration.
 */
public class UI extends Component {

    private final DOMDispatcher dispatcher = new DOMDispatcher();
    private final Router router = new Router(this);
    private final IntUUIDQueue componentIDs = new IntUUIDQueue();
    private final Map<Integer, Component> components = new HashMap<>();

    public UI() {
        super(new Element("body"));
        componentIDs.pop(); // Reserve 0 to avoid using 0 as a valid component ID
        this.getElement().setComponent(this);
    }

    @Override
    protected void create() {

    }

    @Override
    protected void destroy() {

    }

    public void register(Component component) {
        int id = component.getComponentID();
        if (id == 0) {
            id = getNextComponentID();
            component.getElement().setNodeId(id);
        }
        components.put(id, component);
    }

    public void unregister(Component component) {
        int id = component.getComponentID();
        components.remove(id);
        recycleComponentID(id);
    }

    public Component getComponent(int componentID) {
        return components.get(componentID);
    }

    public int getNextComponentID() {
        return componentIDs.pop();
    }

    public void recycleComponentID(int id) {
        componentIDs.push(id);
    }

    public DOMDispatcher getDispatcher() {
        return dispatcher;
    }

    public Router getRouter() {
        return router;
    }

    /**
     * Attaches a component to this UI and calls its onAttach method.
     * All descendants will be recursively attached.
     */
    public void attach(Component component) {
        this.add(component);
        propagateAttach(component);
        push();
    }

    private void propagateAttach(Component component) {
        component.onAttach(new AttachEvent(component));
        component.getChildren().forEach(this::propagateAttach);
    }

    /**
     * Detaches a component and recursively all of its children.
     */
    public void detach(Component component) {
        this.remove(component);
        propagateDetach(component);
        push();
    }

    private void propagateDetach(Component component) {
        component.onDetach(new DetachEvent(component));
        component.getChildren().forEach(this::propagateDetach);
    }

    /**
     * Flushes the DOM update queue and sends it to the client.
     */
    public void push() {
        dispatcher.flush(SessionContext.get().getChannel());
    }

    @Override
    public String tag() {
        return "body";
    }
}
