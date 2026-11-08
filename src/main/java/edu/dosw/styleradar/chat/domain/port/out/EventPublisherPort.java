package edu.dosw.styleradar.chat.domain.port.out;

import edu.dosw.styleradar.chat.domain.event.MessageSentEvent;

/** Output port to publish chat events to the message broker. */
public interface EventPublisherPort {
    void publish(MessageSentEvent event);
}
