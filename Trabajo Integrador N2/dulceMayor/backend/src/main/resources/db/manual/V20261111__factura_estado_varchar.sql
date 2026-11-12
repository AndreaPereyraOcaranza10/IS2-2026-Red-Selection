-- Migración manual para bases MySQL/MariaDB existentes.
-- EnumType.STRING usa los valores del enum de Java; VARCHAR permite sumar nuevos estados
-- (por ejemplo PAGADA) sin que el ENUM nativo de la columna rechace la actualización.
ALTER TABLE factura_cliente
    MODIFY COLUMN estado VARCHAR(20) NOT NULL;
