[⬅ Volver al README principal](../../README.md)

---

## 7. Variables de entorno

Los secretos se inyectan desde el entorno o el gestor de secretos del despliegue. No se publican en README, imágenes ni Git.

| Variable | Uso | Valor local típico |
| --- | --- | --- |
| `SERVER_PORT` | Puerto HTTP | `8320` |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL | URL local a `chat_db` |
| `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `RABBITMQ_VHOST` | Broker | `localhost`, `5672` |
| `RABBITMQ_SSL_ENABLED` | TLS del broker | `false` local; `true` cuando lo requiera el entorno |
| `JWT_SECRET_BASE64` | Verificación HS256; coincide con ms-auth por ambiente | Secreto local Base64 de 256 bits o más |
| `SEGUNDA_MANO_URL`, `TIENDAS_URL`, `DONACIONES_URL` | URLs Feign | URLs de servicios locales |
| `CORS_ALLOWED_ORIGINS`, `WEBSOCKET_ALLOWED_ORIGINS` | Orígenes permitidos | Origen local del frontend |
| `REDIS_URL`, `WEBSOCKET_REDIS_ENABLED` | Relay opcional STOMP | `redis://localhost:6379`, `false` |
| `CHAT_RATE_LIMIT_MESSAGES`, `CHAT_RATE_LIMIT_WINDOW_SECONDS` | Límite de envío | `30`, `60` |

Revise `src/main/resources/application.yml` y `docker-compose.yml` para los valores predeterminados vigentes. El truststore RabbitMQ y cualquier secreto de QA/PROD se administran fuera del repositorio.
