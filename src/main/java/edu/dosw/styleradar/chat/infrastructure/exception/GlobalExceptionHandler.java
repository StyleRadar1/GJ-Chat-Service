package edu.dosw.styleradar.chat.infrastructure.exception;

import edu.dosw.styleradar.chat.domain.exception.SelfChatException;
import edu.dosw.styleradar.chat.domain.exception.ConversationClosedException;
import edu.dosw.styleradar.chat.domain.exception.ConversationNotFoundException;
import edu.dosw.styleradar.chat.domain.exception.ListingUnavailableException;
import edu.dosw.styleradar.chat.domain.exception.ListingNotFoundException;
import edu.dosw.styleradar.chat.domain.exception.ExternalServiceUnavailableException;
import edu.dosw.styleradar.chat.domain.exception.InvalidMessageTextException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.stream.Collectors;

/** Translates domain and framework errors into RFC 7807 problem details. */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {
    private static final Map<String, String> FIELD_NAMES = Map.of("text", "texto");

    @ExceptionHandler({ConversationNotFoundException.class, ListingNotFoundException.class})
    public ProblemDetail notFound(RuntimeException e, HttpServletRequest request) {
        return ProblemDetails.of(HttpStatus.NOT_FOUND, "recurso-no-encontrado", "Recurso no encontrado",
                e.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({ListingUnavailableException.class, SelfChatException.class})
    public ProblemDetail businessRule(RuntimeException e, HttpServletRequest request) {
        return ProblemDetails.of(HttpStatus.UNPROCESSABLE_ENTITY, "regla-de-negocio", "Regla de negocio incumplida",
                e.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ConversationClosedException.class)
    public ProblemDetail conversationConflict(ConversationClosedException e, HttpServletRequest request) {
        return ProblemDetails.of(HttpStatus.CONFLICT, "conflicto", "Conflicto", e.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(InvalidMessageTextException.class)
    public ProblemDetail invalidText(InvalidMessageTextException e, HttpServletRequest request) {
        return ProblemDetails.of(HttpStatus.BAD_REQUEST, "solicitud-invalida", "Solicitud inválida",
                e.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(f -> FIELD_NAMES.getOrDefault(f.getField(), f.getField()), f -> String.valueOf(f.getDefaultMessage()),
                        (a, b) -> a));
        ProblemDetail problem = ProblemDetails.of(HttpStatus.BAD_REQUEST, "solicitud-invalida", "Solicitud inválida",
                "Revise los campos enviados", request.getRequestURI());
        problem.setProperty("errores", errors);
        return problem;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail malformed(Exception e, HttpServletRequest request) {
        return ProblemDetails.of(HttpStatus.BAD_REQUEST, "solicitud-invalida", "Solicitud inválida",
                "La solicitud está mal formada", request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail accessDenied(AccessDeniedException e, HttpServletRequest request) {
        return ProblemDetails.of(HttpStatus.FORBIDDEN, "acceso-denegado", "Acceso denegado",
                "No tiene permiso para realizar esta acción", request.getRequestURI());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail missingRoute(NoResourceFoundException e, HttpServletRequest request) {
        return ProblemDetails.of(HttpStatus.NOT_FOUND, "recurso-no-encontrado", "Recurso no encontrado",
                "La ruta solicitada no existe", request.getRequestURI());
    }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public ProblemDetail externalService(ExternalServiceUnavailableException e, HttpServletRequest request) {
        return ProblemDetails.of(HttpStatus.SERVICE_UNAVAILABLE, "servicio-no-disponible", "Servicio no disponible",
                e.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail unexpected(Exception e, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), e);
        return ProblemDetails.of(HttpStatus.INTERNAL_SERVER_ERROR, "error-interno", "Error interno",
                "Ocurrió un error inesperado", request.getRequestURI());
    }
}
