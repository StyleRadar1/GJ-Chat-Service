package edu.dosw.styleradar.chat.domain.port.out;

/** Output port that remembers consumed event ids so redelivered events are ignored. */
public interface ProcessedEventPort {
    boolean isProcessed(String eventId);

    void markProcessed(String eventId);
}
