# Fichas funcionales de ms-chat

Estado documental: 18 de noviembre de 2026. Las rutas y respuestas se contrastaron con el código de esta copia del repositorio. Esta ficha no acredita por sí sola que los cambios estén desplegados.

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

## SR-RF-40 · Iniciar chat con el vendedor

| Aspecto | Definición |
| --- | --- |
| Actor | Comprador (`USER` en el token de identidad). |
| Operación | `POST /api/v1/second-hand/publications/{publicationId}/conversations` |
| Regla | La publicación debe estar disponible y pertenecer a otra persona. Si ya existe la conversación, se devuelve la existente. |
| Éxito | `200 OK`. Devuelve los campos de **Datos de salida de una conversación**. |
| Errores | `401` JWT ausente, inválido o expirado; `403` rol sin permiso; `404` publicación inexistente; `422` publicación propia o no disponible; `503` servicio de publicaciones no disponible. El cuerpo sigue **Datos de salida en error**. |
| Implementación relacionada | `StartConversationUseCaseImpl`; pruebas en `ChatControllerTest` y `ChatIntegrationTest`. |

## SR-RF-41 · Coordinar la entrega mediante chat

### Consultar mensajes

| Aspecto | Definición |
| --- | --- |
| Actores | Comprador y vendedor que participan en la conversación. Ambos usan el rol oficial `USER`; la participación se valida por usuario y conversación. |
| Operación | `GET /api/v1/conversations/{id}/messages` |
| Éxito | `200 OK`. Devuelve una lista de mensajes; cada elemento contiene los campos de **Datos de salida de un mensaje**. |
| Errores | `401` JWT ausente, inválido o expirado; `403` rol sin permiso; `404` conversación inexistente o ajena. El cuerpo sigue **Datos de salida en error**. |

### Enviar mensaje

| Aspecto | Definición |
| --- | --- |
| Actores | Comprador y vendedor participantes (`USER`). En otros tipos de conversación se admiten los participantes autorizados para ese flujo. No usar el actor genérico «Otro participante». |
| Operación | `POST /api/v1/conversations/{id}/messages` |
| Solicitud | JSON `{"texto":"..."}`. El texto es obligatorio y admite de 1 a 1.000 caracteres. |
| Éxito | `201 Created`. Devuelve los campos de **Datos de salida de un mensaje** para el mensaje persistido. |
| Errores | `400` texto inválido; `401` JWT ausente, inválido o expirado; `403` rol sin permiso; `404` conversación inexistente o ajena; `409` conversación cerrada (el mensaje no se guarda); `429` límite de envío excedido; `503` servicio externo requerido no disponible. El cuerpo sigue **Datos de salida en error**. |

### Consultar conversaciones propias

| Aspecto | Definición |
| --- | --- |
| Operación | `GET /api/v1/users/conversations`. El usuario se obtiene del JWT; no se recibe un identificador de usuario en la ruta. |
| Éxito | `200 OK`. Devuelve una lista de conversaciones; cada elemento contiene los campos de **Datos de salida de una conversación**. |
| Errores | `401` JWT ausente, inválido o expirado; `403` rol sin permiso. El cuerpo sigue **Datos de salida en error**. |

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

| Aspecto | Definición |
| --- | --- |
| Actores | Comprador (`USER`) y administrador del almacén (`STORE`). |
| Iniciar conversación | `POST /api/v1/stores/{nit}/conversations`, con JSON `{"texto":"..."}` (1–1.000 caracteres). |
| Consultar bandeja | `GET /api/v1/stores/conversations`, para un administrador asociado a un almacén aprobado. |
| Salida al iniciar | `200 OK`. Devuelve los campos de **Datos de salida de una conversación**. La conversación se crea o reutiliza por comprador y NIT, y el mensaje inicial queda guardado. |
| Salida de bandeja | `200 OK`. Devuelve una lista de conversaciones; cada elemento contiene los campos de **Datos de salida de una conversación**. |
| Errores al iniciar | `400` texto inválido; `401` JWT ausente, inválido o expirado; `403` rol sin permiso; `422` almacén inexistente o no disponible para chat; `503` ms-tiendas no disponible. El cuerpo sigue **Datos de salida en error**. |
| Errores de bandeja | `401` JWT ausente, inválido o expirado; `403` usuario sin almacén aprobado o sin autorización; `503` ms-tiendas no disponible. El cuerpo sigue **Datos de salida en error**. |
| Seguridad | La identidad del administrador y el NIT se verifican con ms-tiendas. Por STOMP, el administrador solo accede a conversaciones del mismo NIT. |
| Pendiente de integración | Confirmar que ms-notificaciones admite `destinatarioId` nulo y enruta con `almacenNit`; comprobar compatibilidad de los endpoints con el frontend. |

## GJ-57 · Chat sobre una solicitud de donación

El identificador de seguimiento disponible en Jira es **GJ-57**. El código formal `SR-RF-NN` aún debe asignarlo quien administra el catálogo de requerimientos; no se inventa un consecutivo. Esta ficha conserva la misma estructura usada para los demás requisitos de ms-chat.

| Aspecto | Definición |
| --- | --- |
| Actores | Donante (`USER`) y representante de la fundación (`FOUNDATION`) vinculados a la solicitud. |
| Operación | `POST /api/v1/donations/{donationId}/conversations` |
| Regla | La solicitud debe existir y seguir activa. Solo el donante o la fundación vinculados pueden acceder a la conversación y enviar mensajes. |
| Éxito | `200 OK`. Devuelve los campos de **Datos de salida de una conversación**. |
| Errores | `401` JWT ausente, inválido o expirado; `403` usuario ajeno a la donación; `404` solicitud inexistente; `422` donación finalizada; `503` ms-donaciones no disponible. El cuerpo sigue **Datos de salida en error**. |
| Implementación relacionada | `StartDonationConversationUseCaseImpl`, `DonationQueryAdapter` y pruebas de `StoreAndDonationChatControllerTest`. |

## Notas y decisiones

Las decisiones técnicas y sus identificadores (por ejemplo, D-11 a D-15), así como los números de PR, se registran en Jira o en el anexo de decisiones del equipo, no dentro de las fichas funcionales. Esta copia de ms-chat no contiene esas notas identificadas.

## Trazabilidad y estado

Esta ficha describe las rutas y respuestas presentes en el código de ms-chat. Para el estado de implementación, pruebas y despliegue, consultar [`docs/evidencias/sprint-3-trazabilidad.md`](evidencias/sprint-3-trazabilidad.md). Un estado de implementación en código no implica que el flujo haya sido verificado de extremo a extremo con frontend y servicios externos.
