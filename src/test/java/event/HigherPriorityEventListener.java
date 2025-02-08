package event;

import infrastructure.event.EventListener;
import infrastructure.event.EventPriority;

public class HigherPriorityEventListener implements EventListener<TestEvent> {

    @EventPriority(priority = 1)
    @Override
    public void handle(TestEvent event) {
        event.message += "World";
    }

}
