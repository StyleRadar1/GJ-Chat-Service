# Trazabilidad técnica — Sprint 3 ms-chat

Fecha de revisión: 12 de noviembre de 2026. Fuente del backlog: exportación Jira `guatoque_jiraguas_styleradar_2026-11-12_07.04pm.csv` y captura del tablero. El CSV contiene claves, resúmenes y estados, pero no descripciones ni criterios de aceptación. Este documento registra evidencia del repositorio; no actualiza el estado de las incidencias en Jira.

## Avance de implementación local

- `ConversationClosedException` ahora se traduce a HTTP `409`; por STOMP el emisor recibe el conflicto en `/user/queue/conversations/errors`. El mensaje no se confirma ni se difunde.
- El envío STOMP confirma al emisor en `/user/queue/conversations/acks` después de que el caso de uso retorna el mensaje persistido.
- Se añadieron pruebas para JWT inválido, SEND y SUBSCRIBE de no participantes, destinos privados, orden persistencia-confirmación-difusión, conversación cerrada y fallo de Redis conservando entrega local.
- La compilación y 27 pruebas focalizadas pasaron inicialmente; luego también se ejecutó Redis real con Docker.
- El contrato STOMP está documentado como propuesta; falta acordarlo explícitamente con los clientes frontend.
- Verificación completa con Docker: `mvn -B -Dmaven.repo.local=.m2-cache clean verify` terminó BUILD SUCCESS; 100 pruebas, 0 fallos y 0 omitidas. Incluyó Redis Pub/Sub real entre dos instancias y PostgreSQL con Testcontainers; JaCoCo pasó su umbral.

## Evidencia inicial

| Jira | Evidencia revisada | Estado técnico observado |
| --- | --- | --- |
| GJ-54 — STOMP en tiempo real | `WebSocketConfig`, `RealtimeChatController`, pruebas STOMP unitarias | Base funcional; autenticación y persistencia antes de la difusión. Pendiente prueba con dos clientes reales y captura/video. |
| GJ-55 — Redis Pub/Sub | `RedisRealtimeMessageBroadcaster`, `RealtimeMessagingConfiguration`, `application.yml`, perfil `realtime` de Docker Compose | Implementación opcional y aislada. Redis de QA no está provisionado ni configurado; mantener una instancia hasta validarlo. |
| GJ-56 — Chat con almacén aliado | `ConversationType.ALMACEN`, `StoreQueryAdapter`, `StoresFeignClient`, `StartStoreConversationUseCase`, `GetStoreConversationsUseCase`, `StoreAndDonationChatControllerTest` | Implementado y probado. Modelo extendido con tipo `ALMACEN`, NIT y nombre; contrato validado contra ms-tiendas; endpoints `/api/v1/almacenes/...` securizados con rol `ADMIN_ALMACEN` y soporte en STOMP. Migración Flyway V3 aplicada. |
| GJ-57 — Chat de donaciones | `ConversationType.DONACION`, `DonationQueryAdapter`, `DonationsFeignClient`, `StartDonationConversationUseCase`, `StoreAndDonationChatControllerTest` | Implementado y probado. Modelo extendido con tipo `DONACION` e identificador de donación; contrato validado contra ms-donaciones; endpoint `/api/v1/donations/{id}/conversations` con soporte para comprador/donante y representante en STOMP. Migración Flyway V3 aplicada. |
| GJ-103 — Contrato STOMP | `docs/contrato-websocket-stomp.md` | Destinos, payload, confirmación privada, error 409 e historial documentados; falta aprobación explícita de frontend. |
| GJ-104 — Seguridad WebSocket | `JwtStompChannelInterceptor` y `JwtStompChannelInterceptorTest` | CONNECT exige JWT; SEND y SUBSCRIBE validan participación y reclamo de rol admin almacén/donación. Cobertura completa de ramas en tests unitarios. |
| GJ-105 — Flujo de mensajes | `SendMessageUseCaseImpl`, `MessageRepositoryAdapter`, REST historial y `RealtimeChatControllerTest` | Persistencia antes de confirmación privada y difusión; historial recuperable por REST. Sin estados de lectura ni clave idempotente de cliente. |
| GJ-106 — Redis y varias instancias | `RedisRealtimeMessageBroadcasterTest` y `RedisRealtimeMultiInstanceTest` | Unitarias cubren relay, deduplicación de eco y fallo Redis conservando entrega local. La prueba con Redis real usa Testcontainers y necesita Docker. |
| GJ-107 — Gateway y despliegue | `.github/workflows/ci-qa.yml`; evidencia de CI QA #20 | App Service habilita WebSockets y el smoke test QA pasó. Falta validar handshake STOMP extremo a extender a través del gateway usado por frontend. |
| GJ-108 — Observabilidad y documentación | `WebSocketSessionObserver`, logs de error Redis, README y contrato STOMP | Se registran aperturas/cierres de sesiones y errores de distribución sin registrar tokens ni cuerpos. Faltan métricas/dashboards de conexiones y entregas. |
| GJ-109 — Flujos de tienda y donaciones | `StoreAndDonationChatControllerTest`, adapters y repositorios | Implementado y probado. Pruebas automáticas completas para creación, consulta, permisos y excepciones en almacén y donaciones. Cobertura JaCoCo del proyecto en 95.7% (superando el umbral de 85%). |

## Verificación local

- `mvn -B -Dmaven.repo.local=.m2-cache -Dtest=RedisRealtimeMessageBroadcasterTest test`: 3 pruebas aprobadas.
- `mvn -B -Dmaven.repo.local=.m2-cache clean verify`: BUILD SUCCESS; 100 pruebas, 0 fallos, 0 omitidas y umbral JaCoCo alcanzado (ejecución actual con Docker Desktop).
- `RedisRealtimeMultiInstanceTest` verificó entrega desde una instancia hacia otra mediante un Redis real en Docker.

## Estado de cierre de tareas Sprint 3

1. **GJ-56 (Chat con almacén aliado)**: Cerrado técnicamente. Contrato con ms-tiendas integrado, entidad y modelo ampliados, endpoints habilitados y autorización por rol `ADMIN_ALMACEN` implementada.
2. **GJ-57 (Chat de donaciones)**: Cerrado técnicamente. Contrato con ms-donaciones integrado, modelo adaptado para donaciones y endpoints habilitados.
3. **GJ-109 (Pruebas de flujos de tienda y donaciones)**: Cerrado técnicamente. Suite de pruebas unitarias y de integración completa, superando el umbral de cobertura JaCoCo (95.7%).
