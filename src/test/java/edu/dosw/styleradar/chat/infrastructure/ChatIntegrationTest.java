package edu.dosw.styleradar.chat.infrastructure;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.SecondHandFeignClient;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.StoresFeignClient;
import edu.dosw.styleradar.chat.domain.port.out.EventPublisherPort;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.ExternalListingResponse;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.ExternalStoreResponse;
import feign.FeignException;
import feign.Request;
import feign.Response;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.ConversationRepositoryAdapter;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.MessageRepositoryAdapter;
import edu.dosw.styleradar.chat.support.Tokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end chat flow against a real PostgreSQL container. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class ChatIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ConversationRepositoryAdapter conversations;
    @Autowired
    private MessageRepositoryAdapter messages;
    @MockBean
    private SecondHandFeignClient secondHand;
    @MockBean
    private StoresFeignClient stores;
    @MockBean
    private EventPublisherPort eventPublisher;

    private static ExternalListingResponse listing(Long id, Long seller, String status) {
        return new ExternalListingResponse(id, seller, new ExternalListingResponse.Garment("Chaqueta"), status, null);
    }

    @Test
    void fullChatFlow() throws Exception {
        when(secondHand.getListing(100L)).thenReturn(listing(100L, 200L, "DISPONIBLE"));
        String buyer = Tokens.bearer(Tokens.forUser(300L));
        String seller = Tokens.bearer(Tokens.forUser(200L));
        String outsider = Tokens.bearer(Tokens.forUser(400L));

        String response = mvc.perform(post("/api/v1/second-hand/100/conversations").header("Authorization", buyer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendedorId").value(200))
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(response.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mvc.perform(post("/api/v1/second-hand/100/conversations").header("Authorization", buyer))
                .andExpect(jsonPath("$.id").value(id));

        mvc.perform(post("/api/v1/conversations/" + id + "/messages").header("Authorization", buyer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"¿Sigue disponible?\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/conversations/" + id + "/messages").header("Authorization", seller)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"Sí, en Chapinero\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/conversations/" + id + "/messages").header("Authorization", seller))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].texto").value("¿Sigue disponible?"))
                .andExpect(jsonPath("$[1].autorId").value(200));

        mvc.perform(get("/api/v1/users/conversations").header("Authorization", seller))
                .andExpect(jsonPath("$[0].id").value(id));

        mvc.perform(get("/api/v1/conversations/" + id + "/messages").header("Authorization", outsider))
                .andExpect(status().isNotFound());
    }

    @Test
    void ifListingIsWithdrawnConversationIsClosed() throws Exception {
        when(secondHand.getListing(101L)).thenReturn(listing(101L, 201L, "DISPONIBLE"));
        String buyer = Tokens.bearer(Tokens.forUser(301L));
        String response = mvc.perform(post("/api/v1/second-hand/101/conversations").header("Authorization", buyer))
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(response.replaceAll(".*\"id\":(\\d+).*", "$1"));

        when(secondHand.getListing(101L)).thenReturn(listing(101L, 201L, "RETIRADA"));
        mvc.perform(post("/api/v1/conversations/" + id + "/messages").header("Authorization", buyer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"Hola\"}"))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/v1/users/conversations").header("Authorization", buyer))
                .andExpect(jsonPath("$[0].estado").value("CERRADA"));
    }

    @Test
    void ifTwoRequestsCreateTheSameConversationOnlyOneIsKept() {
        Conversation first = conversations.save(new Conversation(null, 102L, "Gorra", 302L, 202L,
                ConversationStatus.OPEN, Instant.now()));
        Conversation duplicate = conversations.save(new Conversation(null, 102L, "Gorra", 302L, 202L,
                ConversationStatus.OPEN, Instant.now()));

        assertThat(duplicate.getId()).isEqualTo(first.getId());
    }

    @Test
    void storeIsAvailableWhenTheStoresServiceReturns200() throws Exception {
        when(stores.getStore("900123456-1")).thenReturn(
                new ExternalStoreResponse("900123456-1", "Atelier Urbano", "Ropa", 4.8));
        String buyer = Tokens.bearer(Tokens.withRoles(310L, List.of("COMPRADOR")));

        mvc.perform(post("/api/v1/stores/900123456-1/conversations").header("Authorization", buyer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"¿Tienen disponibilidad?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("ALMACEN"))
                .andExpect(jsonPath("$.almacenNit").value("900123456-1"))
                .andExpect(jsonPath("$.almacenNombre").value("Atelier Urbano"))
                .andExpect(jsonPath("$.estado").value("ABIERTA"));
        Conversation conversation = conversations.findByStoreAndBuyer("900123456-1", 310L).orElseThrow();
        assertThat(messages.findByConversation(conversation.getId())).hasSize(1);
        assertThat(messages.findByConversation(conversation.getId()).get(0).getText().value())
                .isEqualTo("¿Tienen disponibilidad?");
    }

    @Test
    void storeIsNotAvailableWhenTheStoresServiceReturns404() throws Exception {
        when(stores.getStore("999999-9")).thenThrow(storesError(404));
        String buyer = Tokens.bearer(Tokens.withRoles(311L, List.of("COMPRADOR")));

        mvc.perform(post("/api/v1/stores/999999-9/conversations").header("Authorization", buyer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"Hola\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void storeIsNotAvailableWhenTheStoresServiceFails() throws Exception {
        when(stores.getStore("900123456-2")).thenThrow(storesError(503));
        String buyer = Tokens.bearer(Tokens.withRoles(312L, List.of("COMPRADOR")));

        mvc.perform(post("/api/v1/stores/900123456-2/conversations").header("Authorization", buyer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"Hola\"}"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void healthAndDocsArePublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    private static FeignException storesError(int status) {
        Request request = Request.create(Request.HttpMethod.GET, "http://tiendas/api/v1/almacenes/x",
                Map.of(), null, StandardCharsets.UTF_8, null);
        Response response = Response.builder().status(status).reason("error").request(request).headers(Map.of()).build();
        return FeignException.errorStatus("StoresFeignClient#getStore", response);
    }
}
