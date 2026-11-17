package edu.dosw.styleradar.chat.domain.port.out;

/** Puerto de salida que limita cuántos mensajes puede enviar un usuario en una ventana de tiempo. */
public interface MessageRateLimiterPort {
    /** Implementación sin límite, útil cuando no se quiere restringir el envío. */
    MessageRateLimiterPort UNLIMITED = userId -> true;

    /** Registra un intento de envío y dice si el usuario sigue dentro del límite. */
    boolean tryAcquire(Long userId);
}
