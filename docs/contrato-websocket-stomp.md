# Contrato de chat en tiempo real (STOMP)

Este contrato aplica a las conversaciones que ya existen en ms-chat. La creación y consulta de conversaciones sigue usando la API REST autenticada.

## Conexión y autenticación

- Endpoint WebSocket: `/ws` (STOMP sobre WebSocket nativo).
- Enviar `Authorization: Bearer <JWT>` como cabecera nativa del frame STOMP `CONNECT`.
- El servicio valida la firma y vigencia del JWT. Las conexiones sin token válido se rechazan.
- El usuario debe ser participante de la conversación para suscribirse o enviar mensajes.

## Destinos

| Acción | Destino | Cuerpo |
| --- | --- | --- |
| Enviar mensaje | `SEND /app/conversations/{conversationId}/messages` | `{"text":"Hola"}` |
| Recibir mensajes | `SUBSCRIBE /topic/conversations/{conversationId}` | Objeto `MessageResponse` |
| Confirmación privada de guardado | `SUBSCRIBE /user/queue/conversations/acks` | El `MessageResponse` guardado |
| Error privado del emisor | `SUBSCRIBE /user/queue/conversations/errors` | `{"status":409,"code":"conflicto","detail":"..."}` cuando la conversación está cerrada |

El texto es obligatorio, no puede estar en blanco y admite hasta 1.000 caracteres. El servidor persiste el mensaje antes de enviar la confirmación privada al emisor y difundirlo al tópico. La confirmación privada solo indica que el servicio guardó el mensaje; no confirma que el otro participante lo haya leído. Un mensaje para una conversación cerrada no se guarda ni se difunde: el emisor recibe un error privado con estado lógico `409` y el mismo detalle de negocio que la API REST.

Ejemplo del objeto difundido (los nombres JSON se conservan para coincidir con la API REST):

```json
{
  "id": 20,
  "conversacionId": 7,
  "autorId": 1,
  "texto": "Hola",
  "fechaEnvio": "2026-11-12T20:00:00Z"
}
```

## Reconexión e historial

Redis Pub/Sub es un canal de distribución en vivo, no una cola durable. Después de reconectar, el cliente debe recuperar mensajes persistidos mediante `GET /api/v1/conversaciones/{id}/mensajes` con el mismo JWT. La API REST verifica que el usuario participe en esa conversación.

## Despliegue entre instancias

- En una instancia se conserva el broker simple local de Spring.
- Para varias instancias, configurar `WEBSOCKET_REDIS_ENABLED=true` y `REDIS_URL` con la URL de Redis accesible desde todas ellas. Usar `rediss://` si el proveedor exige TLS.
- Todas las instancias deben usar el mismo `WEBSOCKET_REDIS_CHANNEL` (por defecto `styleradar.chat.realtime`).
- RabbitMQ continúa publicando eventos de negocio; no reemplaza STOMP ni Redis Pub/Sub.
- El App Service actual debe permanecer en una instancia hasta provisionar y comprobar Redis en QA.

## Errores y límites actuales

- Los frames STOMP no autorizados se rechazan con error de mensajería; un cliente debe volver a autenticarse tras reconectar.
- Si Redis no está disponible, el mensaje ya guardado se entrega a las conexiones locales y queda recuperable por REST; se registra una advertencia. La entrega en vivo a otras instancias no está garantizada durante esa interrupción.
- Se registran aperturas/cierres de sesiones STOMP y errores de distribución Redis; no se registran tokens ni contenido de mensajes.
- El estado de lectura, confirmación de lectura y deduplicación por identificador del cliente no forman parte de este contrato actual.
