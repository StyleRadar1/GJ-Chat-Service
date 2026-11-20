[⬅ Volver al README principal](../../README.md)

---

## 4. Eventos que publica y consume

ms-chat usa RabbitMQ para eventos asíncronos. El intercambio y routing se describen en el [contrato de eventos](../SR_GJ_ContratosEventos_v1.md).

| Tipo | Evento | Dirección | Acción |
| --- | --- | --- | --- |
| Publica | `MensajeEnviadoEvent` (`styleradar.chat.mensaje.enviado`) | ms-chat → ms-notificaciones | Solicita notificar un mensaje nuevo |
| Consume | `PrendaSegundaManoVendidaEvent` (`styleradar.segundavida.prenda.vendida`) | Catálogo → ms-chat | Cierra conversaciones de compradores distintos al comprador de la venta |
| Consume | `PrendaSegundaManoRetiradaEvent` (`segunda-mano.prenda.retirada`, propuesto) | Catálogo → ms-chat | Cierra conversaciones de la publicación retirada |

Se habilitan Publisher Confirms y Returns. Estos permiten detectar aceptación o falta de ruta, pero **no garantizan por sí solos que el usuario reciba la notificación** ni definen su recuperación. La política de reintentos y entrega final se debe acordar con `ms-notificaciones` y el responsable de mensajería (SR-RNF-15); esta sección no asigna una implementación adicional a ms-chat. Los contratos continúan sujetos a aprobación de los equipos productores y consumidores.
