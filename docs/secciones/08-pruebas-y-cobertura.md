[⬅ Volver al README principal](../../README.md)

---

## 8. Pruebas y cobertura

### Criterio de Cobertura y Métricas de Calidad

- **Umbral Aceptado por el Curso:** Superior al **80%**.
- **Gate de Compilación (JaCoCo):** Configurado formalmente en `pom.xml` al **85%** de cobertura mínima de líneas e instrucciones sobre las capas de dominio (`edu.dosw.styleradar.chat.domain.*`) y aplicación (`edu.dosw.styleradar.chat.application.*`).
- **Pruebas Automatizadas:** 206+ pruebas unitarias, de integración y de contrato REST que validan el 100% de los casos de uso, simetría REST, reglas de negocio (prenda disponible, no auto-chat, administración de almacén por NIT), seguridad JWT, WebSocket STOMP y manejo de excepciones RFC 7807.
- **Checkstyle:** Auditoría estática de estilo integrada con 0 violaciones (`mvn -B checkstyle:check`).

### Ejecución Local

Ejecute la suite completa y generación de reportes desde la raíz del proyecto usando el Maven Wrapper:

```bash
# En Linux / macOS
./mvnw clean verify

# En Windows
mvnw.cmd clean verify
```

El reporte interactivo de cobertura JaCoCo se genera en:
`target/site/jacoco/index.html`

> Para detalles históricos y trazabilidad de pruebas por sprint, consulte [Trazabilidad Sprint 3](../evidencias/sprint-3-trazabilidad.md).
