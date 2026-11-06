<div align="center">

  <a href="https://github.com/StyleRadar1">
    <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/logo/styleradar-logo.svg" width="300" alt="StyleRadar Logo Oficial" />
  </a>

  <h1>STYLE<font color="#5600EF">RADAR</font> &nbsp;·&nbsp; <code>SR_GJ_AnalisisTecnologias_v1</code></h1>

  <p align="center">
    <img src="https://img.shields.io/badge/MICROSERVICIO-ms--chat-12063A?style=for-the-badge" alt="Servicio" />
    <img src="https://img.shields.io/badge/JAVA-17-5600EF?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java" />
    <img src="https://img.shields.io/badge/SPRING_BOOT-3.2.12-16A34A?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot" />
  </p>

  <p align="center"><em>Análisis de tecnologías de ms-chat · DOSW 2026-2</em></p>

</div>

| Campo | Valor |
| --- | --- |
| Proyecto | StyleRadar · DOSW 2026-2 |
| Alcance | Microservicio de chat (GJ-Chat-Service) |
| Equipo | Guatoque Jiraguas (GJ) · Responsable: Santiago Andrés García Almario |
| Versión · Fecha | v1.0 · 3 de noviembre de 2026 |

---

<a id="01-contexto-y-drivers"></a>

## <img src="https://img.shields.io/badge/01-5600EF?style=for-the-badge" height="20" align="absmiddle" /> CONTEXTO Y DRIVERS

El chat permite que comprador y vendedor de una prenda de segunda mano coordinen la entrega (SR-RF-40 y SR-RF-41). Antes vivía dentro del monolito; se separa para que pueda evolucionar a tiempo real (WebSocket) y escalar sin afectar el catálogo.

| Atributo de calidad | Escenario objetivo | Impacto en la selección |
| --- | --- | --- |
| Consistencia | Una sola conversación por publicación y comprador, incluso con peticiones simultáneas | Base relacional con restricciones `UNIQUE` y transacciones ACID |
| Rendimiento | Leer los mensajes de una conversación en orden, rápido | Índice compuesto por conversación y fecha |
| Seguridad | Solo los participantes ven y escriben en la conversación | JWT validado en cada petición y regla de participante en el dominio |
| Disponibilidad | Si el servicio de publicaciones cae, el chat responde un error claro y no se bloquea | Tiempos límite en Feign y error 503 controlado |
| Mantenibilidad | El equipo entrega en un sprint de una semana | Stack que el equipo ya domina (Java + Spring) |
| Costo | Ambiente académico de bajo costo | Herramientas open source y contenedores |

**Restricciones del curso:** arquitectura hexagonal, Docker, cobertura JaCoCo ≥ 85 %, Swagger en cada servicio y Database per Service.

---

<a id="02-metodología"></a>

## <img src="https://img.shields.io/badge/02-5600EF?style=for-the-badge" height="20" align="absmiddle" /> METODOLOGÍA

Cada decisión se evalúa con cinco criterios ponderados (escala 1 a 5). El total es la suma de calificación × peso.

| Criterio | Peso | Qué mide |
| --- | --- | --- |
| C1 · Ajuste al problema | 30 % | Qué tan bien resuelve las necesidades concretas del chat |
| C2 · Madurez y soporte | 20 % | Estabilidad, documentación y comunidad |
| C3 · Velocidad del equipo | 20 % | Curva de aprendizaje y librerías disponibles |
| C4 · Operabilidad | 15 % | Docker, CI/CD, observabilidad |
| C5 · Costo | 15 % | Licencia y costo en la nube |

---

<a id="03-decisiones-por-capa-adr"></a>

## <img src="https://img.shields.io/badge/03-5600EF?style=for-the-badge" height="20" align="absmiddle" /> DECISIONES POR CAPA (ADR)

### ADR-01 · Lenguaje y framework

| Criterio (peso) | **Java 17 + Spring Boot 3.2** | Node.js + NestJS | Python + FastAPI |
| --- | --- | --- | --- |
| C1 (30 %) | 5 | 4 | 4 |
| C2 (20 %) | 5 | 4 | 4 |
| C3 (20 %) | 5 | 3 | 3 |
| C4 (15 %) | 5 | 4 | 4 |
| C5 (15 %) | 5 | 5 | 5 |
| **Total** | **5.00** | 3.95 | 3.95 |

**Decisión:** Java 17 con Spring Boot 3.2, como pide la guía del curso.
**Por qué:** el monolito ya está en Spring, así que el código del chat se migra sin reescribirlo. Spring Security, Data JPA, Validation, Actuator y OpenFeign cubren todo lo que el servicio necesita.
**Por qué no los otros:** obligarían a reescribir las reglas y pruebas ya existentes y a aprender otro ecosistema dentro del sprint.
**Consecuencia:** el monolito usa Java 21; el curso debe unificar la versión.

### ADR-02 · Base de datos

| Criterio (peso) | **PostgreSQL 15** | MongoDB | Redis |
| --- | --- | --- | --- |
| C1 (30 %) | 5 | 3 | 2 |
| C2 (20 %) | 5 | 5 | 5 |
| C3 (20 %) | 5 | 4 | 3 |
| C4 (15 %) | 5 | 4 | 5 |
| C5 (15 %) | 5 | 4 | 4 |
| **Total** | **5.00** | 3.85 | 3.45 |

**Decisión:** PostgreSQL relacional, una base propia (`chat_db`), con esquema versionado con Flyway.
**Por qué:** el modelo es relacional (mensajes de una conversación) y la regla «una conversación por publicación y comprador» se garantiza con una restricción `UNIQUE`, incluso con peticiones simultáneas. Es la misma base que usa el monolito y Azure la ofrece administrada.
**Por qué no los otros:** MongoDB no garantiza esa unicidad entre peticiones concurrentes con la misma sencillez; Redis no es adecuado como almacenamiento principal de un historial de mensajes.

### ADR-03 · Comunicación con otros servicios

| Criterio (peso) | **Feign (REST) + RabbitMQ (eventos)** | Solo REST | Solo eventos | Java RMI |
| --- | --- | --- | --- | --- |
| C1 (30 %) | 5 | 3 | 3 | 1 |
| C2 (20 %) | 5 | 5 | 5 | 3 |
| C3 (20 %) | 4 | 5 | 3 | 3 |
| C4 (15 %) | 5 | 5 | 4 | 1 |
| C5 (15 %) | 5 | 5 | 5 | 5 |
| **Total** | **4.80** | 4.40 | 3.90 | 2.50 |

**Decisión:** REST con Feign solo cuando se necesita una respuesta inmediata (validar la publicación al iniciar el chat) y eventos en RabbitMQ para lo demás (`MensajeEnviadoEvent`, `PrendaSegundaManoVendidaEvent` y, si se aprueba, `PrendaSegundaManoRetiradaEvent`) desde el Sprint 2. Contratos en [`SR_GJ_ContratosEventos_v1.md`](SR_GJ_ContratosEventos_v1.md).
**Por qué:** iniciar un chat no puede esperar a que llegue un evento; avisar a notificaciones o cerrar el chat sí puede, y con eventos el chat no depende de que el otro servicio esté arriba.
**Por qué no RMI:** el cliente es un navegador, que no habla RMI; además no pasa por el gateway HTTP y obliga a abrir puertos por usuario.

### ADR-04 · Seguridad

**Decisión:** el servicio valida el mismo JWT HS256 que emite el servicio de identidad, sin consultar la base de usuarios, y cada endpoint exige el rol con `@PreAuthorize`. Los errores salen en formato RFC 7807.
**Por qué:** así se cumple «autenticación centralizada, autorización en cada microservicio» sin acoplarse a la base de otro servicio.
**Riesgo:** el token actual no trae roles; mientras identidad no agregue el claim `roles`, se asume COMPRADOR. Se recomienda migrar a RS256, con la llave privada solo en identidad.

### ADR-05 · Tiempo real (Sprint 3)

**Decisión:** WebSocket con STOMP para el chat en tiempo real, como indica la guía del curso (adaptador `adapter/in/websocket`).
**Por qué:** la comunicación del chat va en dos sentidos y el navegador la soporta de forma nativa a través del gateway.

### ADR-06 · Calidad y entrega

| Necesidad | Herramienta |
| --- | --- |
| Pruebas unitarias y de casos de uso | JUnit 5 + Mockito |
| Pruebas de integración con base real | Testcontainers (PostgreSQL 15) |
| Cobertura ≥ 85 % | JaCoCo |
| Análisis estático | Checkstyle (Google Java Style) |
| Mappers sin código repetitivo | MapStruct |
| Documentación de la API | springdoc-openapi (Swagger UI) |
| Contenedores | Docker multi-stage (Temurin 17 JRE Alpine) y Docker Compose |
| CI/CD | GitHub Actions → Docker Hub → Azure App Service |

---

<a id="04-costos-fase-académica"></a>

## <img src="https://img.shields.io/badge/04-5600EF?style=for-the-badge" height="20" align="absmiddle" /> COSTOS (FASE ACADÉMICA)

| Componente | Opción | Costo estimado |
| --- | --- | --- |
| Cómputo | Azure App Service para contenedores (plan B1 compartido entre servicios) o Container Apps con escala a cero | Bajo / gratuito con créditos de estudiante |
| Base de datos | Azure Database for PostgreSQL Flexible Server B1ms (compartida, una base por servicio) | Bajo |
| Imágenes | Docker Hub (repositorio público) | Gratis |
| CI/CD | GitHub Actions (repositorio público) | Gratis |

Para no multiplicar costos con 13 servicios, se recomienda compartir el plan de cómputo y el servidor PostgreSQL (con una base separada por servicio).

---

<a id="05-riesgos-y-mitigación"></a>

## <img src="https://img.shields.io/badge/05-5600EF?style=for-the-badge" height="20" align="absmiddle" /> RIESGOS Y MITIGACIÓN

| ID | Riesgo | Prob. / Imp. | Mitigación |
| --- | --- | --- | --- |
| R1 | El servicio de publicaciones no responde | Media / Medio | Tiempos límite en Feign y error 503 controlado; en el Sprint 2 el cierre pasa a eventos |
| R2 | El JWT no trae roles | Alta / Medio | Rol por defecto configurable y acuerdo con identidad para agregar `roles` |
| R3 | Testcontainers incompatible con Docker 29 en CI | Alta / Bajo | `docker-java.properties` con `api.version=1.44` |
| R4 | Versión de Java distinta entre servicios | Media / Bajo | Definirla en el curso (17 según la guía, 21 en el monolito) |

---

<a id="06-fila-del-servicio-en-la-matriz-del-curso"></a>

## <img src="https://img.shields.io/badge/06-5600EF?style=for-the-badge" height="20" align="absmiddle" /> FILA DEL SERVICIO EN LA MATRIZ DEL CURSO

| Servicio | Equipo | ¿BD? | Tipo de BD | Framework | Broker | Puerto |
| --- | --- | --- | --- | --- | --- | --- |
| GJ-Chat-Service | Guatoque Jiraguas (GJ) | Sí | Relacional · PostgreSQL 15 (`chat_db`) | Spring Boot 3.2 · Java 17 | RabbitMQ (publica `MensajeEnviadoEvent`, consume `PrendaSegundaManoVendidaEvent`) | 8320 |

<a id="07-conclusión"></a>

## <img src="https://img.shields.io/badge/07-5600EF?style=for-the-badge" height="20" align="absmiddle" /> CONCLUSIÓN

El stack elegido permite migrar el chat del monolito sin reescribir sus reglas, cumplir la arquitectura hexagonal y los requisitos de calidad del curso, y dejar listo el camino para eventos (Sprint 2) y tiempo real (Sprint 3).
