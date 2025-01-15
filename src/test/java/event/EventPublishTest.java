package event;

import backend.event.EventPublisher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests the functionality of the event system.
 * <p>
 * This includes:
 * <li>
 * <l>Event Publishing</l>
 * <l>Event Consumption</l>
 * </li>
 *
 * @author Albert Beaupre
 * @since August 29th, 2024
 */
public class EventPublishTest {

    private static final EventPublisher publisher = new EventPublisher();
    private static final TestEvent event = new TestEvent();
    private static final PublishingEventListener listener = new PublishingEventListener();

    @BeforeAll()
    static void configure() {
        publisher.register(TestEvent.class, listener);
    }

    @Test
    void eventPublish() {
        publisher.publish(event);

        assertTrue(event.published);
    }
}
