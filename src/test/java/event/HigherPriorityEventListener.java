package event;

import backend.event.EventListener;
import backend.event.EventPriority;

public class HigherPriorityEventListener implements EventListener<TestEvent> {

    @EventPriority(priority = 1)
    @Override
    public void handle(TestEvent event) {
        event.message += "World";
    }

}
