package infrastructure.web.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Element {
    private final String tag;
    private final List<Element> children = new ArrayList<>();
    private Element parent;
    private Component component;
    private int nodeId;

    public Element(String tag) {
        this.tag = tag;
    }

    public void appendChild(Element child) {
        children.add(child);
        child.parent = this;
    }

    /**
     * Removes a child element from this element.
     *
     * @param child the element to remove
     */
    public void removeChild(Element child) {
        if (children.remove(child)) {
            child.parent = null;
        }
    }

    /**
     * Removes this element from its parent, if any.
     */
    public void removeFromParent() {
        if (parent != null) {
            parent.removeChild(this);
        }
    }

    public List<Element> getChildren() {
        return children;
    }

    public Optional<Component> getComponent() {
        return Optional.ofNullable(component);
    }

    public void setComponent(Component c) {
        this.component = c;
    }

    public Optional<Element> getParent() {
        return Optional.ofNullable(parent);
    }

    public boolean isAttached() {
        return parent != null && (parent.isAttached() || parent.getClass().isAssignableFrom(UI.class));
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }
}
