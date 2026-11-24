[⬅ Volver al README principal](../../README.md)

---

## 5. Tecnologías y dependencias

| Área | Tecnología |
| --- | --- |
| Lenguaje / framework | Java 21, Spring Boot 3.2.12, Spring Cloud OpenFeign |
| REST y tiempo real | Spring MVC, OpenAPI 3, WebSocket/STOMP |
| Datos | PostgreSQL, Spring Data JPA, Flyway |
| Mensajería | RabbitMQ 3.13, Spring AMQP |
| Relay entre instancias | Redis 7, opcional por configuración |
| Seguridad | Spring Security, JWT HS256, clave compartida con ms-auth |
| Calidad | JUnit, Mockito, JaCoCo (umbral configurado 85 %), Checkstyle |
| Contenedores | Docker / Docker Compose |

### Servicios relacionados

- `ms-auth` emite los JWT.
- Servicio de segunda mano devuelve publicación, vendedor y disponibilidad.
- `ms-tiendas` comprueba el almacén y resuelve el almacén asociado al administrador.
- `ms-segunda-vida-donaciones` devuelve la solicitud y sus participantes/estado.
- RabbitMQ comunica mensajes y eventos del ciclo de vida de publicaciones.
- `ms-notificaciones` consume eventos de mensaje.
- PostgreSQL es propio; no hay acceso directo a bases ajenas.

La integración de cada ambiente debe validarse con sus URLs y contratos efectivos.
