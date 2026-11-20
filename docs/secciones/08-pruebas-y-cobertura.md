[⬅ Volver al README principal](../../README.md)

---

## 8. Pruebas y cobertura

Ejecute la suite desde la raíz:

```bash
./mvnw clean verify
```

En Windows: `mvnw.cmd clean verify`. El reporte JaCoCo queda en `target/site/jacoco/index.html`; el umbral Maven configurado es 85 %. Algunas pruebas de integración usan Testcontainers y requieren Docker.

La trazabilidad conserva resultados históricos de ejecución. No se afirma que la suite se haya vuelto a ejecutar al actualizar esta documentación; consulte [evidencia Sprint 3](../evidencias/sprint-3-trazabilidad.md) y el run actual de GitHub Actions antes de reportar cifras.
