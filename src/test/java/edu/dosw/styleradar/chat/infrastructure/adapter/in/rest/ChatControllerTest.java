package edu.dosw.styleradar.chat.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.chat.application.mapper.in.MessageDtoInMapperImpl;
import edu.dosw.styleradar.chat.application.mapper.out.ConversationDtoOutMapperImpl;
import edu.dosw.styleradar.chat.application.mapper.out.MessageDtoOutMapperImpl;
import edu.dosw.styleradar.chat.domain.exception.SelfChatException;
import edu.dosw.styleradar.chat.domain.exception.ConversationClosedException;
import edu.dosw.styleradar.chat.domain.exception.ConversationNotFoundException;
import edu.dosw.styleradar.chat.domain.exception.ListingNotFoundException;
import edu.dosw.styleradar.chat.domain.exception.ExternalServiceUnavailableException;
import edu.dosw.styleradar.chat.domain.exception.InvalidMessageTextException;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.model.MessageText;
import edu.dosw.styleradar.chat.domain.port.in.GetMessagesUseCase;
import edu.dosw.styleradar.chat.domain.port.in.GetMyConversationsUseCase;
import edu.dosw.styleradar.chat.domain.port.in.SendMessageUseCase;
import edu.dosw.styleradar.chat.domain.port.in.StartConversationUseCase;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.JwtTokenAdapter;
import edu.dosw.styleradar.chat.infrastructure.config.SecurityConfig;
import edu.dosw.styleradar.chat.infrastructure.exception.GlobalExceptionHandler;
import edu.dosw.styleradar.chat.infrastructure.exception.SecurityProblemHandler;
import edu.dosw.styleradar.chat.support.Tokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.util.List;

import static edu.dosw.styleradar.chat.support.TestData.BUYER;
import static edu.dosw.styleradar.chat.support.TestData.LISTING;
import static edu.dosw.styleradar.chat.support.TestData.conversation;
import static edu.dosw.styleradar.chat.support.TestData.message;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Web slice tests of the REST contract, security and error mapping. */
@WebMvcTest(controllers = ChatController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, JwtTokenAdapter.class, SecurityProblemHandler.class, GlobalExceptionHandler.class,
        MessageDtoInMapperImpl.class, ConversationDtoOutMapperImpl.class, MessageDtoOutMapperImpl.class,
        ChatControllerTest.FixedClockConfig.class})
class ChatControllerTest {
    private static final String START = "/api/v1/segunda-mano/10/conversaciones";
    private static final String MESSAGES = "/api/v1/conversaciones/7/mensajes";
    private static final String MY_CONVERSATIONS = "/api/v1/usuarios/me/conversaciones";

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        Clock clock() {
            return Clock.systemUTC();
        }
    }

    @Autowired
    private MockMvc mvc;
    @MockBean
    private StartConversationUseCase start;
    @MockBean
    private GetMyConversationsUseCase myConversations;
    @MockBean
    private GetMessagesUseCase getMessages;
    @MockBean
    private SendMessageUseCase send;

    private static String buyer() {
        return Tokens.bearer(Tokens.forUser(BUYER));
    }

    @Test
    void withoutTokenRespondsWith401InProblemDetailsFormat() throws Exception {
        mvc.perform(post(START))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("No autenticado"));
    }

    @Test
    void withInvalidTokenRespondsWith401() throws Exception {
        mvc.perform(get(MY_CONVERSATIONS).header("Authorization", Tokens.bearer(Tokens.withAnotherKey(BUYER))))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(myConversations);
    }

    @Test
    void withAnotherRoleRespondsWith403() throws Exception {
        mvc.perform(get(MY_CONVERSATIONS)
                        .header("Authorization", Tokens.bearer(Tokens.withRoles(BUYER, List.of("ADMIN_STYLERADAR")))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verifyNoInteractions(myConversations);
    }

    @Test
    void startsAConversationWithTheTokenUser() throws Exception {
        when(start.start(BUYER, LISTING)).thenReturn(conversation(7L, ConversationStatus.OPEN));

        mvc.perform(post(START).header("Authorization", buyer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.publicacionId").value(10))
                .andExpect(jsonPath("$.prendaNombre").value("Chaqueta de jean"))
                .andExpect(jsonPath("$.compradorId").value(1))
                .andExpect(jsonPath("$.vendedorId").value(2))
                .andExpect(jsonPath("$.estado").value("ABIERTA"))
                .andExpect(jsonPath("$.fechaCreacion").exists());
    }

    @Test
    void listsMyConversations() throws Exception {
        when(myConversations.fetch(BUYER)).thenReturn(List.of(conversation(7L, ConversationStatus.CLOSED)));

        mvc.perform(get(MY_CONVERSATIONS).header("Authorization", buyer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].estado").value("CERRADA"));
    }

    @Test
    void listsTheMessages() throws Exception {
        when(getMessages.fetch(BUYER, 7L)).thenReturn(List.of(message(20L, 7L, BUYER, "Hola")));

        mvc.perform(get(MESSAGES).header("Authorization", buyer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20))
                .andExpect(jsonPath("$[0].conversacionId").value(7))
                .andExpect(jsonPath("$[0].autorId").value(1))
                .andExpect(jsonPath("$[0].texto").value("Hola"));
    }

    @Test
    void sendsAMessageAndRespondsWith201() throws Exception {
        when(send.send(eq(BUYER), eq(7L), eq(new MessageText("Hola"))))
                .thenReturn(message(20L, 7L, BUYER, "Hola"));

        mvc.perform(post(MESSAGES).header("Authorization", buyer())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"  Hola  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.texto").value("Hola"));
    }

    @Test
    void anEmptyMessageRespondsWith400WithTheField() throws Exception {
        mvc.perform(post(MESSAGES).header("Authorization", buyer())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.texto").exists());
        verifyNoInteractions(send);
    }

    @Test
    void aMalformedBodyRespondsWith400() throws Exception {
        mvc.perform(post(MESSAGES).header("Authorization", buyer())
                        .contentType(MediaType.APPLICATION_JSON).content("{texto"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aNonNumericIdRespondsWith400() throws Exception {
        mvc.perform(get("/api/v1/conversaciones/abc/mensajes").header("Authorization", buyer()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anInvalidDomainTextRespondsWith400() throws Exception {
        when(send.send(any(), any(), any())).thenThrow(new InvalidMessageTextException("inválido"));

        mvc.perform(post(MESSAGES).header("Authorization", buyer())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"Hola\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void foreignOrMissingConversationRespondsWith404() throws Exception {
        when(getMessages.fetch(BUYER, 7L)).thenThrow(new ConversationNotFoundException(7L));

        mvc.perform(get(MESSAGES).header("Authorization", buyer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe la conversación 7"))
                .andExpect(jsonPath("$.instance").value(MESSAGES));
    }

    @Test
    void missingListingRespondsWith404() throws Exception {
        when(start.start(BUYER, LISTING)).thenThrow(new ListingNotFoundException("no existe"));

        mvc.perform(post(START).header("Authorization", buyer())).andExpect(status().isNotFound());
    }

    @Test
    void businessRulesRespondWith422() throws Exception {
        when(start.start(BUYER, LISTING)).thenThrow(new SelfChatException("propia"));
        mvc.perform(post(START).header("Authorization", buyer()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Regla de negocio incumplida"));

        when(send.send(any(), any(), any())).thenThrow(new ConversationClosedException("cerrada"));
        mvc.perform(post(MESSAGES).header("Authorization", buyer())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"Hola\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("cerrada"));
    }

    @Test
    void ifListingServiceFailsRespondsWith503() throws Exception {
        when(start.start(BUYER, LISTING)).thenThrow(new ExternalServiceUnavailableException("caído"));

        mvc.perform(post(START).header("Authorization", buyer())).andExpect(status().isServiceUnavailable());
    }

    @Test
    void anUnexpectedErrorRespondsWith500WithoutInternalDetails() throws Exception {
        when(myConversations.fetch(BUYER)).thenThrow(new IllegalStateException("detalle interno"));

        mvc.perform(get(MY_CONVERSATIONS).header("Authorization", buyer()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Ocurrió un error inesperado"));
    }

    @Test
    void aMissingRouteRespondsWith404() throws Exception {
        mvc.perform(get("/api/v1/no-existe").header("Authorization", buyer())).andExpect(status().isNotFound());
    }
}
