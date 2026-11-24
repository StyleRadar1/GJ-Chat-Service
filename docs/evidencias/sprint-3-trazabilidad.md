# Trazabilidad técnica — Sprint 3 ms-chat

Fecha de revisión inicial: 12 de noviembre de 2026. Última actualización documental: 20 de noviembre de 2026. Fuentes: código incluido en esta copia, requerimientos funcionales recibidos y estado reportado de `develop`. Los números de pruebas y despliegues más abajo son registros históricos aportados antes de esta actualización, no ejecuciones realizadas hoy. Este documento no actualiza Jira ni acredita por sí solo un despliegue o una prueba de integración actual.

## Ajustes SR-RF-40/41 en esta copia local

- `POST /api/v1/second-hand/publications/{publicationId}/conversations` recibe `{"texto":"..."}` y persiste el mensaje inicial cuando crea una conversación.
- La consulta idempotente de conversación existente ocurre antes de consultar la publicación, así se puede recuperar su historial aunque ya no esté disponible.
- Los historiales REST de mensajes, conversaciones propias y bandeja del almacén usan paginación de base de datos (`page`, `size`, máximo 100), con orden estable.
- El código actual solo valida JWT (firma, expiración e identidad/roles). Rechazar una cuenta bloqueada aunque el JWT vigente no refleje el cambio sigue pendiente del contrato de estado/revocación de ms-auth.
- La paginación de mensajes, conversaciones propias y bandeja de almacén está implementada en esta copia (base cero, tamaño limitado a 100). La compatibilidad del cliente debe verificarse aparte.
- El cliente frontend debe enviar el texto inicial a la ruta C2C y consumir el sobre de paginación en las tres consultas; no se actualizó el repositorio frontend en esta tarea.
- Compilación de fuentes y tests con `mvn -q -DskipTests test-compile`: correcta. Las pruebas no se ejecutaron. Esta copia no tiene `.git`; los cambios aún deben copiarse a la rama de trabajo y pasar CI.

## Avance de implementación

- `ConversationClosedException` se traduce a HTTP `409`; por STOMP el emisor recibe el conflicto en `/user/queue/conversations/errors`. El mensaje no se confirma ni se difunde.
- El envío STOMP confirma al emisor en `/user/queue/conversations/acks` después de que el caso de uso retorna el mensaje persistido.
- Chat con almacén: ms-tiendas expone almacenes aliados verificados en `GET /api/v1/stores/{nit}`; el 404 se traduce a regla de negocio 422. La identidad del administrador se resuelve por `GET /api/v1/users/{userId}/store`, reenviando JWT y `X-User-Id`.
- Seguridad WebSocket: `JwtStompChannelInterceptor` extrae el identificador de la conversación del destino en SUBSCRIBE y SEND. El comprador entra solo si es participante. El administrador de almacén entra solo si el NIT de su almacén (consultado a ms-tiendas) coincide con el de la conversación; la regla vive en el dominio (`Conversation.allowsRealtimeAccess` y `belongsToStore`). Si ms-tiendas falla, el acceso se deniega.
- Las rutas REST se renombraron a inglés y sin `/me/` (ver tabla en la sección de contrato REST). Las rutas de los clientes Feign hacia otros microservicios y las routing keys de RabbitMQ no cambiaron.
- Se añadieron pruebas para JWT inválido, SEND y SUBSCRIBE de no participantes, administrador de otro almacén, destinos privados, orden persistencia-confirmación-difusión, conversación cerrada, fallo de ms-tiendas y fallo de Redis conservando entrega local.
- Se implementaron métricas con Micrometer/Prometheus (`ChatMetrics`, endpoint `/actuator/prometheus`). Desde el 17 de noviembre `/actuator/metrics` y `/actuator/prometheus` exigen JWT; solo salud y documentación son públicas.
- Límite de mensajes por usuario (30 por minuto, configurable): `RateLimitedSendMessageUseCase` lo aplica antes del caso de uso real y responde `429` por REST y por STOMP.
- Confirmaciones del publicador de RabbitMQ (`publisher-confirm-type: correlated`, `publisher-returns` y `mandatory`): un evento sin confirmar o sin ruta se registra y suma a `chat.events.publish.failed`. Esto detecta ciertos fallos, pero no garantiza que la notificación llegue al usuario. La política de reintentos/entrega final debe acordarse con `ms-notificaciones` y el responsable de mensajería; no se asigna una implementación de recuperación a ms-chat (SR-RNF-15).
- La consulta de conversaciones propias queda únicamente en `GET /api/v1/users/conversations`; el usuario se determina a partir del JWT.
- GJ-56 / SR-RF-73: el código de esta copia implementa inicio con mensaje inicial, bandeja por administrador y autorización por NIT. El estado de trabajo reportado para `develop` es implementado; faltan las validaciones de integración con ms-tiendas desplegado, frontend y consumidor de ms-notificaciones. No declarar la integración completa hasta obtener esa evidencia.
- GJ-57: el chat de donaciones está presente en el código, en las rutas y en las pruebas del repositorio. La verificación de extremo a extremo con el servicio de donaciones y el frontend debe evidenciarse separadamente; no omitir esta ficha de la matriz del equipo.
- Pipelines: smoke test posterior al despliegue de PROD, `TIENDAS_URL` y `DONACIONES_URL` en el despliegue de QA, Redis opcional en QA (secret `REDIS_URL_QA`), CodeQL y Dependabot.
- Registro histórico reportado de verificación con Docker: `mvn clean verify` terminó en BUILD SUCCESS con 179 pruebas, 0 fallos y 0 omitidas. Incluyó Redis Pub/Sub real entre dos instancias y PostgreSQL con Testcontainers; JaCoCo reportó 88,5 % de cobertura de instrucciones (umbral 85 %, 77 clases). No se volvió a ejecutar en esta actualización.
- El contrato STOMP está documentado como propuesta; falta acordarlo explícitamente con los clientes frontend.

## Contrato REST vigente

| Operación | Ruta |
| --- | --- |
| Iniciar chat con el vendedor de una publicación | `POST /api/v1/second-hand/publications/{publicationId}/conversations` |
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
| GJ-56 — Chat con almacén aliado / SR-RF-73 | `ConversationType.ALMACEN`, `StoreQueryAdapter`, `StoresFeignClient`, casos de uso, REST/STOMP | Implementado en `develop` según estado reportado. La copia contiene primer mensaje obligatorio, bandeja y autorización por NIT. Pendiente validar con ms-tiendas desplegado, alinear frontend y confirmar consumo de `almacenNit` en ms-notificaciones. |
| GJ-57 — Chat de donaciones | `ConversationType.DONACION`, `DonationQueryAdapter`, `DonationsFeignClient`, `StartDonationConversationUseCase`, `StoreAndDonationChatControllerTest` | Código y trazabilidad presentes; alcance aprobado previamente. Integración con ms-segunda-vida-donaciones y frontend pendiente de evidencia. Una sala existente se recupera sin consultar el estado externo; no hay cierre automático por cambios posteriores de estado. Acordar evento/ciclo de vida con el servicio de donaciones. |
| GJ-103 — Contrato STOMP | `docs/contrato-websocket-stomp.md` | Destinos, payload, confirmación privada, error 409 e historial documentados; falta aprobación explícita de frontend. |
| GJ-104 — Seguridad WebSocket | `JwtStompChannelInterceptor`, `Conversation`, `JwtStompChannelInterceptorTest`, `ConversationTest` | CONNECT exige JWT; SEND y SUBSCRIBE validan participación. El administrador de almacén solo accede a conversaciones de su propio NIT; si ms-tiendas falla, se deniega. Cobertura de ramas en pruebas unitarias. Pendiente validar contra ms-tiendas real que un administrador del almacén A no pueda suscribirse a la conversación del almacén B. |
| GJ-105 — Flujo de mensajes | `SendMessageUseCaseImpl`, `MessageRepositoryAdapter`, REST historial y `RealtimeChatControllerTest` | Persistencia antes de confirmación privada y difusión; historial recuperable por REST. Sin estados de lectura ni clave idempotente de cliente. |
| GJ-106 — Redis y varias instancias | `RedisRealtimeMessageBroadcasterTest` y `RedisRealtimeMultiInstanceTest` | Unitarias cubren relay, deduplicación de eco y fallo Redis conservando entrega local. La prueba con Redis real usa Testcontainers y necesita Docker. |
| GJ-107 — Gateway y despliegue | `.github/workflows/ci-qa.yml`, `.github/workflows/ci-prod.yml`; ejecuciones CI QA #20 y #37, CI PROD #1 | App Service habilita WebSockets y el smoke test QA pasó. CI QA #37 (fusión del PR #6 en `develop`) terminó en verde con despliegue a Azure QA. PROD se creó y se desplegó con el tag `v1.0.0` (CI PROD #1 en verde). Falta validar handshake STOMP extremo a extremo a través del gateway usado por frontend. |
| GJ-108 — Observabilidad y documentación | `WebSocketSessionObserver`, `ChatMetrics`, logs de error Redis, README y contrato STOMP | Se registran aperturas/cierres de sesiones y errores de distribución sin registrar tokens ni cuerpos; métricas expuestas en `/actuator/prometheus`. Faltan dashboards de conexiones y entregas. |
| GJ-109 — Flujos de tienda y donaciones | `ChatIntegrationTest`, `StoreAndDonationChatControllerTest`, adapters y repositorios | Los casos existen en el código. La evidencia de cierre requiere ejecutar los casos actualizados y mostrar permisos, inicio, persistencia, bandeja y respuestas con servicios integrados; esta actualización documental no ejecutó pruebas. |

## Despliegue

| Entorno | URL | Estado |
| --- | --- | --- |
| QA | `https://gj-chat-service-qa-santi-gxhjccffcxdgbfcj.canadacentral-01.azurewebsites.net` | Desplegado desde `develop` (CI QA #37). Swagger con las 7 rutas en inglés. |
| PROD | `https://gj-chat-service-prod-santi.azurewebsites.net` | Desplegado con el tag `v1.0.0` (CI PROD #1, con aprobación manual del ambiente `production`). Swagger con las 7 rutas en inglés. |

Ambos entornos exponen `/swagger-ui.html`, `/actuator/health` y el WebSocket en `/ws`. PROD comparte plan de App Service con QA, y usa por ahora el perfil `qa`, el broker RabbitMQ de QA y los servicios de tiendas y donaciones de QA.

## Verificación local

- Registro histórico: `mvn -B clean verify` terminó en BUILD SUCCESS; 179 pruebas, 0 fallos, 0 omitidas y umbral JaCoCo alcanzado (ejecución con Docker Desktop). No se repitió en esta revisión.
- Registro histórico: `RedisRealtimeMultiInstanceTest` verificó entrega entre instancias con Redis real en Docker.
- Registro histórico: Swagger local mostró las 7 rutas en inglés.

## Estado de cierre de tareas Sprint 3

1. **GJ-56 (Chat con almacén aliado)**: El código de esta copia incluye contrato REST, mensaje inicial, bandeja y autorización por `ADMIN_ALMACEN`/NIT. Pendientes la validación actual con frontend, ms-tiendas y ms-notificaciones; esta revisión no ejecutó pruebas ni hizo despliegue.
2. **GJ-57 (Chat de donaciones)**: El código y la ruta están presentes. Pendientes la prueba de integración con ms-segunda-vida-donaciones y frontend, y acordar/probar si cambios posteriores de estado cierran el chat.
3. **GJ-109 (Pruebas de flujos de tienda y donaciones)**: No cerrar todavía. Ejecutar pruebas de ambos flujos integrados; incluir inicio, permisos por NIT/solicitud, persistencia, bandeja del almacén y respuesta. En donaciones, acordar y probar el cierre ante cambios posteriores de estado si forma parte del ciclo de vida requerido.

## Pendientes

- Para GJ-56: validar contra ms-tiendas real que un comprador puede iniciar con NIT aprobado y que un administrador solo abre/responde chats del NIT asociado.
- Para GJ-56: el ZIP de frontend recibido usa rutas provisionales; acordar y cambiar cliente a `POST /api/v1/stores/{nit}/conversations` con `{"texto":"..."}` y `GET /api/v1/stores/conversations`.
- Para GJ-56: confirmar que ms-notificaciones acepta `destinatarioId=null` y usa `almacenNit` para resolver administradores. Hasta esa confirmación no declarar completa la notificación del chat de tienda.
- Para SR-RF-41: acordar con ms-auth una forma de detectar cuentas bloqueadas/inactivas y revocación posterior a la emisión del JWT; firma y expiración no bastan para conocer ese estado.
- Para SR-RNF-15: acordar con `ms-notificaciones` y el responsable de mensajería cómo se garantiza y evidencia la entrega de notificaciones. En ms-chat, confirms/returns y métricas informan sobre el intento de publicación; no certifican la entrega al usuario.
- Aprobación funcional de SR-RF-40, SR-RF-41, SR-RF-73 y GJ-57: pendiente de los responsables del requerimiento; no se asigna una aprobación por el solo hecho de documentar o implementar.
- Para GJ-56: volver a desplegar la rama ya fusionada y capturar evidencia de QA; los cambios de esta copia local no están en QA/PROD.
- Rechazar cuentas bloqueadas con token vigente: falta acordar con ms-identidad cómo consultar el estado de la cuenta.
- Crear Azure Cache for Redis en QA y guardar su URL como secret `REDIS_URL_QA` (GJ-55 y GJ-107).
- Crear un perfil `prod` y un broker RabbitMQ propio para PROD.
- Acordar explícitamente el contrato STOMP con frontend.
