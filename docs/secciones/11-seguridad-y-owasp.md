[⬅ Volver al README principal](../../README.md)

---

## 11. Seguridad, Habeas Data y análisis OWASP Top 10

### 1. Política de Habeas Data y Tratamiento de Datos Personales

En cumplimiento de la legislación aplicable de protección de datos personales (Ley 1581 de 2012 / Habeas Data):

- **Finalidad del Tratamiento:** Los datos recopilados por `ms-chat` (identificadores de usuario `userId`, NIT de almacenes aliados `storeNit`, metadatos de prendas y el contenido de los mensajes) se destinan exclusivamente a facilitar la comunicación directa, transparente y segura entre compradores, vendedores, almacenes y fundaciones.
- **Minimización y Confidencialidad:** El servicio no almacena contraseñas ni datos bancarios. Solo persiste la información estrictamente necesaria para la trazabilidad de la conversación.
- **Protección contra Fuga de Información en Logs:** Se prohíbe explícitamente y se valida en código que los interceptores de logging y filtros HTTP (`JwtAuthenticationFilter`, `GlobalExceptionHandler`, adaptadores Feign) no registren tokens JWT en texto plano ni el cuerpo privado de los mensajes en los archivos de log (`Slf4j`/Logback).
- **Derechos de Supresión y Consulta:** El usuario tiene derecho a consultar sus propias conversaciones (`GET /api/v1/users/conversations` y `GET /api/v1/users/{userId}/conversations`) y los mensajes de las conversaciones donde participa. El cierre de chats o depuración se sincroniza cuando una prenda o solicitud finaliza.

---

### 2. Controles Técnicos Demostrables Implementados

| Control | Mecanismo de Implementación | Evidencia / Ubicación |
|---|---|---|
| **Autenticación Stateless** | Validación criptográfica de tokens JWT (HS256) con clave mínima de 256 bits (`JWT_SECRET_BASE64`), reloj sincronizado y expiración. | `JwtTokenAdapter.java`, `JwtAuthenticationFilter.java` |
| **Control de Acceso Granular (BOLA/IDOR)** | Toda consulta o envío de mensaje valida en capa de dominio que el `userId` del token sea participante directo (`buyerId`, `sellerId` o administrador del `storeNit`). | `GetMessagesUseCaseImpl.java`, `SendMessageUseCaseImpl.java` |
| **Autorización Symmetrical Store NIT** | Los administradores de tienda solo pueden acceder a conversaciones si su NIT coincide con el asignado al usuario. | `GetStoreConversationsUseCaseImpl.java`, `ChatController.java` |
| **Seguridad WebSocket STOMP** | Handshake HTTP en `/ws` y `/api/v1/ws`. El comando STOMP `CONNECT` valida el JWT mediante interceptor antes de permitir sesión; `SUBSCRIBE` y `SEND` validan destino. | `WebSocketConfig.java`, `JwtStompChannelInterceptor.java` |
| **Rate Limiting** | Límite de tasa configurable (por defecto 30 mensajes por minuto por usuario) para mitigar spam y ataques DoS a nivel de aplicación. | `RateLimitedSendMessageUseCase.java`, `application.yml` |
| **Sanitización y Validación** | Validación estricta con `@Valid`, anotaciones Bean Validation, longitud máxima (5000 caracteres) y trim de espacios en blanco. | `SendMessageRequest.java`, `MessageText.java` |
| **CORS Delegado** | CORS desactivado localmente en Spring Security para delegar 100% de la política al API Gateway, eliminando cabeceras duplicadas. | `SecurityConfig.java` |

---

### 3. Matriz de Análisis OWASP Top 10 (Pertinente a `ms-chat`)

| Código OWASP | Riesgo Identificado | Impacto en ms-chat | Controles y Mitigaciones Implementadas | Estado |
|---|---|---|---|---|
| **A01:2021 - Broken Access Control** | Acceso a mensajes de chats ajenos o suplantación de tienda aliada. | Alto | Verificación en dominio de participantes por conversación y pertenencia de NIT de almacén. Pruebas unitarias de acceso denegado (403). | **Implementado** |
| **A02:2021 - Cryptographic Failures** | Interceptación de credenciales o mensajes en tránsito. | Alto | Cifrado en tránsito vía TLS/HTTPS en Azure. Clave JWT HS256 robusta (>= 256 bits). SSL/TLS en conexiones a RabbitMQ (`RABBITMQ_SSL_ENABLED=true`). | **Implementado** |
| **A03:2021 - Injection** | Inyección SQL en consultas de mensajes o conversaciones. | Alto | Uso estricto de Spring Data JPA con consultas parametrizadas. Migraciones de esquema bajo Flyway con DDL controlado. | **Implementado** |
| **A04:2021 - Insecure Design** | Abuso de recursos por envío masivo o auto-conversaciones. | Medio | Restricción de auto-chat (un vendedor no puede chatear consigo mismo). Rate limiting de 30 msgs/min. | **Implementado** |
| **A05:2021 - Security Misconfiguration** | Exposición de endpoints de infraestructura o métricas sensibles. | Medio | Endpoints de Actuator (`metrics`, `prometheus`) protegidos por JWT. Solo `health` e `info` son públicos. CORS centralizado en Gateway. | **Implementado** |
| **A07:2021 - Identification & Auth Failures** | Uso de tokens falsificados, manipulados o expirados. | Alto | Validación estricta de firma HMAC SHA-256 y fecha de expiración en cada petición REST y en handshake/frame STOMP. | **Implementado** |
| **A08:2021 - Software & Data Integrity** | Modificación no autorizada de eventos o dependencias vulnerables. | Medio | Escaneo continuo de vulnerabilidades con GitHub CodeQL y Dependabot. Envelope firmado con Correlation ID en eventos RabbitMQ. | **Implementado** |
| **A09:2021 - Security Logging Failures** | Falta de visibilidad de ataques o fallos de seguridad. | Medio | Registro de intentos de acceso denegado y errores 401/403 con métricas Micrometer (`ChatMetrics`) sin registrar PII. | **Implementado** |

---

### 4. Pendientes y Dependencias de Integración

- **Revocación en Tiempo Real (Blacklist):** Si un usuario es suspendido o bloqueado en `ms-auth`, el token JWT emitido previamente sigue siendo criptográficamente válido hasta su expiración a menos que se implemente una lista de revocación distribuida (Redis / eventos de invalidación).
- **Certificación Formal:** La presente matriz documenta los controles de diseño e implementación del equipo; no constituye una certificación de auditoría externa formal.
