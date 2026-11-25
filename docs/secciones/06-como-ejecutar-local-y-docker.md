[⬅ Volver al README principal](../../README.md)

---

## 6. Cómo ejecutar local y con Docker

### Requisitos

- JDK 21 y Maven 3.9+ (o Maven Wrapper).
- Docker Desktop/Engine y Docker Compose si se levantan dependencias en contenedores.
- Servicios externos locales o sus URLs configuradas para probar Feign.

### Levantar dependencias

```bash
docker compose up -d postgres rabbitmq
```

Para probar Redis Pub/Sub real:

```bash
docker compose --profile realtime up -d postgres rabbitmq redis
```

### Ejecutar el servicio

Configure `JWT_SECRET_BASE64` con una clave local de al menos 256 bits, y ajuste URLs externas según el ambiente:

```bash
./mvnw spring-boot:run
```

En Windows use `mvnw.cmd spring-boot:run`. La configuración local de Compose usa PostgreSQL, RabbitMQ y el puerto HTTP `8320`; no coloque secretos reales en Git. Ver [variables](07-variables-de-entorno.md).
