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

    @ParameterizedTest
    @ValueSource(strings = {
        "usuario@gmail.com",
        "Escríbeme a test.user@mail.escuelaing.edu.co porfa",
        "contacto: soporte@styleradar.co!",
        "Mi email es COMPRADOR_123@HOTMAIL.COM"
    })
    void rejectsEmailAddresses(String emailText) {
        assertThatThrownBy(() -> new MessageText(emailText))
                .isInstanceOf(InvalidMessageTextException.class)
                .hasMessageContaining("correos electrónicos");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Mi número es 3001234567 llámame",
        "Escríbeme al +57 310 987 6543",
        "Hablemos al 320-123-4567",
        "300.555.1234 whatsapp",
        "Llama al 6012345678",
        "fijo: 7654321"
    })
    void rejectsPhoneNumbers(String phoneText) {
        assertThatThrownBy(() -> new MessageText(phoneText))
                .isInstanceOf(InvalidMessageTextException.class)
                .hasMessageContaining("números telefónicos");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "eres una gonorrea",
        "que GonOrReA de producto",
        "eres un hijueputa",
        "vete a la MiErDa",
        "hpta ladron",
        "hp desagradecido",
        "es una putita",
        "eres una PUTA",
        "malparido estafador",
        "malparida mentirosa",
        "carechimba no responde",
        "que chimba pero eres un estupido",
        "estúpido de mierda",
        "eres un idiota",
        "imbécil total",
        "pendejo no sabe nada",
        "marica responde",
        "maricón no jodas",
        "eres una zorra",
        "perra traicionera",
        "pirobo ese",
        "bobo hp"
    })
    void rejectsProfanityAndOffensiveLanguage(String offensiveText) {
        assertThatThrownBy(() -> new MessageText(offensiveText))
                .isInstanceOf(InvalidMessageTextException.class)
                .hasMessageContaining("lenguaje inapropiado");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Hola, ¿sigue disponible la chaqueta?",
        "Buenas tardes, ¿cuál es el precio final?",
        "Tengo una pregunta sobre la talla M",
        "¿Hacen envíos a Medellín o Bogotá?",
        "Excelente estado de la prenda, me interesa comprarla."
    })
    void acceptsLegitimateChatMessages(String validMessage) {
        assertThat(new MessageText(validMessage).value()).isEqualTo(validMessage);
    }
}
