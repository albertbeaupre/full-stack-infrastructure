package event;

import backend.event.EventListener;

public class PublishingEventListener implements EventListener<TestEvent> {

    @Override
    public void handle(TestEvent event) {
        event.published = true;
        event.message += "!";
    }
}