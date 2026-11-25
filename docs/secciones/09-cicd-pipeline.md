[⬅ Volver al README principal](../../README.md)

---

## 9. CI/CD con el badge del pipeline

### Badges de Estado en Tiempo Real

[![CI QA](https://github.com/StyleRadar1/GJ-Chat-Service/actions/workflows/ci-qa.yml/badge.svg)](https://github.com/StyleRadar1/GJ-Chat-Service/actions/workflows/ci-qa.yml)
[![CI PROD](https://github.com/StyleRadar1/GJ-Chat-Service/actions/workflows/ci-prod.yml/badge.svg)](https://github.com/StyleRadar1/GJ-Chat-Service/actions/workflows/ci-prod.yml)

---

### Workflows del Repositorio

| Archivo | Activación | Responsabilidad |
|---|---|---|
| `.github/workflows/ci-qa.yml` | Push y PR a `develop` / tags QA | Validación Maven (test + cobertura JaCoCo 85%), auditoría Checkstyle, construcción de imagen multi-stage Docker Hub y despliegue continuo en Azure App Service QA. |
| `.github/workflows/ci-prod.yml` | Push a `master` / tags `v*.*.*` | Validación completa, empaquetado Docker Hub (`latest` y versión), configuración de variables de entorno y despliegue automatizado a Azure App Service PROD con Smoke Test. |
| `.github/workflows/codeql.yml` | Semanal y en PRs críticos | Análisis estático de vulnerabilidades de seguridad de código (SAST) con GitHub CodeQL. |

### Flujo de Validación y Despliegue

1. **Test & Cobertura:** Compilación con Java 21 Temurin, ejecución de 206+ pruebas y validación estricta del gate de JaCoCo.
2. **Build de Contenedores:** Creación de imagen Docker multi-etapa basada en `eclipse-temurin:21-jre-alpine` con usuario no-root (`styleradar`).
3. **Despliegue en Azure:** Despliegue en Azure Web App para Linux y ejecución de **Smoke Test** posdespliegue consultando activamente `/actuator/health` hasta confirmar estado `UP` (200 OK).

El diagrama completo de arquitectura del pipeline se encuentra disponible en [pipeline-ci-cd.png](../diagramas/pipeline-ci-cd.png).
