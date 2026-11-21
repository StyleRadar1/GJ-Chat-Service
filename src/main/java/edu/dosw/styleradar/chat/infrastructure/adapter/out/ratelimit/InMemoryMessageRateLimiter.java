package edu.dosw.styleradar.chat.infrastructure.adapter.out.ratelimit;

import edu.dosw.styleradar.chat.domain.port.out.MessageRateLimiterPort;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.metrics.ChatMetrics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita los mensajes por usuario con una ventana deslizante en memoria. El límite es por instancia:
 * con varias instancias cada una aplica su propio contador.
 */
@Component
public class InMemoryMessageRateLimiter implements MessageRateLimiterPort {
    private static final int CLEANUP_THRESHOLD = 10_000;

    private final Map<Long, Deque<Long>> attempts = new ConcurrentHashMap<>();
    private final int maxMessages;
    private final long windowMillis;
    private final Clock clock;
    private final ChatMetrics metrics;

    public InMemoryMessageRateLimiter(@Value("${styleradar.rate-limit.messages-per-window:30}") int maxMessages,
                                      @Value("${styleradar.rate-limit.window-seconds:60}") long windowSeconds,
                                      Clock clock, ChatMetrics metrics) {
        if (maxMessages < 1 || windowSeconds < 1) {
            throw new IllegalArgumentException("El límite y la ventana de mensajes deben ser mayores que cero");
        }
        this.maxMessages = maxMessages;
        this.windowMillis = windowSeconds * 1000;
        this.clock = clock;
        this.metrics = metrics;
    }

    @Override
    public boolean tryAcquire(Long userId) {
        long now = clock.millis();
        Deque<Long> window = attempts.computeIfAbsent(userId, id -> new ArrayDeque<>());
        boolean allowed;
        synchronized (window) {
            long oldestAllowed = now - windowMillis;
            while (!window.isEmpty() && window.peekFirst() <= oldestAllowed) {
                window.pollFirst();
            }
            allowed = window.size() < maxMessages;
            if (allowed) {
                window.addLast(now);
            }
        }
        if (!allowed) {
            metrics.recordMessageRateLimited();
        }
        if (attempts.size() > CLEANUP_THRESHOLD) {
            purge(now - windowMillis);
        }
        return allowed;
    }

    private void purge(long oldestAllowed) {
        attempts.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                Long last = entry.getValue().peekLast();
                return last == null || last <= oldestAllowed;
            }
        });
    }
}
