[⬅ Volver al README principal](../../README.md)

---

## 10. Ramas y cómo contribuir

El flujo del equipo integra funcionalidades mediante ramas de trabajo y Pull Requests hacia `develop`; los cambios destinados a producción pasan por el flujo de revisión de `main` y el workflow de release configurado. Confirme las reglas de protección en GitHub, porque pueden cambiar por configuración del repositorio.

Convención recomendada de ramas: `feature/GJ-<id>-<descripcion>` y `fix/GJ-<id>-<descripcion>`. Convención de commits: Conventional Commits, por ejemplo `feat(chat): documentar los casos de uso`.

Antes del PR, revise `git diff --check`, ejecute `mvnw clean verify` cuando corresponda, actualice los documentos/diagramas afectados y no agregue secretos, certificados privados ni archivos `.env`.
