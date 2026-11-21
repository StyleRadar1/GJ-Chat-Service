[⬅ Volver al README principal](../../README.md)

---

## 11. Seguridad y controles

- JWT firmado con HS256, clave Base64 de al menos 256 bits, igual a la de `ms-auth` para el mismo ambiente.
- Claims esperados: `sub` numérico y arreglo `roles`; roles canónicos: `COMPRADOR`, `ADMIN_ALMACEN`, `REPRESENTANTE_FUNDACION`, `ADMIN_STYLERADAR`.
- Autorización por participante y conversación; para almacenes se comprueba también que el NIT corresponda al administrador.
- Validación de estado de cuenta bloqueada/inactiva después de emitirse un JWT vigente: pendiente de contrato e implementación con ms-auth.
- WebSocket valida JWT en CONNECT y autorización individual en SUBSCRIBE/SEND.
- Las consultas externas usan Feign y configuración de timeouts; el servicio mantiene su propia base de datos.
- No registre tokens ni texto privado de los mensajes en logs.

Los hallazgos, pendientes y dependencias de integración están en [trazabilidad](../evidencias/sprint-3-trazabilidad.md). No se declara certificación OWASP formal con esta lista.
