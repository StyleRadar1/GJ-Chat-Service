-- ms-chat · soporte para chat de almacenes aliados (GJ-56) y donaciones (GJ-57)

ALTER TABLE conversaciones ADD COLUMN IF NOT EXISTS tipo VARCHAR(30) NOT NULL DEFAULT 'SEGUNDA_MANO';
ALTER TABLE conversaciones ADD COLUMN IF NOT EXISTS almacen_nit VARCHAR(50);
ALTER TABLE conversaciones ADD COLUMN IF NOT EXISTS almacen_nombre VARCHAR(200);
ALTER TABLE conversaciones ADD COLUMN IF NOT EXISTS donacion_id BIGINT;

ALTER TABLE conversaciones ALTER COLUMN publicacion_id DROP NOT NULL;
ALTER TABLE conversaciones ALTER COLUMN vendedor_id DROP NOT NULL;

ALTER TABLE conversaciones DROP CONSTRAINT IF EXISTS ck_conversacion_participantes;
ALTER TABLE conversaciones ADD CONSTRAINT ck_conversacion_participantes CHECK (vendedor_id IS NULL OR comprador_id <> vendedor_id);

CREATE INDEX IF NOT EXISTS ix_conversacion_almacen ON conversaciones (almacen_nit);
CREATE INDEX IF NOT EXISTS ix_conversacion_donacion ON conversaciones (donacion_id);
