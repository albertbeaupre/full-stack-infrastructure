package infrastructure.event;

/**
 * An {@code Event} can be published by the {@code EventPublisher} so it can be
 * handled by an {@code EventListener}.
 * 
 * <p>
 * Events can be consumed so that no further {@code EventListener} can handle the
 * Event past the one which handled it last.
 * 
 * @author Albert Beaupre
 * @since August 29th, 2024
 */
public class Event {
    
    private boolean consumed; // flag to determine if it already has been consumed
    
    /**
     * Returns True if this {@code Event} has been consumed; false otherwise.
     */
    public boolean consumed() {
        return consumed;
    }
    
    /**
     * Consumes this {@code Event} so that it may no longer be handled by
     * any further {@code EventListener} past the one which handled it last.
     */
    public void consume() {
        if (consumed)
            throw new UnsupportedOperationException("Event has already been consumed!");
        this.consumed = true;
    }
    
}