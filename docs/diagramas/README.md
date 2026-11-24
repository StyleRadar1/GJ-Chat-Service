# Diagramas de ms-chat

Actualización de alcance: 20 de noviembre de 2026. Los diagramas de dominio y base de datos incluyen `SEGUNDA_MANO`, `ALMACEN` y `DONACION`; también se incorporan casos de uso y flujos para los tres tipos de conversación. La fuente editable es el `.drawio`; el `.png` permite revisar y adjuntar la vista.

Los archivos `.drawio` son editables en [draw.io](https://app.diagrams.net). Las imágenes PNG acompañan los documentos y usan una paleta coordinada con StyleRadar (morado `#48119A`, relleno lavanda `#E4DBF0`); los diagramas específicos mantienen la notación propia de cada tipo.

| Diagrama | Archivo | Dónde se usa |
| :--- | :--- | :--- |
| Capas de la arquitectura hexagonal | `arquitectura-hexagonal-capas` | README, sección 3 |
| Pipeline CI/CD | `pipeline-ci-cd` | README, sección de CI/CD |
| Estructura de ramas | `estructura-de-ramas` | README, estrategia de ramas |
| Contexto del sistema | `contexto-del-sistema` | Documento técnico, 2.1 |
| Casos de uso de ms-chat | `casos-de-uso-ms-chat` | README modular, arquitectura |
| Componentes generales | `componentes-general` | Documento técnico, 2.2 |
| Despliegue | `despliegue` | Documento técnico, 2.3 |
| Flujos de conversación | `flujos-ms-chat` | README modular, arquitectura |
| Hexagonal en detalle | `arquitectura-hexagonal-detalle` | Documento técnico, 04 |
| Secuencia: enviar un mensaje | `secuencia-enviar-mensaje` | Documento técnico, 04 |
| Clases del dominio | `modelo-de-dominio-clases` | Documento técnico, 05 |
| Entidad-relación | `modelo-entidad-relacion` | Documento técnico, 06 |
| Componentes específicos de ms-chat | `componentes-ms-chat` | Documento técnico, arquitectura Sprint 3 |

Los diagramas de casos de uso, componentes, despliegue, flujos, clases y base de datos se actualizaron para reflejar el alcance y las dependencias actuales de ms-chat. Los diagramas generales del curso siguen siendo una vista parcial desde este microservicio.

## Cómo editar un diagrama

1. Abre el `.drawio` en draw.io (web o escritorio).
2. Mantén la notación UML adecuada para el tipo de diagrama y aplica la paleta acordada.
3. Exporta con *Archivo → Exportar como → PNG* (fondo blanco) con el mismo nombre del archivo.
4. Sube el `.drawio` y el `.png` en el mismo commit.
