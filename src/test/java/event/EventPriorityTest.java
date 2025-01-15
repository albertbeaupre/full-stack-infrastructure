package event;

import backend.event.EventPublisher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventPriorityTest {


    private static final EventPublisher publisher = new EventPublisher();
    private static final TestEvent event = new TestEvent();
    private static final PublishingEventListener listener = new PublishingEventListener();
    private static final HigherPriorityEventListener listener2 = new HigherPriorityEventListener();

    @BeforeAll
    static void configure() {
        publisher.register(TestEvent.class, listener);
        publisher.register(TestEvent.class, listener2);
    }

    @Test
    void testPriority() {
        publisher.publish(event);

        assertEquals("World!", event.message);
    }
}
