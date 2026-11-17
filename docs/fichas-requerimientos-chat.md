# Fichas de requerimientos de ms-chat

Estado a 16 de noviembre de 2026 (versión `v1.0.0`, ramas `develop` y `master`). Las rutas son las vigentes: en inglés y sin `/me/`. Los clientes Feign hacia otros microservicios siguen usando las rutas de esos servicios.

## SR-RF-40 · Iniciar chat con vendedor

| Campo | Valor |
| --- | --- |
| Estado | Implementado |
| Actor | Comprador |
| Ruta | `POST /api/v1/second-hand/{listingId}/conversations` |
| Rol | `COMPRADOR` |
| Regla | Solo sobre una publicación disponible y ajena; si la conversación ya existe, devuelve la misma. |
| Respuestas | `200`, `401`, `403`, `404` (publicación inexistente), `422` (no disponible o propia), `503` (ms-segunda-vida no responde) |
| Evidencia | `StartConversationUseCaseImpl`, `ChatControllerTest`, `ChatIntegrationTest` |

## SR-RF-41 · Coordinar entrega mediante chat interno

| Campo | Valor |
| --- | --- |
| Estado | Implementado |
| Actores | Comprador y vendedor |
| Rutas REST | `GET` y `POST /api/v1/conversations/{id}/messages`; `GET /api/v1/users/conversations` y `GET /api/v1/users/{id}/conversations` (el id debe ser el del token) |
| Tiempo real | STOMP en `/ws`: envío en `/app/conversations/{id}/messages`, difusión en `/topic/conversations/{id}`, confirmaciones y errores por `/user/queue/conversations/acks` y `/errors` |
| Reglas | Solo los participantes leen y escriben; máximo 1.000 caracteres; se guarda antes de difundir; una conversación cerrada no acepta mensajes (`409`); máximo 30 mensajes por usuario por minuto (`429`). |
| Respuestas | `200`, `201`, `400`, `401`, `403`, `404`, `409`, `429`, `503` |
| Evidencia | `SendMessageUseCaseImpl`, `RateLimitedSendMessageUseCase`, `RealtimeChatController`, `JwtStompChannelInterceptor` y sus pruebas |

## SR-RF-73 · Chat con almacén aliado

| Campo | Valor |
| --- | --- |
| Estado | Implementado (antes figuraba como «no implementada») |
| Actores | Comprador y administrador del almacén |
| Rutas | `POST /api/v1/stores/{nit}/conversations` (comprador) y `GET /api/v1/stores/conversations` (bandeja del almacén, rol `ADMIN_ALMACEN`) |
| Regla de disponibilidad | ms-tiendas responde `200` con NIT: almacén disponible. `404`: `StoreNotFoundException`. Otro error o respuesta sin NIT: `ExternalServiceUnavailableException` (`503`). |
| Seguridad | El administrador solo accede a las conversaciones de su propio NIT, por REST y por STOMP (`Conversation.allowsRealtimeAccess`). Si ms-tiendas falla, se deniega. |
| Evidencia | `StoreQueryAdapter`, `StartStoreConversationUseCaseImpl`, `JwtStompChannelInterceptor`, `StoreAndDonationChatControllerTest`, `StoreQueryAdapterTest` |

## SR-RNF-15 · Mensajería confiable

| Campo | Valor |
| --- | --- |
| Estado | Implementado |
| Medidas | Consumidores idempotentes con tabla de eventos procesados; reintentos y cola de errores (`q.chat.segunda-mano.dlq`); confirmaciones del publicador de RabbitMQ con registro y métrica (`chat.events.publish.failed`) cuando un evento no se confirma o no se puede enrutar. |
| Alertas | `docs/observabilidad/alertas-prometheus.yml` |
