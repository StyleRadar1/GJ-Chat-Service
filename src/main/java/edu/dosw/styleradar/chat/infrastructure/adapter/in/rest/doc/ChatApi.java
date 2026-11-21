package edu.dosw.styleradar.chat.infrastructure.adapter.in.rest.doc;

import edu.dosw.styleradar.chat.application.dto.request.SendMessageRequest;
import edu.dosw.styleradar.chat.application.dto.response.ConversationResponse;
import edu.dosw.styleradar.chat.application.dto.response.MessageResponse;
import edu.dosw.styleradar.chat.application.dto.response.PageResponse;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** OpenAPI contract of the chat REST endpoints across second-hand, ally stores, and donations. */
@Tag(name = "StyleRadar Chat Service", description = "Private messaging for second-hand items, partner stores, and donations")
@SecurityRequirement(name = "bearerAuth")
public interface ChatApi {
    String PROBLEM_JSON = "application/problem+json";
    String EX_400 = "{\"type\":\"https://styleradar.dosw/errores/solicitud-invalida\",\"title\":\"Solicitud inválida\",\"status\":400,\"detail\":\"Revise los campos enviados\",\"instance\":\"/api/v1/conversations/7/messages\",\"errores\":{\"texto\":\"El mensaje no puede estar vacío\"}}";
    String EX_401 = "{\"type\":\"https://styleradar.dosw/errores/no-autenticado\",\"title\":\"No autenticado\",\"status\":401,\"detail\":\"JWT ausente, inválido o expirado\",\"instance\":\"/api/v1/users/conversations\"}";
    String EX_403 = "{\"type\":\"https://styleradar.dosw/errores/acceso-denegado\",\"title\":\"Acceso denegado\",\"status\":403,\"detail\":\"No tiene permiso para realizar esta acción\",\"instance\":\"/api/v1/users/conversations\"}";
    String EX_404 = "{\"type\":\"https://styleradar.dosw/errores/recurso-no-encontrado\",\"title\":\"Recurso no encontrado\",\"status\":404,\"detail\":\"No existe la conversación 7\",\"instance\":\"/api/v1/conversations/7/messages\"}";
    String EX_422 = "{\"type\":\"https://styleradar.dosw/errores/regla-de-negocio\",\"title\":\"Regla de negocio incumplida\",\"status\":422,\"detail\":\"No puede iniciar un chat sobre su propia publicación\",\"instance\":\"/api/v1/second-hand/publications/10/conversations\"}";
    String EX_409 = "{\"type\":\"https://styleradar.dosw/errores/conflicto\",\"title\":\"Conflicto\",\"status\":409,\"detail\":\"La publicación fue retirada; la conversación está cerrada\",\"instance\":\"/api/v1/conversations/7/messages\"}";
    String EX_503 = "{\"type\":\"https://styleradar.dosw/errores/servicio-no-disponible\",\"title\":\"Servicio no disponible\",\"status\":503,\"detail\":\"El servicio externo no está disponible\",\"instance\":\"/api/v1/second-hand/publications/10/conversations\"}";

    @Operation(summary = "Start chat with seller",
            description = "Role BUYER. Receives and persists the first message when creating the chat. If a conversation already exists for the publication, returns it without requiring the listing to remain available.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversation started or existing conversation returned with the initial message saved"),
            @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired JWT",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_401))),
            @ApiResponse(responseCode = "403", description = "Role not authorized",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_403))),
            @ApiResponse(responseCode = "404", description = "Publication not found",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_404))),
            @ApiResponse(responseCode = "422", description = "Publication not available or belongs to the buyer",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_422))),
            @ApiResponse(responseCode = "503", description = "Publication service unavailable",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_503)))
    })
    @PreAuthorize("hasRole('COMPRADOR')")
    @PostMapping("/api/v1/second-hand/publications/{publicationId}/conversations")
    ResponseEntity<ConversationResponse> start(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long publicationId,
            @Valid @RequestBody SendMessageRequest request);

    @Operation(summary = "Start chat with partner store",
            description = "Role BUYER. Validates store availability with the stores service. Creates or reuses a conversation by buyer and NIT, saving the initial message in the history.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversation created or reused with the initial message persisted"),
            @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired JWT",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_401))),
            @ApiResponse(responseCode = "403", description = "Role not authorized",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_403))),
            @ApiResponse(responseCode = "422", description = "Store does not exist or is not active for chat",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_422))),
            @ApiResponse(responseCode = "503", description = "Store service unavailable",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_503)))
    })
    @PreAuthorize("hasRole('COMPRADOR')")
    @PostMapping("/api/v1/stores/{nit}/conversations")
    ResponseEntity<ConversationResponse> startStoreConversation(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String nit,
            @Valid @RequestBody SendMessageRequest request);

    @Operation(summary = "Get partner store conversation inbox",
            description = "Role STORE_ADMIN (compatible with ADMIN_ALMACEN/TIENDA). Resolves the NIT of the authenticated administrator. Supports page (zero-based) and size (1-100, default 20).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversations received by the partner store"),
            @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired JWT",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_401))),
            @ApiResponse(responseCode = "403", description = "Role not authorized or user does not manage a partner store",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_403))),
            @ApiResponse(responseCode = "503", description = "Store service unavailable",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_503)))
    })
    @PreAuthorize("hasAnyRole('ADMIN_ALMACEN', 'TIENDA')")
    @GetMapping("/api/v1/stores/conversations")
    ResponseEntity<PageResponse<ConversationResponse>> getStoreConversations(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size);

    @Operation(summary = "Start chat for donation request",
            description = "Role BUYER or FOUNDATION_REPRESENTATIVE. Validates with the donations service that the request is active.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversation started or existing conversation returned for the donation"),
            @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired JWT",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_401))),
            @ApiResponse(responseCode = "403", description = "User is neither donor nor recipient foundation",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_403))),
            @ApiResponse(responseCode = "404", description = "Donation request not found",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_404))),
            @ApiResponse(responseCode = "422", description = "Donation request has ended (REJECTED or DELIVERED)",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_422))),
            @ApiResponse(responseCode = "503", description = "Donations service unavailable",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_503)))
    })
    @PreAuthorize("hasAnyRole('COMPRADOR', 'REPRESENTANTE_FUNDACION', 'FUNDACION')")
    @PostMapping("/api/v1/donations/{donationId}/conversations")
    ResponseEntity<ConversationResponse> startDonationConversation(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long donationId);

    @Operation(summary = "Get user conversations",
            description = "Roles BUYER, STORE_ADMIN, FOUNDATION_REPRESENTATIVE. Includes conversations where the user participates. Supports page (zero-based) and size (1-100, default 20).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User conversations"),
            @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired JWT",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_401))),
            @ApiResponse(responseCode = "403", description = "Role not authorized",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_403)))
    })
    @PreAuthorize("hasAnyRole('COMPRADOR', 'ADMIN_ALMACEN', 'REPRESENTANTE_FUNDACION', 'TIENDA', 'FUNDACION')")
    @GetMapping("/api/v1/users/conversations")
    ResponseEntity<PageResponse<ConversationResponse>> getMyConversations(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size);

    @Operation(summary = "Get messages",
            description = "Roles BUYER, STORE_ADMIN, FOUNDATION_REPRESENTATIVE. Conversation participants only. Supports page (zero-based) and size (1-100, default 50); messages are ordered chronologically from oldest to newest.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversation messages"),
            @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired JWT",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_401))),
            @ApiResponse(responseCode = "403", description = "Role not authorized",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_403))),
            @ApiResponse(responseCode = "404", description = "Conversation not found or access denied",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_404)))
    })
    @PreAuthorize("hasAnyRole('COMPRADOR', 'ADMIN_ALMACEN', 'REPRESENTANTE_FUNDACION', 'TIENDA', 'FUNDACION')")
    @GetMapping("/api/v1/conversations/{id}/messages")
    ResponseEntity<PageResponse<MessageResponse>> getMessages(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size);

    @Operation(summary = "Send message",
            description = "Roles BUYER, STORE_ADMIN, FOUNDATION_REPRESENTATIVE. Conversation participants only; returns 409 if the conversation is closed.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Message sent"),
            @ApiResponse(responseCode = "400", description = "Message empty or exceeds 1,000 characters",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_400))),
            @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired JWT",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_401))),
            @ApiResponse(responseCode = "403", description = "Role not authorized",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_403))),
            @ApiResponse(responseCode = "404", description = "Conversation not found or access denied",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_404))),
            @ApiResponse(responseCode = "409", description = "Conversation is closed; message not saved",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_409))),
            @ApiResponse(responseCode = "503", description = "External service unavailable",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EX_503)))
    })
    @PreAuthorize("hasAnyRole('COMPRADOR', 'ADMIN_ALMACEN', 'REPRESENTANTE_FUNDACION', 'TIENDA', 'FUNDACION')")
    @PostMapping("/api/v1/conversations/{id}/messages")
    ResponseEntity<MessageResponse> sendMessage(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody SendMessageRequest request);
}
