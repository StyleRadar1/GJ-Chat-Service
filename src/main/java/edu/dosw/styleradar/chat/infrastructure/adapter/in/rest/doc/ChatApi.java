package edu.dosw.styleradar.chat.infrastructure.adapter.in.rest.doc;

import edu.dosw.styleradar.chat.application.dto.request.EnviarMensajeRequest;
import edu.dosw.styleradar.chat.application.dto.response.ConversacionResponse;
import edu.dosw.styleradar.chat.application.dto.response.MensajeResponse;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Chat de segunda mano", description = "Mensajes privados entre comprador y vendedor (SR-RF-40, SR-RF-41)")
@SecurityRequirement(name = "bearerAuth")
public interface ChatApi {

    String PROBLEM_JSON = "application/problem+json";
    String EJ_400 = "{\"type\":\"https://styleradar.dosw/errores/solicitud-invalida\",\"title\":\"Solicitud inválida\",\"status\":400,\"detail\":\"Revise los campos enviados\",\"instance\":\"/api/v1/conversaciones/7/mensajes\",\"errores\":{\"texto\":\"El mensaje no puede estar vacío\"}}";
    String EJ_401 = "{\"type\":\"https://styleradar.dosw/errores/no-autenticado\",\"title\":\"No autenticado\",\"status\":401,\"detail\":\"JWT ausente, inválido o expirado\",\"instance\":\"/api/v1/usuarios/me/conversaciones\"}";
    String EJ_403 = "{\"type\":\"https://styleradar.dosw/errores/acceso-denegado\",\"title\":\"Acceso denegado\",\"status\":403,\"detail\":\"No tiene permiso para realizar esta acción\",\"instance\":\"/api/v1/usuarios/me/conversaciones\"}";
    String EJ_404 = "{\"type\":\"https://styleradar.dosw/errores/recurso-no-encontrado\",\"title\":\"Recurso no encontrado\",\"status\":404,\"detail\":\"No existe la conversación 7\",\"instance\":\"/api/v1/conversaciones/7/mensajes\"}";
    String EJ_422 = "{\"type\":\"https://styleradar.dosw/errores/regla-de-negocio\",\"title\":\"Regla de negocio incumplida\",\"status\":422,\"detail\":\"No puede iniciar un chat sobre su propia publicación\",\"instance\":\"/api/v1/segunda-mano/10/conversaciones\"}";
    String EJ_503 = "{\"type\":\"https://styleradar.dosw/errores/servicio-no-disponible\",\"title\":\"Servicio no disponible\",\"status\":503,\"detail\":\"El servicio de publicaciones no está disponible\",\"instance\":\"/api/v1/segunda-mano/10/conversaciones\"}";

    @Operation(summary = "Iniciar chat con el vendedor (SR-RF-40)",
            description = "Rol COMPRADOR. Publicación DISPONIBLE y ajena. Si ya existe la conversación, la devuelve.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversación iniciada o existente"),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido o expirado",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EJ_401))),
            @ApiResponse(responseCode = "403", description = "Rol sin permiso",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EJ_403))),
            @ApiResponse(responseCode = "404", description = "Publicación inexistente",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EJ_404))),
            @ApiResponse(responseCode = "422", description = "Publicación no disponible o propia",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EJ_422))),
            @ApiResponse(responseCode = "503", description = "El servicio de publicaciones no respondió",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EJ_503)))
    })
    @PreAuthorize("hasRole('COMPRADOR')")
    @PostMapping("/api/v1/segunda-mano/{publicacionId}/conversaciones")
    ResponseEntity<ConversacionResponse> iniciar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long publicacionId);

    @Operation(summary = "Consultar mis conversaciones (SR-RF-41)",
            description = "Rol COMPRADOR. Incluye las conversaciones como comprador y como vendedor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversaciones propias"),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido o expirado",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EJ_401))),
            @ApiResponse(responseCode = "403", description = "Rol sin permiso",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EJ_403)))
    })
    @PreAuthorize("hasRole('COMPRADOR')")
    @GetMapping("/api/v1/usuarios/me/conversaciones")
    ResponseEntity<List<ConversacionResponse>> consultarMisConversaciones(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Consultar mensajes (SR-RF-41)", description = "Rol COMPRADOR. Solo participantes; en orden de envío.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mensajes de la conversación"),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido o expirado",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EJ_401))),
            @ApiResponse(responseCode = "403", description = "Rol sin permiso",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EJ_403))),
            @ApiResponse(responseCode = "404", description = "Conversación inexistente o ajena",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EJ_404)))
    })
    @PreAuthorize("hasRole('COMPRADOR')")
    @GetMapping("/api/v1/conversaciones/{id}/mensajes")
    ResponseEntity<List<MensajeResponse>> consultarMensajes(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id);

    @Operation(summary = "Enviar mensaje para coordinar la entrega (SR-RF-41)",
            description = "Rol COMPRADOR. Solo participantes; no se puede escribir si la publicación fue retirada.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensaje enviado"),
            @ApiResponse(responseCode = "400", description = "Mensaje vacío o de más de 1.000 caracteres",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EJ_400))),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido o expirado",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EJ_401))),
            @ApiResponse(responseCode = "403", description = "Rol sin permiso",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EJ_403))),
            @ApiResponse(responseCode = "404", description = "Conversación inexistente o ajena",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EJ_404))),
            @ApiResponse(responseCode = "422", description = "La publicación fue retirada",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EJ_422))),
            @ApiResponse(responseCode = "503", description = "El servicio de publicaciones no respondió",
                    content = @Content(mediaType = PROBLEM_JSON, examples = @ExampleObject(value = EJ_503)))
    })
    @PreAuthorize("hasRole('COMPRADOR')")
    @PostMapping("/api/v1/conversaciones/{id}/mensajes")
    ResponseEntity<MensajeResponse> enviarMensaje(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id,
            @Valid @RequestBody EnviarMensajeRequest request);
}
