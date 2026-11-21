# Fichas funcionales de ms-chat

Estado documental: 20 de noviembre de 2026. Las rutas y respuestas se contrastaron con el código de esta copia local y con el reporte de estado de `develop`; esta ficha no acredita por sí sola que los cambios estén desplegados ni reemplaza la aprobación funcional.

## Convenciones de respuestas

Las respuestas de éxito y las respuestas de error se documentan en apartados distintos. Los nombres siguientes son los campos JSON públicos de la API; no son nombres de clases Java.

### Datos de salida en error

Las respuestas REST de error usan `application/problem+json`.

| Campo JSON | Tipo | Significado |
| --- | --- | --- |
| `type` | Texto (URI) | Identificador de la categoría del error. |
| `title` | Texto | Título breve del error. |
| `status` | Entero | Código HTTP de la respuesta. |
| `detail` | Texto | Explicación específica de la causa. |
| `instance` | Texto (ruta URI) | Ruta que produjo el error. |
| `timestamp` | Texto (fecha y hora ISO-8601) | Momento en que se generó el error. |
| `errores` | Objeto, opcional | Errores por campo cuando falla la validación de la solicitud. |

Cada endpoint indica por separado sus códigos HTTP y el caso que produce cada uno. El campo `detail` debe describir la causa concreta; no se debe copiar la salida de error dentro de la tabla de éxito.

Ejemplo de error de validación (el ejemplo va fuera de la tabla para que los campos sigan siendo legibles):

```json
{
  "type": "https://styleradar.dosw/errores/solicitud-invalida",
  "title": "Solicitud inválida",
  "status": 400,
  "detail": "Revise los campos enviados",
  "instance": "/api/v1/conversations/7/messages",
  "timestamp": "2026-11-20T15:05:00Z",
  "errores": { "texto": "El mensaje no puede estar vacío" }
}
```

### Datos de salida de una conversación

Este objeto se devuelve al iniciar o consultar conversaciones.

| Campo JSON | Tipo | Significado |
| --- | --- | --- |
| `id` | Entero | Identificador de la conversación. |
| `tipo` | Texto | Tipo: `SEGUNDA_MANO`, `ALMACEN` o `DONACION`. |
| `publicacionId` | Entero o `null` | Identificador de la publicación de segunda mano, si aplica. |
| `prendaNombre` | Texto o `null` | Nombre de la prenda, si aplica. |
| `compradorId` | Entero | Identificador del comprador o usuario que inició el chat. |
| `vendedorId` | Entero o `null` | Identificador del vendedor, si aplica. |
| `almacenNit` | Texto o `null` | NIT del almacén, si aplica. |
| `almacenNombre` | Texto o `null` | Nombre del almacén, si aplica. |
| `donacionId` | Entero o `null` | Identificador de la solicitud de donación, si aplica. |
| `estado` | Texto | Estado actual de la conversación, por ejemplo `ABIERTA` o `CERRADA`. |
| `fechaCreacion` | Texto (fecha y hora ISO-8601) | Fecha de creación de la conversación. |

### Datos de salida de un mensaje

Este objeto se devuelve al consultar o enviar mensajes.

| Campo JSON | Tipo | Significado |
| --- | --- | --- |
| `id` | Entero | Identificador del mensaje guardado. |
| `conversacionId` | Entero | Conversación a la que pertenece. |
| `autorId` | Entero | Usuario que envió el mensaje. |
| `texto` | Texto | Contenido del mensaje. |
| `fechaEnvio` | Texto (fecha y hora ISO-8601) | Fecha en que se guardó el mensaje. |

### Sobre de salida paginado

Los endpoints de historial devuelven un objeto con `content` (elementos), `page` (base cero), `size`, `totalElements` y `totalPages`. Se puede solicitar `?page=0&size=20`; el máximo permitido es 100. Los mensajes mantienen orden cronológico ascendente dentro de cada página.

## SR-RF-40 · Iniciar chat con el vendedor

**Flujo principal**

1. El comprador selecciona una publicación y envía el mensaje inicial obligatorio.
2. El servicio busca primero la conversación asociada a ese comprador y publicación.
3. Si ya existe, devuelve esa conversación sin volver a consultar la disponibilidad de la publicación; no crea una sala duplicada ni vuelve a insertar el mensaje inicial. El historial se consulta en el endpoint de mensajes.
4. Si no existe, consulta la publicación. Solo si existe, está disponible y pertenece a otra persona crea la conversación y persiste el mensaje inicial.

**Alternativas:** texto vacío o inválido → `400`; publicación inexistente → `404`; propia o no disponible al crear chat nuevo → `422`; servicio de publicaciones no disponible → `503`.

| Aspecto | Definición |
| --- | --- |
| Actor | Usuario comprador (rol `COMPRADOR` en el claim `roles`). |
| Operación | `POST /api/v1/second-hand/publications/{publicationId}/conversations` |
| Solicitud | JSON `{"texto":"..."}`. El texto inicial es obligatorio en la petición y admite de 1 a 1.000 caracteres. |
| Regla | Primero buscar la conversación existente para ese comprador y publicación. Si existe, devolverla aunque la publicación esté vendida o retirada. Solo para crear una nueva, validar que la publicación exista, esté disponible y pertenezca a otra persona. |
| Éxito | `200 OK`. Devuelve los campos de **Datos de salida de una conversación**. Al crearla, persiste y publica el mensaje inicial mediante el caso de uso de envío. Al reutilizarla, no duplica el mensaje inicial. |
| Errores | `401` JWT ausente, inválido o expirado; `403` rol sin permiso; `404` publicación inexistente; `422` publicación propia o no disponible; `503` servicio de publicaciones no disponible. El cuerpo sigue **Datos de salida en error**. |
| Implementación relacionada | `StartConversationUseCaseImpl`, `SecondHandQueryAdapter` (consulta `GET /api/v1/second-hand/publications/{id}`); pruebas en `ChatControllerTest` y `ChatIntegrationTest`. |
| Estado | Implementado en el código de esta copia: mensaje inicial obligatorio para REST, búsqueda idempotente previa a consultar disponibilidad y recuperación de conversación existente. La integración cliente y su despliegue deben validarse por separado. |
| Aprobación | Pendiente de aprobación funcional del responsable del requerimiento. |

## SR-RF-41 · Coordinar la entrega mediante chat

**Flujo principal:** un participante solicita su bandeja o el historial paginado; el servicio deriva su identidad del JWT, comprueba participación (o administración del NIT para chat de almacén), recupera la página solicitada y devuelve `content`, `page`, `size`, `totalElements` y `totalPages`. Para enviar, valida texto y estado abierto, persiste el mensaje y responde con el mensaje guardado.

**Alternativas:** JWT inválido o ausente → `401`; usuario sin permiso/participación → `403` o recurso no visible `404` según endpoint; parámetros de página fuera de rango → `400`; conversación cerrada → `409` sin guardar el mensaje.

### Consultar mensajes

| Aspecto | Definición |
| --- | --- |
| Actores | Usuario comprador (participantes de la conversación: comprador y dueño de la prenda, ambos con rol oficial `COMPRADOR`). La participación se valida por usuario y conversación. |
| Operación | `GET /api/v1/conversations/{id}/messages` |
| Parámetros | `page` (base cero, predeterminado `0`) y `size` (predeterminado `50`, máximo `100`). |
| Éxito | `200 OK`. Devuelve el **Sobre de salida paginado**, con mensajes en orden cronológico ascendente; `content` contiene los campos de **Datos de salida de un mensaje**. |
| Errores | `401` JWT ausente, inválido o expirado; `403` rol sin permiso; `404` conversación inexistente o ajena. El cuerpo sigue **Datos de salida en error**. |
| Pendiente de seguridad | La verificación actual de firma, expiración e identidad del JWT no detecta por sí sola que una cuenta haya sido bloqueada después de emitirse el token. Acordar con ms-auth e implementar una consulta de estado, introspección o revocación; rechazar la operación antes de leer o escribir el historial. |
| Estado | Historiales paginados implementados en esta copia; detección de cuentas bloqueadas/inactivas con JWT vigente sigue pendiente de integración con ms-auth. |
| Aprobación | Pendiente de aprobación funcional del responsable del requerimiento. |

### Enviar mensaje

| Aspecto | Definición |
| --- | --- |
| Actores | Participantes autorizados según tipo de chat: para segunda mano, `Usuario comprador` (rol `COMPRADOR`); para almacén, `Usuario comprador` (`COMPRADOR`) y `Administrador de almacén` (`ADMIN_ALMACEN`); para donación, `Usuario comprador` (`COMPRADOR`) y `Representante de fundación` (`REPRESENTANTE_FUNDACION`). |
| Operación | `POST /api/v1/conversations/{id}/messages` |
| Solicitud | JSON `{"texto":"..."}`. El texto es obligatorio y admite de 1 a 1.000 caracteres. |
| Éxito | `201 Created`. Devuelve los campos de **Datos de salida de un mensaje** para el mensaje persistido. |
| Errores | `400` texto inválido; `401` JWT ausente, inválido o expirado; `403` rol sin permiso; `404` conversación inexistente o ajena; `409` conversación cerrada (el mensaje no se guarda); `429` límite de envío excedido; `503` servicio externo requerido no disponible. El cuerpo sigue **Datos de salida en error**. |

### Consultar conversaciones propias

| Aspecto | Definición |
| --- | --- |
| Operación | `GET /api/v1/users/conversations`. El usuario se obtiene del JWT; no se recibe un identificador de usuario en la ruta. |
| Parámetros | `page` (base cero, predeterminado `0`) y `size` (predeterminado `20`, máximo `100`). |
| Éxito | `200 OK`. Devuelve el **Sobre de salida paginado**; `content` contiene los campos de **Datos de salida de una conversación**. |
| Errores | `401` JWT ausente, inválido o expirado; `403` rol sin permiso. El cuerpo sigue **Datos de salida en error**. |

**Pendientes SR-RF-41:** la paginación se encuentra implementada en esta copia y debe conservarse en el contrato de los clientes. Sigue pendiente acordar con ms-auth e implementar el control de cuentas bloqueadas/inactivas con JWT vigente. La aprobación funcional de la ficha permanece pendiente.

### Mensajería en tiempo real

| Acción | Destino / respuesta |
| --- | --- |
| Conectar | STOMP sobre `/ws`, con JWT Bearer validado durante el handshake. |
| Enviar | `/app/conversations/{conversationId}/messages`. |
| Recibir mensajes | Suscripción a `/topic/conversations/{conversationId}`. |
| Confirmación privada | `/user/queue/conversations/acks`, con los datos del mensaje guardado. |
| Error privado | `/user/queue/conversations/errors`, con `status`, `code` y `detail`. |

Solo los participantes autorizados pueden suscribirse o enviar mensajes. El mensaje se guarda antes de confirmar y difundirlo. Una conversación cerrada se rechaza con estado lógico `409`.

## SR-RF-73 · Contactar a un almacén por chat

**Flujo principal:** comprador envía NIT y primer mensaje → ms-chat valida el almacén con ms-tiendas → crea o recupera la conversación del comprador y NIT, y guarda el primer mensaje → un administrador autenticado consulta la bandeja propia, se valida el NIT asociado y responde en la conversación. Mensajes e historial comparten el contrato de SR-RF-41.

**Alternativas:** almacén no existe/no está aprobado → `422`; administrador sin almacén asociado o de otro NIT → acceso denegado; ms-tiendas no disponible → `503`; conversación cerrada → `409` y no se guarda mensaje.

| Aspecto | Definición |
| --- | --- |
| Actores | Comprador (`COMPRADOR`) y administrador del almacén (`ADMIN_ALMACEN`); la relación entre administrador y NIT se valida con ms-tiendas. |
| Iniciar conversación | `POST /api/v1/stores/{nit}/conversations`, con JSON `{"texto":"..."}` (1–1.000 caracteres). |
| Consultar bandeja | `GET /api/v1/stores/conversations`, para un administrador asociado a un almacén aprobado. |
| Salida al iniciar | `200 OK`. Devuelve los campos de **Datos de salida de una conversación**. La conversación se crea o reutiliza por comprador y NIT, y el mensaje inicial queda guardado. |
| Salida de bandeja | `200 OK`. Devuelve el **Sobre de salida paginado** (`page`, `size`, `totalElements`, `totalPages` y `content`); `content` contiene conversaciones. `size` admite hasta 100. |
| Errores al iniciar | `400` texto inválido; `401` JWT ausente, inválido o expirado; `403` rol sin permiso; `422` almacén inexistente o no disponible para chat; `503` ms-tiendas no disponible. El cuerpo sigue **Datos de salida en error**. |
| Errores de bandeja | `401` JWT ausente, inválido o expirado; `403` usuario sin almacén aprobado o sin autorización; `503` ms-tiendas no disponible. El cuerpo sigue **Datos de salida en error**. |
| Seguridad | La identidad del administrador y el NIT se verifican con ms-tiendas. Por STOMP, el administrador solo accede a conversaciones del mismo NIT. |
| Pendiente de integración | Confirmar que ms-notificaciones admite `destinatarioId` nulo y enruta con `almacenNit`; comprobar compatibilidad de los endpoints con el frontend. |
| Estado | Implementación del flujo REST, modelo y autorización por NIT reportada en `develop`; las validaciones contra ms-tiendas desplegado, frontend y consumidor de ms-notificaciones siguen pendientes antes de cierre integral. |
| Aprobación | Pendiente de aprobación funcional del responsable del requerimiento. |

## GJ-57 · Chat sobre una solicitud de donación

**Flujo principal:** donante o representante de la fundación abre el chat de una solicitud → ms-chat verifica si ya hay conversación y que el solicitante participe; si no existe, consulta ms-segunda-vida-donaciones y valida que la solicitud esté activa → guarda/recupera la conversación → participantes consultan historial paginado y envían mensajes.

**Alternativas:** usuario ajeno → acceso denegado; solicitud inexistente → `404`; solicitud inactiva para una conversación nueva → `422`; servicio de donaciones no disponible → `503`; conversación cerrada → `409` sin guardar mensaje.

El identificador de seguimiento disponible en Jira es **GJ-57**. El código formal `SR-RF-NN` aún debe asignarlo quien administra el catálogo de requerimientos; no se inventa un consecutivo. Esta ficha conserva la misma estructura usada para los demás requisitos de ms-chat.

| Aspecto | Definición |
| --- | --- |
| Actores | Donante (`COMPRADOR`) y representante de la fundación (`REPRESENTANTE_FUNDACION`) vinculados a la solicitud. Los roles canónicos vienen en el claim JWT `roles` como arreglo. |
| Operación | `POST /api/v1/donations/{donationId}/conversations` |
| Regla | Para crear una conversación nueva, la solicitud debe existir y estar en estado activo; solo el donante o la fundación vinculados pueden crearla. Si ya existe, el participante vinculado puede recuperarla sin volver a consultar el estado externo. El envío posterior solo comprueba que la conversación local siga abierta; el código actual no recibe eventos de cierre/cambio de estado de la solicitud para cerrar automáticamente el chat. |
| Éxito | `200 OK`. Devuelve los campos de **Datos de salida de una conversación**. |
| Errores | `401` JWT ausente, inválido o expirado; `403` usuario ajeno a la donación; `404` solicitud inexistente; `422` donación finalizada; `503` ms-donaciones no disponible. El cuerpo sigue **Datos de salida en error**. |
| Implementación relacionada | `StartDonationConversationUseCaseImpl`, `DonationQueryAdapter` (consulta `GET /api/v1/donations/{id}`) y pruebas de `StoreAndDonationChatControllerTest`. |
| Estado | GJ-57 está incorporado en el código y en la trazabilidad de ms-chat. Falta validar extremo a extremo con ms-segunda-vida-donaciones y frontend; también debe acordarse si los cambios de estado posteriores deben cerrar el chat y qué evento comunicará ese cierre. |
| Aprobación | Pendiente de aprobación funcional del responsable del requerimiento. |

## SR-RNF-15 · Resiliencia e idempotencia de eventos (Adenda v4)

Esta ficha incorpora los criterios corregidos en la **Adenda correctiva v4** del documento de requerimientos general de StyleRadar.

| Aspecto | Texto corregido / Definición |
| --- | --- |
| Qué demuestran las confirmaciones | `Publisher confirms` permiten saber si RabbitMQ aceptó la publicación; `publisher returns` permiten detectar mensajes no enrutados cuando la publicación es mandatory. |
| Qué no garantizan | Ni las confirmaciones ni el registro/metricado del error conservan por sí solos un evento que no pudo publicarse. Si falla RabbitMQ después de guardar el mensaje de chat, el mensaje puede quedar persistido sin aviso asociado. |
| Estado actual | Parcial: los fallos de publicación se detectan y registran; eso mejora la observabilidad, pero no asegura reintento durable ni recuperación automática. |
| Mecanismo pendiente | Implementar outbox transaccional (o mecanismo durable equivalente): guardar el evento en la misma transacción que el mensaje, publicarlo mediante un relay con reintentos, marcarlo confirmado y permitir recuperar eventos pendientes. Mantener consumidores idempotentes. |
| Garantía objetivo | Entrega al menos una vez con consumidores idempotentes y sin pérdida silenciosa; no declarar entrega exactamente una vez. |

### Criterios y evidencia de aceptación pendiente
- Simular RabbitMQ caído luego del commit del mensaje y demostrar que el evento queda en outbox y se publica al recuperarse el broker.
- Comprobar reintentos, recuperación tras reinicio del servicio, estado de eventos fallidos y ausencia de notificaciones duplicadas.
- En `ms-chat`, el evento `MensajeEnviadoEvent` se publica en `styleradar.exchange` con routing key `styleradar.chat.mensaje.enviado` y sobre estándar `EventEnvelope` enriquecido con campos planos para compatibilidad directa con el consumidor de `ms-notificaciones`.

## Historial de revisiones

| Versión | Fecha | Autor | Cambios y justificación |
| --- | --- | --- | --- |
| v1.0 | 29/10/2026 | Equipo DOSW – StyleRadar | Versión inicial generada a partir de plantilla. |
| v2.0 | 12/11/2026 | Daniel Barrera / Oscar Lasso | Reescrita con el código de ms-notificaciones y ms-chat. Fusión de chat con mensaje inicial, orden de validación C2C y reglas de tiempo real. |
| v3.0 | 18/11/2026 | Oscar Lasso (Equipo GJ) | Correcciones de la revisión docente: separación estricta de salidas de éxito y error RFC 7807, historiales paginados y eliminación de nombres de clases internas. |
| v4.0 | 20/11/2026 | Santiago García (Equipo GJ) | Alineación completa con **Adenda v4**: incorporación de SR-RNF-15 (delimitación de publisher confirms vs outbox transaccional), actualización de rutas Feign (`/api/v1/second-hand/publications/{id}` y `/api/v1/donations/{id}`), compatibilidad híbrida en evento hacia ms-notificaciones y estandarización OpenAPI en inglés sin prefijos de código. |

## Notas y decisiones

Las decisiones técnicas y sus identificadores (por ejemplo, D-11 a D-15), así como los números de PR, se registran en Jira o en el anexo de decisiones del equipo, no dentro de las fichas funcionales. Esta copia de ms-chat no contiene esas notas identificadas.

## Trazabilidad y estado

Esta ficha describe las rutas y respuestas presentes en el código de ms-chat. Para el estado de implementación, pruebas y despliegue, consultar [`docs/evidencias/sprint-3-trazabilidad.md`](evidencias/sprint-3-trazabilidad.md). Un estado de implementación en código no implica que el flujo haya sido verificado de extremo a extremo con frontend y servicios externos.
