package event;

import infrastructure.event.EventPublisher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventConsumeTest {

    private static final EventPublisher publisher = new EventPublisher();
    private static final TestEvent event = new TestEvent();
    private static final PublishingEventListener listener = new PublishingEventListener();
    private static final ConsumingEventListener listener2 = new ConsumingEventListener();

    @BeforeAll
    static void configure() {
        publisher.register(TestEvent.class, listener2);
        publisher.register(TestEvent.class, listener);
    }

    @Test
    void testConsume() {
        publisher.publish(event);

        assertTrue(event.isConsumed());
        assertEquals("Hello", event.message);
    }
}
