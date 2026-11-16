package edu.dosw.styleradar.chat.application.dto.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageRequestsTest {
    @Test
    void testSendMessageRequest() {
        SendMessageRequest request = new SendMessageRequest("Hola mundo");
        assertThat(request.text()).isEqualTo("Hola mundo");
    }

    @Test
    void testRealtimeMessageRequest() {
        RealtimeMessageRequest request = new RealtimeMessageRequest("Hola realtime");
        assertThat(request.text()).isEqualTo("Hola realtime");
    }
}
