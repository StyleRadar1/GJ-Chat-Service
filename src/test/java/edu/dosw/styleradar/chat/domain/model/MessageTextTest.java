package edu.dosw.styleradar.chat.domain.model;

import edu.dosw.styleradar.chat.domain.exception.InvalidMessageTextException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests for the message text value object. */
class MessageTextTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\n\t"})
    void rejectsEmptyText(String value) {
        assertThatThrownBy(() -> new MessageText(value))
                .isInstanceOf(InvalidMessageTextException.class)
                .hasMessageContaining("vacío");
    }

    @Test
    void trimsExtraSpaces() {
        assertThat(new MessageText("  ¿Sigue disponible?  ").value()).isEqualTo("¿Sigue disponible?");
    }

    @Test
    void acceptsExactlyOneThousandCharacters() {
        assertThat(new MessageText("a".repeat(1000)).value()).hasSize(1000);
    }

    @Test
    void rejectsMoreThanOneThousandCharacters() {
        assertThatThrownBy(() -> new MessageText("a".repeat(1001)))
                .isInstanceOf(InvalidMessageTextException.class)
                .hasMessageContaining("1000");
    }

    @Test
    void countsTheLimitAfterTrimming() {
        assertThat(new MessageText("  " + "a".repeat(1000) + "  ").value()).hasSize(1000);
    }
}
