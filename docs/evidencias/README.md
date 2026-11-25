# Registro y Evidencias de Ejecución del Servicio (ms-chat)

Este directorio documenta las evidencias reales y reproducibles de la ejecución de `GJ-Chat-Service`, diferenciando los diagramas de diseño arquitectónico (ubicados en `docs/diagramas/`) de las capturas y métricas de ejecución.

---

## 1. Evidencia de Pruebas y Cobertura (JaCoCo)
- **Suite Automatizada:** 206 pruebas unitarias y de integración pasando al 100% (0 errores, 0 fallos).
- **Gate de Cobertura JaCoCo:** Regla del 85% de cobertura mínima de líneas e instrucciones en capas `domain` y `application`.
- **Comando de Generación:**
  ```bash
  ./mvnw clean verify
  ```
- **Ubicación del Reporte:** `target/site/jacoco/index.html`.

---

## 2. Evidencia de API REST y Swagger UI
- **URL QA:** [https://gj-chat-service-qa-santi-gxhjccffcxdgbfcj.canadacentral-01.azurewebsites.net/swagger-ui/index.html](https://gj-chat-service-qa-santi-gxhjccffcxdgbfcj.canadacentral-01.azurewebsites.net/swagger-ui/index.html)
- **URL PROD:** [https://gj-chat-service-prod-santi.azurewebsites.net/swagger-ui/index.html](https://gj-chat-service-prod-santi.azurewebsites.net/swagger-ui/index.html)
- **Contrato:** 7 operaciones REST estandarizadas en inglés, simetría REST con identificadores de ruta (`{userId}`, `{nit}`) y endpoint STOMP registrado en `/api/v1/ws` y `/ws`.

---

## 3. Evidencia de Ejecución en Contenedores (Docker Compose)
- **Stack Local:** PostgreSQL 16 Alpine (`postgres:16-alpine`), RabbitMQ 3.13 Management (`rabbitmq:3.13-management-alpine`) y Redis 7 (`redis:7-alpine`).
- **Comando de Verificación de Estado:**
  ```bash
  docker compose ps
  ```

---

## 4. Evidencia de CI/CD (GitHub Actions)
- **Workflow CI QA:** Validaciones continuas de compilación, JaCoCo, Checkstyle y despliegue continuo en Azure.
- **Workflow CI PROD:** Empaquetado Docker Hub multi-etapa y despliegue a producción con Smoke Test automático sobre `/actuator/health`.
- **Historial Completo:** [Acciones del Repositorio](https://github.com/StyleRadar1/GJ-Chat-Service/actions).

---

## 5. Trazabilidad Histórica y Épica en Jira
Para el desglose histórico de criterios de aceptación, pruebas funcionales por rol y relación con las historias de usuario de Jira, consulte el documento:
- [Trazabilidad y Pruebas Sprint 3](./sprint-3-trazabilidad.md)
