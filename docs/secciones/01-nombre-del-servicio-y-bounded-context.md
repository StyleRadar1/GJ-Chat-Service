[⬅ Volver al README principal](../../README.md)

---

## 1. Nombre del servicio y bounded context

- **Servicio:** `ms-chat` (`GJ-Chat-Service`; artefacto `gj-chat-service`).
- **Squad:** Guatoque Jiraguas (`GJ`), Paquete 3.
- **Puerto:** `8320`.
- **Contexto:** conversaciones privadas y mensajería en tiempo real.
- **Persistencia propia:** PostgreSQL, base `chat_db`; patrón Database per Service.
- **Requerimientos:** `SR-RF-40`, `SR-RF-41`, `SR-RF-73`, `GJ-56` y `GJ-57`.

### Responsabilidades

ms-chat inicia y autoriza conversaciones de segunda mano, almacenes aliados y donaciones; persiste mensajes; entrega historiales paginados; emite eventos de mensajes y consume eventos de segunda mano. No es dueño de publicaciones, tiendas ni solicitudes de donación y no accede a bases de datos ajenas. Las notificaciones al usuario pertenecen a `ms-notificaciones`.

**Actores JWT vigentes:** `COMPRADOR`, `ADMIN_ALMACEN`, `REPRESENTANTE_FUNDACION` y `ADMIN_STYLERADAR`. El token lo emite `ms-auth`.
