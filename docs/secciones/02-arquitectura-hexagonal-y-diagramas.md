[⬅ Volver al README principal](../../README.md)

---

## 2. Arquitectura hexagonal con sus diagramas

El dominio y los casos de uso están aislados de Spring, JPA, HTTP, RabbitMQ y STOMP. Los adaptadores de infraestructura implementan puertos de entrada y salida.

### Diagramas del servicio

| Diagrama | Archivo PNG | Fuente editable |
| --- | --- | --- |
| Casos de uso | [casos-de-uso-ms-chat.png](../diagramas/casos-de-uso-ms-chat.png) | [draw.io](../diagramas/casos-de-uso-ms-chat.drawio) |
| Componentes generales StyleRadar con ms-chat resaltado | [componentes-general.png](../diagramas/componentes-general.png) | [draw.io](../diagramas/componentes-general.drawio) |
| Despliegue local, QA y PROD | [despliegue.png](../diagramas/despliegue.png) | [draw.io](../diagramas/despliegue.drawio) |
| Componentes internos e integraciones de ms-chat | [componentes-ms-chat.png](../diagramas/componentes-ms-chat.png) | [draw.io](../diagramas/componentes-ms-chat.drawio) |
| Clases del dominio | [modelo-de-dominio-clases.png](../diagramas/modelo-de-dominio-clases.png) | [draw.io](../diagramas/modelo-de-dominio-clases.drawio) |
| Modelo entidad-relación | [modelo-entidad-relacion.png](../diagramas/modelo-entidad-relacion.png) | [draw.io](../diagramas/modelo-entidad-relacion.drawio) |
| Flujo de mensajería y dependencias | [flujos-ms-chat.png](../diagramas/flujos-ms-chat.png) | [draw.io](../diagramas/flujos-ms-chat.drawio) |
| Secuencia de envío de mensaje | [secuencia-enviar-mensaje.png](../diagramas/secuencia-enviar-mensaje.png) | [draw.io](../diagramas/secuencia-enviar-mensaje.drawio) |
| Arquitectura hexagonal por capas | [arquitectura-hexagonal-capas.png](../diagramas/arquitectura-hexagonal-capas.png) | [draw.io](../diagramas/arquitectura-hexagonal-capas.drawio) |
| Arquitectura hexagonal detallada | [arquitectura-hexagonal-detalle.png](../diagramas/arquitectura-hexagonal-detalle.png) | [draw.io](../diagramas/arquitectura-hexagonal-detalle.drawio) |
| CI/CD y ramas | [pipeline-ci-cd.png](../diagramas/pipeline-ci-cd.png), [estructura-de-ramas.png](../diagramas/estructura-de-ramas.png) | archivos `.drawio` homónimos |

Los diagramas distinguen relaciones síncronas Feign, mensajería RabbitMQ y relay opcional Redis. El código no consulta directamente bases de datos de otros servicios. Consulta [el índice de diagramas](../diagramas/README.md) y el [documento técnico](../SR_GJ_DocumentoTecnico_v1.md) para el detalle.
