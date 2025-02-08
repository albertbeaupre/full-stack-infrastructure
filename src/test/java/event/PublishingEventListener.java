package event;

import infrastructure.event.EventListener;

public class PublishingEventListener implements EventListener<TestEvent> {

    @Override
    public void handle(TestEvent event) {
        event.published = true;
        event.message += "!";
    }
}