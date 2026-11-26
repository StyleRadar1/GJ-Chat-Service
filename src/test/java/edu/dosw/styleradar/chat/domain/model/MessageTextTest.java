package edu.dosw.styleradar.chat.domain.model;

import edu.dosw.styleradar.chat.domain.exception.InvalidMessageTextException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests for the message text value object with censoring behavior. */
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

    @Test
    void censorsEmailAddresses() {
        MessageText text = new MessageText("Escríbeme a usuario@gmail.com por favor");
        assertThat(text.value()).doesNotContain("usuario@gmail.com");
        assertThat(text.value()).contains("*****************");
    }

    @Test
    void censorsPhoneNumbers() {
        MessageText text = new MessageText("Mi número es 3001234567 y el fijo 6012345678");
        assertThat(text.value()).doesNotContain("3001234567");
        assertThat(text.value()).doesNotContain("6012345678");
        assertThat(text.value()).contains("**********");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Tetranutra",
        "eres un tetranutra",
        "putita",
        "eres una gonorrea",
        "que GonOrReA de producto",
        "vete a la MiErDa",
        "hpta ladron",
        "hp desagradecido",
        "eres una PUTA",
        "malparido estafador",
        "carechimba",
        "estúpido de mierda",
        "eres un idiota",
        "imbécil total"
    })
    void censorsProfanityAndOffensiveLanguage(String offensive) {
        MessageText messageText = new MessageText(offensive);
        assertThat(messageText.value()).contains("*");
        assertThat(messageText.value()).doesNotContainIgnoringCase("gonorrea");
        assertThat(messageText.value()).doesNotContainIgnoringCase("putita");
        assertThat(messageText.value()).doesNotContainIgnoringCase("mierda");
        assertThat(messageText.value()).doesNotContainIgnoringCase("hpta");
        assertThat(messageText.value()).doesNotContainIgnoringCase("malparido");
        assertThat(messageText.value()).doesNotContainIgnoringCase("carechimba");
        assertThat(messageText.value()).doesNotContainIgnoringCase("tetranutra");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Treintra y tres mil veces doble hijue p ut a",
        "eres un h i j u e p u t a",
        "es una p u t a",
        "m i e r d a de servicio",
        "g o n o r r e a",
        "t e t r a n u t r a"
    })
    void censorsSpacedProfanityDisguisedWithSpaces(String spacedOffensive) {
        MessageText messageText = new MessageText(spacedOffensive);
        assertThat(messageText.value()).contains("*");
        assertThat(messageText.value()).doesNotContainIgnoringCase("p ut a");
        assertThat(messageText.value()).doesNotContainIgnoringCase("p u t a");
        assertThat(messageText.value()).doesNotContainIgnoringCase("m i e r d a");
        assertThat(messageText.value()).doesNotContainIgnoringCase("g o n o r r e a");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Hola, ¿sigue disponible la chaqueta?",
        "Buenas tardes, ¿cuál es el precio final?",
        "Tengo una pregunta sobre la talla M",
        "¿Hacen envíos a Medellín o Bogotá?",
        "Excelente estado de la prenda, me interesa comprarla."
    })
    void preservesLegitimateChatMessages(String validMessage) {
        assertThat(new MessageText(validMessage).value()).isEqualTo(validMessage);
    }
}
