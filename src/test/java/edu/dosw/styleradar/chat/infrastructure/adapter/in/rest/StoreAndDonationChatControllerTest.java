package edu.dosw.styleradar.chat.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.chat.application.dto.request.SendMessageRequest;
import edu.dosw.styleradar.chat.application.mapper.in.MessageDtoInMapperImpl;
import edu.dosw.styleradar.chat.application.mapper.out.ConversationDtoOutMapperImpl;
import edu.dosw.styleradar.chat.application.mapper.out.MessageDtoOutMapperImpl;
import edu.dosw.styleradar.chat.domain.exception.DonationNotFoundException;
import edu.dosw.styleradar.chat.domain.exception.DonationUnavailableException;
import edu.dosw.styleradar.chat.domain.exception.StoreUnavailableException;
import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.model.ConversationType;
import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.model.MessageText;
import edu.dosw.styleradar.chat.domain.model.PageResult;
import edu.dosw.styleradar.chat.domain.port.in.GetMessagesUseCase;
import edu.dosw.styleradar.chat.domain.port.in.GetMyConversationsUseCase;
import edu.dosw.styleradar.chat.domain.port.in.GetStoreConversationsUseCase;
import edu.dosw.styleradar.chat.domain.port.in.SendMessageUseCase;
import edu.dosw.styleradar.chat.domain.port.in.StartConversationUseCase;
import edu.dosw.styleradar.chat.domain.port.in.StartDonationConversationUseCase;
import edu.dosw.styleradar.chat.domain.port.in.StartStoreConversationUseCase;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.JwtTokenAdapter;
import edu.dosw.styleradar.chat.infrastructure.config.SecurityConfig;
import edu.dosw.styleradar.chat.infrastructure.exception.GlobalExceptionHandler;
import edu.dosw.styleradar.chat.infrastructure.exception.SecurityProblemHandler;
import edu.dosw.styleradar.chat.support.Tokens;
import org.junit.jupiter.api.DisplayName;
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
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, JwtTokenAdapter.class, SecurityProblemHandler.class, GlobalExceptionHandler.class,
        MessageDtoInMapperImpl.class, ConversationDtoOutMapperImpl.class, MessageDtoOutMapperImpl.class,
        StoreAndDonationChatControllerTest.FixedClockConfig.class})
class StoreAndDonationChatControllerTest {

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
    private StartStoreConversationUseCase startStoreConversation;

    @MockBean
    private GetStoreConversationsUseCase getStoreConversations;

    @MockBean
    private StartDonationConversationUseCase startDonationConversation;

    @MockBean
    private GetMyConversationsUseCase getMyConversations;

    @MockBean
    private GetMessagesUseCase getMessages;

    @MockBean
    private SendMessageUseCase sendMessage;

    @MockBean
    private edu.dosw.styleradar.chat.infrastructure.adapter.out.metrics.ChatMetrics metrics;

    @Test
    @DisplayName("GJ-56: Comprador inicia chat con almacén aliado exitosamente")
    void startStoreConversationSuccess() throws Exception {
        String token = Tokens.bearer(Tokens.withRoles(100L, List.of("COMPRADOR")));
        Conversation conv = new Conversation(1L, ConversationType.ALMACEN, null, null, 100L, null,
                "900123456-1", "Atelier Urbano", null, ConversationStatus.OPEN, Instant.now());

        when(startStoreConversation.start(100L, "900123456-1", new MessageText("¿Tienen disponibilidad?")))
                .thenReturn(conv);

        mvc.perform(post("/api/v1/stores/900123456-1/conversations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"¿Tienen disponibilidad?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.tipo", is("ALMACEN")))
                .andExpect(jsonPath("$.almacenNit", is("900123456-1")))
                .andExpect(jsonPath("$.almacenNombre", is("Atelier Urbano")))
                .andExpect(jsonPath("$.estado", is("ABIERTA")));
    }

    @Test
    @DisplayName("GJ-56: Iniciar chat con almacén inexistente devuelve 422")
    void startStoreConversationWhenStoreDoesNotExist() throws Exception {
        String token = Tokens.bearer(Tokens.withRoles(100L, List.of("COMPRADOR")));
        when(startStoreConversation.start(100L, "999999-9", new MessageText("Hola")))
                .thenThrow(new StoreUnavailableException("El almacén no existe o no está activo"));

        mvc.perform(post("/api/v1/stores/999999-9/conversations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Hola\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title", is("Regla de negocio incumplida")));
    }

    @Test
    @DisplayName("GJ-56: Iniciar chat con almacén no verificado devuelve 422")
    void startStoreConversationUnavailable() throws Exception {
        String token = Tokens.bearer(Tokens.withRoles(100L, List.of("COMPRADOR")));
        when(startStoreConversation.start(100L, "900123456-1", new MessageText("Hola")))
                .thenThrow(new StoreUnavailableException("Este almacén no está disponible para chat"));

        mvc.perform(post("/api/v1/stores/900123456-1/conversations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Hola\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title", is("Regla de negocio incumplida")));
    }

    @Test
    @DisplayName("GJ-56: Administrador de almacén consulta bandeja de conversaciones")
    void getStoreConversationsSuccess() throws Exception {
        String token = Tokens.bearer(Tokens.withRoles(200L, List.of("STORE_ADMIN")));
        Conversation conv = new Conversation(1L, ConversationType.ALMACEN, null, null, 100L, 200L,
                "900123456-1", "Atelier Urbano", null, ConversationStatus.OPEN, Instant.now());

        when(getStoreConversations.fetchForStoreAdmin(200L, 0, 20)).thenReturn(
                new PageResult<>(List.of(conv), 0, 20, 1, 1));

        mvc.perform(get("/api/v1/stores/conversations")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].almacenNit", is("900123456-1")));
    }

    @Test
    @DisplayName("Symmetrical REST: Admin almacén consulta por NIT /api/v1/stores/{nit}/conversations")
    void getStoreConversationsByNitSuccess() throws Exception {
        String token = Tokens.bearer(Tokens.withRoles(200L, List.of("STORE_ADMIN")));
        Conversation conv = new Conversation(1L, ConversationType.ALMACEN, null, null, 100L, 200L,
                "900123456-1", "Atelier Urbano", null, ConversationStatus.OPEN, Instant.now());

        when(getStoreConversations.fetchForStore(200L, "900123456-1", 0, 20)).thenReturn(
                new PageResult<>(List.of(conv), 0, 20, 1, 1));

        mvc.perform(get("/api/v1/stores/900123456-1/conversations")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].almacenNit", is("900123456-1")));
    }

    @Test
    @DisplayName("GJ-56: Usuario sin rol de tienda no puede consultar bandeja de almacén (403)")
    void getStoreConversationsForbidden() throws Exception {
        String token = Tokens.bearer(Tokens.withRoles(100L, List.of("COMPRADOR")));

        mvc.perform(get("/api/v1/stores/conversations")
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GJ-57: Donante o fundación inicia chat de donación exitosamente")
    void startDonationConversationSuccess() throws Exception {
        String token = Tokens.bearer(Tokens.withRoles(100L, List.of("COMPRADOR")));
        Conversation conv = new Conversation(2L, ConversationType.DONACION, null, "Donación #5", 100L, 500L,
                null, null, 5L, ConversationStatus.OPEN, Instant.now());

        when(startDonationConversation.start(100L, 5L)).thenReturn(conv);

        mvc.perform(post("/api/v1/donations/5/conversations")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.tipo", is("DONACION")))
                .andExpect(jsonPath("$.donacionId", is(5)))
                .andExpect(jsonPath("$.estado", is("ABIERTA")));
    }

    @Test
    @DisplayName("GJ-57: Iniciar chat sobre donación inexistente devuelve 404")
    void startDonationNotFound() throws Exception {
        String token = Tokens.bearer(Tokens.withRoles(100L, List.of("COMPRADOR")));
        when(startDonationConversation.start(100L, 999L))
                .thenThrow(new DonationNotFoundException("No existe la solicitud de donación 999"));

        mvc.perform(post("/api/v1/donations/999/conversations")
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GJ-57: Iniciar chat sobre donación finalizada devuelve 422")
    void startDonationFinalized() throws Exception {
        String token = Tokens.bearer(Tokens.withRoles(100L, List.of("COMPRADOR")));
        when(startDonationConversation.start(100L, 5L))
                .thenThrow(new DonationUnavailableException("La donación ya finalizó"));

        mvc.perform(post("/api/v1/donations/5/conversations")
                        .header("Authorization", token))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("GJ-109: Rol ADMIN_ALMACEN puede enviar mensajes en la conversación")
    void sendMessageAsStoreAdmin() throws Exception {
        String token = Tokens.bearer(Tokens.withRoles(200L, List.of("ADMIN_ALMACEN")));
        Message msg = new Message(10L, 1L, 200L, new MessageText("Hola, tenemos stock disponible"), Instant.now());

        when(sendMessage.send(eq(200L), eq(1L), any(MessageText.class))).thenReturn(msg);

        mvc.perform(post("/api/v1/conversations/1/messages")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Hola, tenemos stock disponible\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.autorId", is(200)));
    }
}
