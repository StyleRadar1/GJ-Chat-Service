[⬅ Volver al README principal](../../README.md)

---

## 9. CI/CD

Workflows del repositorio:

| Archivo | Función |
| --- | --- |
| `.github/workflows/ci-qa.yml` | Validación, construcción/publicación de imagen y despliegue QA según configuración del workflow |
| `.github/workflows/ci-prod.yml` | Pipeline de producción, activado por tags de versión y controles definidos en Actions |
| `.github/workflows/codeql.yml` | Análisis de seguridad de código |

La ejecución en verde acredita los jobs incluidos en ese run, no la compatibilidad funcional de todos los servicios ni el uso de Redis en producción. Verifique los resultados, aprobaciones y URL del ambiente directamente en Actions antes de publicar evidencia. El diagrama de [pipeline](../diagramas/pipeline-ci-cd.png) muestra el flujo general.
