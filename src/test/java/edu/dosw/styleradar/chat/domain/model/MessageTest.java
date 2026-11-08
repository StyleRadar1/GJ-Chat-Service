package edu.dosw.styleradar.chat.domain.model;

import org.junit.jupiter.api.Test;

import static edu.dosw.styleradar.chat.support.TestData.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests for the Message entity. */
class MessageTest {
    @Test
    void exposesItsData() {
        Message message = new Message(5L, 7L, 1L, new MessageText("Hola"), NOW);
        assertThat(message.getId()).isEqualTo(5L);
        assertThat(message.getConversationId()).isEqualTo(7L);
        assertThat(message.getAuthorId()).isEqualTo(1L);
        assertThat(message.getText().value()).isEqualTo("Hola");
        assertThat(message.getSentAt()).isEqualTo(NOW);
    }

    @Test
    void requiresConversationAuthorTextAndDate() {
        MessageText text = new MessageText("Hola");
        assertThatThrownBy(() -> new Message(null, null, 1L, text, NOW)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Message(null, 7L, null, text, NOW)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Message(null, 7L, 1L, null, NOW)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Message(null, 7L, 1L, text, null)).isInstanceOf(NullPointerException.class);
    }
}
