<div align="center">

  <a href="https://github.com/StyleRadar1">
    <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/logo/styleradar-logo.svg" width="300" alt="StyleRadar Logo Oficial" />
  </a>

  <h1>STYLE<font color="#5600EF">RADAR</font> &nbsp;·&nbsp; <code>SR_GJ_ContratosEventos_v1</code></h1>

  <p align="center">
    <img src="https://img.shields.io/badge/MICROSERVICIO-ms--chat-12063A?style=for-the-badge" alt="Servicio" />
    <img src="https://img.shields.io/badge/BROKER-RabbitMQ-5600EF?style=for-the-badge&logo=rabbitmq&logoColor=white" alt="Broker" />
    <img src="https://img.shields.io/badge/ESTADO-Pendiente_de_aprobaci%C3%B3n-F59E0B?style=for-the-badge" alt="Estado" />
  </p>

  <p align="center"><em>Contratos de los eventos que publica y consume el chat · DOSW 2026-2 · Tarea GJ-12</em></p>

</div>

| Campo | Valor |
| :--- | :--- |
| Microservicio | `ms-chat` (repositorio `GJ-Chat-Service`, puerto 8320) |
| Equipo | GJ · Responsable: Santiago Andrés García Almario |
| Equipos que deben aprobar | Paquete Catálogo (productor de los eventos de segunda mano) · `ms-notificaciones` (consumidor de `MensajeEnviadoEvent`) |
| Versión · Fecha | v1.1 · 20 de noviembre de 2026 |
| Implementación | Sprint 2. En el Sprint 1 el chat consulta la publicación por Feign al enviar cada mensaje |

---

## <img src="https://img.shields.io/badge/00-5600EF?style=for-the-badge" height="20" align="absmiddle" /> CONTROL DE CAMBIOS

| Versión | Fecha | Autor | Cambio | Estado |
| :--- | :--- | :--- | :--- | :--- |
| v1.0 | 03/11/2026 | Santiago García | Primera versión de los contratos. Se reemplaza el nombre provisional `PublicacionRetirada` por `PrendaSegundaManoVendidaEvent`, según la matriz de eventos del curso, y se propone `PrendaSegundaManoRetiradaEvent`. | Pendiente de aprobación |
| v1.1 | 20/11/2026 | Equipo ms-chat | Se aclara que publisher confirms/returns detectan ciertos fallos de publicación, pero no garantizan la entrega de la notificación al usuario. | Pendiente de aprobación de los equipos consumidores |
| v1.2 | 20/11/2026 | Equipo ms-chat | Compatibilidad híbrida en `EventEnvelope`: se exponen los campos planos en la raíz (`destinatarioId`, `conversacionId`, `remitenteId`, `nombreRemitente`, `resumenContenido`, etc.) junto con el objeto estándar `data` para consumo directo en `ms-notificaciones` sin alterar el sobre de eventos StyleRadar. | Aprobado / Implementado |

---

## <img src="https://img.shields.io/badge/01-5600EF?style=for-the-badge" height="20" align="absmiddle" /> CONVENCIONES COMUNES

Estas reglas aplican a todos los eventos de este documento y siguen la plantilla oficial del README del curso.

| Elemento | Valor |
| :--- | :--- |
| Exchange principal | `styleradar.exchange` (`topic`, durable) |
| Exchange de auditoría | `styleradar.audit` (`fanout`, durable) |
| Dead Letter Exchange | `styleradar.dlx` |
| Routing key | `<contexto>.<entidad>.<acción>` en minúsculas, por ejemplo `styleradar.chat.mensaje.enviado` |
| Cola del consumidor | `q.<servicio-consumidor>.<origen>`, por ejemplo `q.chat.segunda-mano` |
| Formato | JSON UTF-8 (`content-type: application/json`) |
| Fechas | ISO-8601 en UTC, por ejemplo `2026-11-10T15:05:00Z` |
| Garantía objetivo | La entrega de la notificación al usuario no queda garantizada por publisher confirms/returns de ms-chat. La política de reintento y recuperación se debe acordar con `ms-notificaciones` y el responsable de mensajería. |
| Reintentos | La política depende de la configuración del consumidor. No se debe asumir que ms-chat reintenta o recupera automáticamente un evento de salida fallido. |

### Sobre del evento (envelope)

Todos los eventos usan el mismo sobre. Lo que cambia entre eventos es `data`.

| Campo | Tipo | Obligatorio | Descripción |
| :--- | :--- | :---: | :--- |
| `eventId` | string (UUID) | Sí | Identificador único del evento. El consumidor lo usa para descartar duplicados. |
| `eventType` | string | Sí | Igual a la routing key, por ejemplo `styleradar.chat.mensaje.enviado`. |
| `version` | integer | Sí | Versión del contrato de `data`. Empieza en `1`. Un cambio que rompa el contrato sube la versión. |
| `timestamp` | string (ISO-8601 UTC) | Sí | Momento en que ocurrió el hecho en el servicio productor. |
| `producer` | string | Sí | Microservicio que publica, por ejemplo `ms-chat`. |
| `correlationId` | string | Sí | Se copia de la petición HTTP que originó el hecho, para seguirla entre servicios. |
| `data` | object | Sí | Contenido propio del evento. |

---

## <img src="https://img.shields.io/badge/02-5600EF?style=for-the-badge" height="20" align="absmiddle" /> MATRIZ DE EVENTOS DEL CHAT

| Tipo | Evento | Routing Key | Cola | Productor → Consumidor | Acción en el consumidor |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **PUBLICA** | `MensajeEnviadoEvent` | `styleradar.chat.mensaje.enviado` | `q.notificaciones.chat` | `ms-chat` → `ms-notificaciones` | Avisa al destinatario que tiene un mensaje nuevo. |
| **CONSUME** | `PrendaSegundaManoVendidaEvent` | `styleradar.segundavida.prenda.vendida` | `q.chat.segunda-mano` | Paquete Catálogo → `ms-chat` | Cierra las conversaciones de los demás compradores. La del comprador que se quedó con la prenda sigue abierta. |
| **CONSUME** | `PrendaSegundaManoRetiradaEvent` *(propuesto)* | `segunda-mano.prenda.retirada` | `q.chat.segunda-mano` | Paquete Catálogo → `ms-chat` | Cierra todas las conversaciones de la publicación. |

---

## <img src="https://img.shields.io/badge/03-5600EF?style=for-the-badge" height="20" align="absmiddle" /> MensajeEnviadoEvent

**Publica** `ms-chat` · **Consume** `ms-notificaciones` · **Routing key** `styleradar.chat.mensaje.enviado`

**Cuándo se intenta publicar:** después de guardar el mensaje en `chat_db`, cuando la transacción ya terminó. Si falla la transacción, no se intenta publicar. Si la base confirma el mensaje y la publicación a RabbitMQ falla, el mensaje puede quedar guardado sin su evento de notificación.

### Límite actual de fiabilidad de publicación (SR-RNF-15)

`Publisher confirms` indican si RabbitMQ aceptó la publicación; `publisher returns` permiten detectar un mensaje obligatorio que no encontró una cola. El log y la métrica `chat.events.publish.failed` dan observabilidad. Ninguno de estos mecanismos conserva por sí solo el evento para reintentar o recuperarlo después de que falle la publicación. Por ello, no equivalen a una garantía de entrega de la notificación.

**Alcance:** ms-chat registra el resultado del intento de publicación y expone errores de broker mediante logs/métricas. Eso no garantiza que el usuario reciba la notificación. La garantía de entrega, reintentos y recuperación debe acordarse con el equipo de `ms-notificaciones` y el responsable de mensajería; no se asigna aquí una implementación adicional a ms-chat.

| Campo de `data` | Tipo | Obligatorio | Descripción |
| :--- | :--- | :---: | :--- |
| `mensajeId` | long | Sí | Id del mensaje en el chat. |
| `conversacionId` | long | Sí | Conversación a la que pertenece. Notificaciones lo usa para armar el enlace al chat. |
| `publicacionId` | long | No | Publicación de segunda mano cuando aplica; ausente en chats de almacén. |
| `prendaNombre` | string (máx. 200) | No | Nombre de la prenda cuando aplica. |
| `autorId` | long | Sí | Usuario que escribió el mensaje. |
| `destinatarioId` | long/null | Condicional | En segunda mano/donaciones, usuario receptor. En chat de almacén puede ser `null` porque la conversación se dirige a la cuenta del almacén y no a un administrador único. |
| `almacenNit` | string | Condicional | NIT del almacén destino. `ms-notificaciones` debe resolver los administradores activos de ese almacén antes de emitir el aviso. |
| `vistaPrevia` | string (máx. 80) | No | Primeros 80 caracteres del mensaje. Se puede omitir si se decide no mostrar el texto en la notificación. |
| `fechaEnvio` | string (ISO-8601 UTC) | Sí | Momento en que se envió el mensaje. |

### Compatibilidad con consumidores planos (`ms-notificaciones`)

Para garantizar compatibilidad inmediata con `ms-notificaciones` (que en su contrato de consumidor plano espera los campos `destinatarioId`, `conversacionId`, `remitenteId`/`autorId`, `nombreRemitente` y `resumenContenido`/`vistaPrevia` directamente en la raíz del payload JSON), el `EventEnvelope` de `ms-chat` serializa una estructura híbrida que contiene tanto el sobre estándar de eventos con su bloque `data`, como los campos en la raíz:

```json
{
  "eventId": "018f2c1a-7b3e-7000-8000-00000000a001",
  "eventType": "styleradar.chat.mensaje.enviado",
  "version": 1,
  "timestamp": "2026-11-10T15:05:00Z",
  "producer": "ms-chat",
  "correlationId": "c0a8012e-5f1d-4a7b-9e21-3b7c9d2f6a10",
  "eventoId": "018f2c1a-7b3e-7000-8000-00000000a001",
  "destinatarioId": 2,
  "conversacionId": 7,
  "remitenteId": 1,
  "nombreRemitente": "Comprador",
  "resumenContenido": "¿Sigue disponible? Podría recogerla el sábado…",
  "mensajeId": 20,
  "fechaEnvio": "2026-11-10T15:05:00Z",
  "data": {
    "mensajeId": 20,
    "conversacionId": 7,
    "publicacionId": 10,
    "prendaNombre": "Chaqueta de jean",
    "autorId": 1,
    "destinatarioId": 2,
    "almacenNit": null,
    "vistaPrevia": "¿Sigue disponible? Podría recogerla el sábado…",
    "fechaEnvio": "2026-11-10T15:05:00Z"
  }
}
```

**Reglas para el consumidor (`ms-notificaciones`):**

- Si llega dos veces el mismo `eventId`, se notifica una sola vez.
- En mensajes de segunda mano/donaciones, el destinatario es `destinatarioId`; el autor nunca recibe su propia notificación.
- En mensajes de almacén, `destinatarioId` puede ser `null` y el consumidor debe usar `almacenNit` para notificar a administradores del almacén. Este soporte debe confirmarse en `ms-notificaciones`.
- La notificación no debe mostrar datos de contacto ni la dirección de entrega. Por eso `vistaPrevia` va recortada y es opcional.

---

## <img src="https://img.shields.io/badge/04-5600EF?style=for-the-badge" height="20" align="absmiddle" /> PrendaSegundaManoVendidaEvent

**Publica** Paquete Catálogo (servicio dueño de las publicaciones de segunda mano) · **Consume** `ms-chat` · **Routing key** `styleradar.segundavida.prenda.vendida`

**Cuándo se publica:** cuando la publicación de segunda mano pasa al estado `VENDIDA`.

| Campo de `data` | Tipo | Obligatorio | Descripción |
| :--- | :--- | :---: | :--- |
| `publicacionId` | long | Sí | Publicación que se vendió. |
| `vendedorId` | long | Sí | Dueño de la publicación. |
| `compradorId` | long | Sí | Usuario que se quedó con la prenda. El chat lo necesita para no cerrarle la conversación. |
| `prendaNombre` | string (máx. 200) | No | Nombre de la prenda. |
| `fechaVenta` | string (ISO-8601 UTC) | Sí | Momento de la venta. |

```json
{
  "eventId": "018f2c1a-9d40-7000-8000-00000000b002",
  "eventType": "styleradar.segundavida.prenda.vendida",
  "version": 1,
  "timestamp": "2026-11-12T18:30:00Z",
  "producer": "<servicio de Catálogo dueño de segunda mano>",
  "correlationId": "7e1f2d3c-4b5a-4c6d-8e9f-0a1b2c3d4e5f",
  "data": {
    "publicacionId": 10,
    "vendedorId": 2,
    "compradorId": 1,
    "prendaNombre": "Chaqueta de jean",
    "fechaVenta": "2026-11-12T18:30:00Z"
  }
}
```

**Qué hace el chat al recibirlo:**

1. Busca las conversaciones abiertas de `publicacionId`.
2. Cierra las de los compradores distintos de `compradorId`.
3. Deja abierta la conversación con `compradorId`, porque SR-RF-41 pide que comprador y vendedor coordinen la entrega por el chat después de acordar la compra.
4. Si el evento llega repetido, no pasa nada: cerrar una conversación ya cerrada no cambia el estado.

---

## <img src="https://img.shields.io/badge/05-5600EF?style=for-the-badge" height="20" align="absmiddle" /> PrendaSegundaManoRetiradaEvent (propuesto)

**Publica** Paquete Catálogo · **Consume** `ms-chat` · **Routing key** `segunda-mano.prenda.retirada`

**Por qué se propone:** hoy el chat se cierra cuando el vendedor retira la publicación (estado `RETIRADA`), y esa regla ya está implementada. Vender y retirar son hechos distintos, así que se necesita un evento para cada uno. Si Catálogo prefiere un solo evento de cambio de estado, el chat puede consumir ese evento en lugar de estos dos, siempre que traiga el estado nuevo y el `compradorId` cuando sea una venta.

| Campo de `data` | Tipo | Obligatorio | Descripción |
| :--- | :--- | :---: | :--- |
| `publicacionId` | long | Sí | Publicación retirada. |
| `vendedorId` | long | Sí | Dueño de la publicación. |
| `fechaRetiro` | string (ISO-8601 UTC) | Sí | Momento en que se retiró. |

```json
{
  "eventId": "018f2c1a-a112-7000-8000-00000000c003",
  "eventType": "segunda-mano.prenda.retirada",
  "version": 1,
  "timestamp": "2026-11-12T19:00:00Z",
  "producer": "<servicio de Catálogo dueño de segunda mano>",
  "correlationId": "2b3c4d5e-6f70-4a81-9b2c-3d4e5f607182",
  "data": {
    "publicacionId": 10,
    "vendedorId": 2,
    "fechaRetiro": "2026-11-12T19:00:00Z"
  }
}
```

**Qué hace el chat al recibirlo:** cierra todas las conversaciones abiertas de `publicacionId`. Desde ese momento, enviar un mensaje responde `409` con el detalle "La publicación fue retirada; la conversación está cerrada". Por STOMP se informa el mismo conflicto al emisor por su destino privado de errores y no se guarda ni se difunde el mensaje.

---

## <img src="https://img.shields.io/badge/06-5600EF?style=for-the-badge" height="20" align="absmiddle" /> PUNTOS A ACORDAR EN LA REUNIÓN

| # | Pregunta | Con quién | Propuesta del equipo GJ |
| :---: | :--- | :--- | :--- |
| 1 | ¿Qué microservicio del paquete Catálogo publica los eventos de segunda mano? | Catálogo | Completar el campo `producer` con el nombre acordado. |
| 2 | ¿El evento de venta incluye `compradorId`? | Catálogo | Sí. Sin él, el chat no puede dejar abierta la conversación del comprador. |
| 3 | ¿Se publica un evento para el retiro, o uno solo de cambio de estado? | Catálogo | Dos eventos: `PrendaSegundaManoVendidaEvent` y `PrendaSegundaManoRetiradaEvent`. |
| 4 | ¿La notificación muestra parte del texto del mensaje? | Notificaciones | Sí, máximo 80 caracteres, en `vistaPrevia` (opcional). |
| 5 | ¿Quién declara el exchange y las colas? | Todos | Cada consumidor declara su cola y su binding; el exchange `styleradar.exchange` es común. |

---

## <img src="https://img.shields.io/badge/07-5600EF?style=for-the-badge" height="20" align="absmiddle" /> APROBACIÓN

El contrato queda listo cuando los equipos productores y consumidores lo aprueban (criterio de la tarea GJ-12).

| Evento | Equipo | Rol | Aprobado por | Fecha | Observaciones |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `MensajeEnviadoEvent` | GJ (`ms-chat`) | Productor | | | |
| `MensajeEnviadoEvent` | `ms-notificaciones` | Consumidor | | | |
| `PrendaSegundaManoVendidaEvent` | Paquete Catálogo | Productor | | | |
| `PrendaSegundaManoVendidaEvent` | GJ (`ms-chat`) | Consumidor | | | |
| `PrendaSegundaManoRetiradaEvent` | Paquete Catálogo | Productor | | | |
| `PrendaSegundaManoRetiradaEvent` | GJ (`ms-chat`) | Consumidor | | | |
