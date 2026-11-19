# Trazabilidad técnica — Sprint 3 ms-chat

Fecha de revisión inicial: 12 de noviembre de 2026. Última actualización: 18 de noviembre de 2026. Fuente de actualización funcional: PDF SR-RF-40/41/73 recibido el 18 de noviembre. Este documento registra evidencia de esta copia del repositorio; no actualiza Jira ni implica que los cambios estén desplegados.

## Avance de implementación

- `ConversationClosedException` se traduce a HTTP `409`; por STOMP el emisor recibe el conflicto en `/user/queue/conversations/errors`. El mensaje no se confirma ni se difunde.
- El envío STOMP confirma al emisor en `/user/queue/conversations/acks` después de que el caso de uso retorna el mensaje persistido.
- Chat con almacén: ms-tiendas expone almacenes aliados verificados en `GET /api/v1/stores/{nit}`; el 404 se traduce a regla de negocio 422. La identidad del administrador se resuelve por `GET /api/v1/users/{userId}/store`, reenviando JWT y `X-User-Id`.
- Seguridad WebSocket: `JwtStompChannelInterceptor` extrae el identificador de la conversación del destino en SUBSCRIBE y SEND. El comprador entra solo si es participante. El administrador de almacén entra solo si el NIT de su almacén (consultado a ms-tiendas) coincide con el de la conversación; la regla vive en el dominio (`Conversation.allowsRealtimeAccess` y `belongsToStore`). Si ms-tiendas falla, el acceso se deniega.
- Las rutas REST se renombraron a inglés y sin `/me/` (ver tabla en la sección de contrato REST). Las rutas de los clientes Feign hacia otros microservicios y las routing keys de RabbitMQ no cambiaron.
- Se añadieron pruebas para JWT inválido, SEND y SUBSCRIBE de no participantes, administrador de otro almacén, destinos privados, orden persistencia-confirmación-difusión, conversación cerrada, fallo de ms-tiendas y fallo de Redis conservando entrega local.
- Se implementaron métricas con Micrometer/Prometheus (`ChatMetrics`, endpoint `/actuator/prometheus`). Desde el 17 de noviembre `/actuator/metrics` y `/actuator/prometheus` exigen JWT; solo salud y documentación son públicas.
- Límite de mensajes por usuario (30 por minuto, configurable): `RateLimitedSendMessageUseCase` lo aplica antes del caso de uso real y responde `429` por REST y por STOMP.
- Confirmaciones del publicador de RabbitMQ (`publisher-confirm-type: correlated`, `publisher-returns` y `mandatory`): un evento sin confirmar o sin ruta se registra y suma a `chat.events.publish.failed`. Reglas de alerta en `docs/observabilidad/alertas-prometheus.yml`.
- Nueva ruta `GET /api/v1/users/{id}/conversations` (mismo formato que ms-notificaciones; `403` si el id no es el del token). Se conserva `GET /api/v1/users/conversations`.
- Corrección en progreso para GJ-56: el comprador debe enviar el primer mensaje al crear el chat; administradores del mismo NIT pueden responder sin que el primero quede asignado como único vendedor. Los eventos de mensajes de tienda incluyen `almacenNit` para permitir notificación a los administradores; falta validar el consumidor de ms-notificaciones.
- Pipelines: smoke test posterior al despliegue de PROD, `TIENDAS_URL` y `DONACIONES_URL` en el despliegue de QA, Redis opcional en QA (secret `REDIS_URL_QA`), CodeQL y Dependabot.
- Verificación completa con Docker: `mvn clean verify` termina en BUILD SUCCESS con 179 pruebas, 0 fallos y 0 omitidas. Incluye Redis Pub/Sub real entre dos instancias y PostgreSQL con Testcontainers; JaCoCo cumple el umbral del 85 % (cobertura global de instrucciones: 88,5 %, 77 clases).
- El contrato STOMP está documentado como propuesta; falta acordarlo explícitamente con los clientes frontend.

## Contrato REST vigente

| Operación | Ruta |
| --- | --- |
| Iniciar chat con el vendedor de una publicación | `POST /api/v1/second-hand/{listingId}/conversations` |
| Iniciar chat sobre una donación | `POST /api/v1/donations/{donationId}/conversations` |
| Iniciar chat con un almacén aliado | `POST /api/v1/stores/{nit}/conversations` |
| Consultar mensajes | `GET /api/v1/conversations/{id}/messages` |
| Enviar mensaje | `POST /api/v1/conversations/{id}/messages` |
| Conversaciones del usuario | `GET /api/v1/users/conversations` |
| Bandeja del almacén aliado | `GET /api/v1/stores/conversations` |

Las rutas anteriores (`/api/v1/usuarios/me/...`, `/api/v1/almacenes/me/...` y similares) ya no existen y responden 404.

## Evidencia por tarea

| Jira | Evidencia revisada | Estado técnico observado |
| --- | --- | --- |
| GJ-54 — STOMP en tiempo real | `WebSocketConfig`, `RealtimeChatController`, pruebas STOMP unitarias | Base funcional; autenticación y persistencia antes de la difusión. Pendiente prueba con dos clientes reales y captura/video. |
| GJ-55 — Redis Pub/Sub | `RedisRealtimeMessageBroadcaster`, `RealtimeMessagingConfiguration`, `application.yml`, perfil `realtime` de Docker Compose | Implementación opcional y aislada. Redis de QA no está provisionado ni configurado; mantener una instancia hasta validarlo. |
| GJ-56 — Chat con almacén aliado | `ConversationType.ALMACEN`, adaptador Feign, casos de uso, REST/STOMP y pruebas | En corrección: primer mensaje obligatorio persistido al iniciar; contrato de ms-tiendas actualizado al ZIP; `STORE_ADMIN` normalizado; autorización por NIT sin fijar un único admin. Validación local pendiente. Cierre integral pendiente de alinear frontend (el ZIP usa endpoints provisionales) y confirmar que ms-notificaciones consume `almacenNit`. |
| GJ-57 — Chat de donaciones | `ConversationType.DONACION`, `DonationQueryAdapter`, `DonationsFeignClient`, `StartDonationConversationUseCase`, `StoreAndDonationChatControllerTest` | Implementado y probado. Modelo extendido con tipo `DONACION` e identificador de donación; contrato validado contra ms-donaciones; ruta `/api/v1/donations/{donationId}/conversations` con soporte para comprador/donante y representante en STOMP. Migración Flyway V3 aplicada. |
| GJ-103 — Contrato STOMP | `docs/contrato-websocket-stomp.md` | Destinos, payload, confirmación privada, error 409 e historial documentados; falta aprobación explícita de frontend. |
| GJ-104 — Seguridad WebSocket | `JwtStompChannelInterceptor`, `Conversation`, `JwtStompChannelInterceptorTest`, `ConversationTest` | CONNECT exige JWT; SEND y SUBSCRIBE validan participación. El administrador de almacén solo accede a conversaciones de su propio NIT; si ms-tiendas falla, se deniega. Cobertura de ramas en pruebas unitarias. Pendiente validar contra ms-tiendas real que un administrador del almacén A no pueda suscribirse a la conversación del almacén B. |
| GJ-105 — Flujo de mensajes | `SendMessageUseCaseImpl`, `MessageRepositoryAdapter`, REST historial y `RealtimeChatControllerTest` | Persistencia antes de confirmación privada y difusión; historial recuperable por REST. Sin estados de lectura ni clave idempotente de cliente. |
| GJ-106 — Redis y varias instancias | `RedisRealtimeMessageBroadcasterTest` y `RedisRealtimeMultiInstanceTest` | Unitarias cubren relay, deduplicación de eco y fallo Redis conservando entrega local. La prueba con Redis real usa Testcontainers y necesita Docker. |
| GJ-107 — Gateway y despliegue | `.github/workflows/ci-qa.yml`, `.github/workflows/ci-prod.yml`; ejecuciones CI QA #20 y #37, CI PROD #1 | App Service habilita WebSockets y el smoke test QA pasó. CI QA #37 (fusión del PR #6 en `develop`) terminó en verde con despliegue a Azure QA. PROD se creó y se desplegó con el tag `v1.0.0` (CI PROD #1 en verde). Falta validar handshake STOMP extremo a extremo a través del gateway usado por frontend. |
| GJ-108 — Observabilidad y documentación | `WebSocketSessionObserver`, `ChatMetrics`, logs de error Redis, README y contrato STOMP | Se registran aperturas/cierres de sesiones y errores de distribución sin registrar tokens ni cuerpos; métricas expuestas en `/actuator/prometheus`. Faltan dashboards de conexiones y entregas. |
| GJ-109 — Flujos de tienda y donaciones | `ChatIntegrationTest`, `StoreAndDonationChatControllerTest`, adapters y repositorios | En revalidación tras el cambio de contrato de GJ-56. La prueba de integración ahora cubre persistir el mensaje inicial, pero las pruebas deben ejecutarse y el flujo de donaciones sigue sin cambios en esta revisión. |

## Despliegue

| Entorno | URL | Estado |
| --- | --- | --- |
| QA | `https://gj-chat-service-qa-santi-gxhjccffcxdgbfcj.canadacentral-01.azurewebsites.net` | Desplegado desde `develop` (CI QA #37). Swagger con las 7 rutas en inglés. |
| PROD | `https://gj-chat-service-prod-santi.azurewebsites.net` | Desplegado con el tag `v1.0.0` (CI PROD #1, con aprobación manual del ambiente `production`). Swagger con las 7 rutas en inglés. |

Ambos entornos exponen `/swagger-ui.html`, `/actuator/health` y el WebSocket en `/ws`. PROD comparte plan de App Service con QA, y usa por ahora el perfil `qa`, el broker RabbitMQ de QA y los servicios de tiendas y donaciones de QA.

## Verificación local

- `mvn -B clean verify`: BUILD SUCCESS; 179 pruebas, 0 fallos, 0 omitidas y umbral JaCoCo alcanzado (ejecución con Docker Desktop).
- `RedisRealtimeMultiInstanceTest` verificó entrega desde una instancia hacia otra mediante un Redis real en Docker.
- Swagger local verificado con las 7 rutas en inglés.

## Estado de cierre de tareas Sprint 3

1. **GJ-56 (Chat con almacén aliado)**: En curso. Esta copia local incluye cambios para el contrato actualizado, mensaje inicial, autorización por NIT y `STORE_ADMIN`; pruebas no ejecutadas y no desplegado. Falta confirmar compatibilidad de frontend y ms-notificaciones.
2. **GJ-57 (Chat de donaciones)**: Cerrado técnicamente. Contrato con ms-donaciones integrado, modelo adaptado para donaciones y ruta habilitada.
3. **GJ-109 (Pruebas de flujos de tienda y donaciones)**: No cerrar todavía por la actualización de GJ-56. Tras completar GJ-56, ejecutar pruebas de ambos flujos; incluir inicio con primer mensaje, permisos por NIT y persistencia.

## Pendientes

- Para GJ-56: validar contra ms-tiendas real que un comprador puede iniciar con NIT aprobado y que un administrador solo abre/responde chats del NIT asociado.
- Para GJ-56: el ZIP de frontend recibido usa rutas provisionales; acordar y cambiar cliente a `POST /api/v1/stores/{nit}/conversations` con `{"texto":"..."}` y `GET /api/v1/stores/conversations`.
- Para GJ-56: confirmar que ms-notificaciones acepta `destinatarioId=null` y usa `almacenNit` para resolver administradores. Hasta esa confirmación no declarar completa la notificación del chat de tienda.
- Para GJ-56: volver a desplegar la rama ya fusionada y capturar evidencia de QA; los cambios de esta copia local no están en QA/PROD.
- Rechazar cuentas bloqueadas con token vigente: falta acordar con ms-identidad cómo consultar el estado de la cuenta.
- Crear Azure Cache for Redis en QA y guardar su URL como secret `REDIS_URL_QA` (GJ-55 y GJ-107).
- Crear un perfil `prod` y un broker RabbitMQ propio para PROD.
- Acordar explícitamente el contrato STOMP con frontend.
