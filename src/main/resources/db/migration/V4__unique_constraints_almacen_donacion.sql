-- ms-chat · restricción única para evitar conversaciones duplicadas de almacén y donación

-- Eliminar posibles duplicados antiguos conservando el más antiguo antes de crear el índice único
DELETE FROM conversaciones c1
USING conversaciones c2
WHERE c1.id > c2.id
  AND c1.tipo = 'ALMACEN'
  AND c2.tipo = 'ALMACEN'
  AND c1.almacen_nit = c2.almacen_nit
  AND c1.comprador_id = c2.comprador_id;

DELETE FROM conversaciones c1
USING conversaciones c2
WHERE c1.id > c2.id
  AND c1.tipo = 'DONACION'
  AND c2.tipo = 'DONACION'
  AND c1.donacion_id = c2.donacion_id;

CREATE UNIQUE INDEX IF NOT EXISTS uq_conversacion_almacen_comprador
    ON conversaciones (almacen_nit, comprador_id)
    WHERE tipo = 'ALMACEN';

CREATE UNIQUE INDEX IF NOT EXISTS uq_conversacion_donacion
    ON conversaciones (donacion_id)
    WHERE tipo = 'DONACION';
