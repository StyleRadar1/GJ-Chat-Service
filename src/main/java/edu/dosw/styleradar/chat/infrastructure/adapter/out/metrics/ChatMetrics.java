package edu.dosw.styleradar.chat.infrastructure.adapter.out.metrics;

import edu.dosw.styleradar.chat.domain.model.ConversationType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/** Micrometer metrics collector for the chat service (GJ-108). */
@Component
public class ChatMetrics {
    private final MeterRegistry registry;

    public ChatMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /** Records the creation of a new conversation categorized by type. */
    public void recordConversationCreated(ConversationType type) {
        Counter.builder("chat.conversations.created")
                .description("Total number of conversations created")
                .tag("type", type != null ? type.name() : "UNKNOWN")
                .register(registry)
                .increment();
    }

    /** Records a successfully sent chat message. */
    public void recordMessageSent(ConversationType type) {
        Counter.builder("chat.messages.sent")
                .description("Total number of messages sent")
                .tag("type", type != null ? type.name() : "UNKNOWN")
                .register(registry)
                .increment();
    }

    /** Measures message processing and sending duration. */
    public void recordMessageLatency(long durationMs) {
        Timer.builder("chat.messages.latency")
                .description("Latency of sending messages in milliseconds")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /** Records a realtime message broadcasted via WebSocket. */
    public void recordRealtimeMessageSent() {
        Counter.builder("chat.websocket.messages.sent")
                .description("Total number of messages broadcasted via WebSocket")
                .register(registry)
                .increment();
    }
}
