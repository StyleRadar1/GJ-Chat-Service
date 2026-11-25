[⬅ Volver al README principal](../../README.md)

---

## 3. Endpoints y enlace a Swagger

### Enlaces de entorno

| Entorno | URL base | Swagger | Health |
| --- | --- | --- | --- |
| Local | `http://localhost:8320` | `/swagger-ui.html` | `/actuator/health` |
| QA | [App Service QA](https://gj-chat-service-qa-santi-gxhjccffcxdgbfcj.canadacentral-01.azurewebsites.net) | `/swagger-ui/index.html` | `/actuator/health` |
| PROD | [App Service PROD](https://gj-chat-service-prod-santi.azurewebsites.net) | `/swagger-ui/index.html` | `/actuator/health` |

Las URLs son los endpoints reportados para el proyecto; validar disponibilidad en el ambiente antes de usarlas como evidencia.

### API REST

Las rutas protegidas requieren `Authorization: Bearer <JWT>`. El usuario se obtiene de los claims validados.

| Método | Ruta | Función |
| --- | --- | --- |
| `POST` | `/api/v1/second-hand/publications/{publicationId}/conversations` | Inicia o recupera chat C2C; recibe `{"texto":"..."}` y persiste el mensaje inicial al crear |
| `POST` | `/api/v1/stores/{nit}/conversations` | Inicia o recupera chat con almacén; persiste mensaje inicial |
| `GET` | `/api/v1/stores/conversations?page=0&size=20` | Bandeja paginada del almacén del administrador autenticado |
| `POST` | `/api/v1/donations/{donationId}/conversations` | Inicia o recupera chat de donación |
| `GET` | `/api/v1/conversations/{id}/messages?page=0&size=50` | Historial paginado |
| `POST` | `/api/v1/conversations/{id}/messages` | Envía mensaje |
| `GET` | `/api/v1/users/conversations?page=0&size=20` | Conversaciones propias paginadas |

Los errores REST usan `application/problem+json`. Paginación máxima: 100. Consulte [OpenAPI en QA](https://gj-chat-service-qa-santi-gxhjccffcxdgbfcj.canadacentral-01.azurewebsites.net/swagger-ui/index.html) y [fichas](../fichas-requerimientos-chat.md).
