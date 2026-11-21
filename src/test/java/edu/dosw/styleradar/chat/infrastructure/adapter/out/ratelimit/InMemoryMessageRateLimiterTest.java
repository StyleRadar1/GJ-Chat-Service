package edu.dosw.styleradar.chat.infrastructure.adapter.out.ratelimit;

import edu.dosw.styleradar.chat.infrastructure.adapter.out.metrics.ChatMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pruebas del limitador de mensajes por usuario con ventana deslizante. */
class InMemoryMessageRateLimiterTest {
    private MutableClock clock;
    private SimpleMeterRegistry registry;
    private InMemoryMessageRateLimiter limiter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-11-16T12:00:00Z"));
        registry = new SimpleMeterRegistry();
        limiter = new InMemoryMessageRateLimiter(3, 60, clock, new ChatMetrics(registry));
    }

    @Test
    void allowsUpToTheLimitAndRejectsTheNext() {
        assertThat(limiter.tryAcquire(1L)).isTrue();
        assertThat(limiter.tryAcquire(1L)).isTrue();
        assertThat(limiter.tryAcquire(1L)).isTrue();
        assertThat(limiter.tryAcquire(1L)).isFalse();
        assertThat(registry.get("chat.messages.rate_limited").counter().count()).isEqualTo(1.0);
    }

    @Test
    void theLimitIsPerUser() {
        for (int i = 0; i < 3; i++) {
            limiter.tryAcquire(1L);
        }

        assertThat(limiter.tryAcquire(1L)).isFalse();
        assertThat(limiter.tryAcquire(2L)).isTrue();
    }

    @Test
    void allowsAgainOnceTheWindowHasPassed() {
        for (int i = 0; i < 3; i++) {
            limiter.tryAcquire(1L);
        }
        assertThat(limiter.tryAcquire(1L)).isFalse();

        clock.advanceSeconds(61);

        assertThat(limiter.tryAcquire(1L)).isTrue();
    }

    @Test
    void rejectsAnInvalidConfiguration() {
        ChatMetrics metrics = new ChatMetrics(registry);

        assertThatThrownBy(() -> new InMemoryMessageRateLimiter(0, 60, clock, metrics))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new InMemoryMessageRateLimiter(3, 0, clock, metrics))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /** Reloj que se puede adelantar para probar la ventana de tiempo. */
    private static final class MutableClock extends Clock {
        private Instant now;

        private MutableClock(Instant start) {
            this.now = start;
        }

        private void advanceSeconds(long seconds) {
            now = now.plusSeconds(seconds);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
