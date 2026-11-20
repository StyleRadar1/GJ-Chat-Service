<!-- README modularizado según la plantilla común StyleRadar de 12 secciones. -->

<div align="center">
  <a href="https://github.com/StyleRadar1"><img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/logo/styleradar-logo.svg" width="340" alt="StyleRadar" /></a>
  <h1>STYLE<font color="#5600EF">RADAR</font> · <code>ms-chat</code></h1>
  <p>
    <img src="https://img.shields.io/badge/BOUNDED_CONTEXT-Mensajer%C3%ADa-12063A?style=for-the-badge" alt="Mensajería" />
    <img src="https://img.shields.io/badge/PUERTO-8320-5600EF?style=for-the-badge" alt="Puerto 8320" />
    <img src="https://img.shields.io/badge/ARQUITECTURA-HEXAGONAL-16A34A?style=for-the-badge" alt="Arquitectura hexagonal" />
    <img src="https://img.shields.io/badge/SQUAD-GJ-12063A?style=for-the-badge" alt="Squad GJ" />
  </p>
  <p><em>Mensajería REST y WebSocket/STOMP para segunda mano, almacenes aliados y donaciones.</em></p>
  <p>
    <a href="https://github.com/StyleRadar1/GJ-Chat-Service/actions/workflows/ci-qa.yml"><img src="https://github.com/StyleRadar1/GJ-Chat-Service/actions/workflows/ci-qa.yml/badge.svg" alt="CI QA" /></a>
    <a href="https://github.com/StyleRadar1/GJ-Chat-Service/actions/workflows/ci-prod.yml"><img src="https://github.com/StyleRadar1/GJ-Chat-Service/actions/workflows/ci-prod.yml/badge.svg" alt="CI PROD" /></a>
  </p>
</div>

---

## Índice

1. [Nombre del servicio y bounded context](docs/secciones/01-nombre-del-servicio-y-bounded-context.md)
2. [Arquitectura hexagonal y diagramas](docs/secciones/02-arquitectura-hexagonal-y-diagramas.md)
3. [Endpoints y Swagger](docs/secciones/03-endpoints-y-enlace-a-swagger.md)
4. [Eventos publicados y consumidos](docs/secciones/04-eventos-que-publica-y-consume.md)
5. [Tecnologías y dependencias](docs/secciones/05-tecnologias-y-dependencias.md)
6. [Ejecución local y Docker](docs/secciones/06-como-ejecutar-local-y-docker.md)
7. [Variables de entorno](docs/secciones/07-variables-de-entorno.md)
8. [Pruebas y cobertura](docs/secciones/08-pruebas-y-cobertura.md)
9. [CI/CD](docs/secciones/09-cicd-pipeline.md)
10. [Ramas y contribución](docs/secciones/10-ramas-y-como-contribuir.md)
11. [Seguridad y OWASP](docs/secciones/11-seguridad-y-owasp.md)
12. [Equipo y Jira](docs/secciones/12-equipo-responsables-y-epica-jira.md)

## Documentación de ms-chat

| Documento | Para qué sirve |
| --- | --- |
| [Fichas funcionales](docs/fichas-requerimientos-chat.md) | Flujos, entradas, salidas, errores y estado de SR-RF-40/41/73 y GJ-57 |
| [Trazabilidad Sprint 3](docs/evidencias/sprint-3-trazabilidad.md) | Estado observado, evidencia y dependencias externas |
| [Contrato STOMP](docs/contrato-websocket-stomp.md) | Destinos, payloads, autenticación y confirmaciones |
| [Contrato de eventos](docs/SR_GJ_ContratosEventos_v1.md) | RabbitMQ, productores/consumidores y alcance de entrega de notificaciones |
| [Documento técnico](docs/SR_GJ_DocumentoTecnico_v1.md) | Arquitectura, clases, datos, despliegue y contratos |

> El contenido describe el código y los estados reportados, no certifica que la copia local esté desplegada ni sustituye la aprobación de los responsables. Ver [trazabilidad](docs/evidencias/sprint-3-trazabilidad.md) para los pendientes de integración.
