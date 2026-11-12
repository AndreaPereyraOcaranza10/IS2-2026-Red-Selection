-- Migracion manual para bases MySQL/MariaDB existentes.
-- Conserva los valores y permite agregar proveedores como AUTH0 sin ENUM nativo.
ALTER TABLE usuario
    MODIFY COLUMN proveedor_auth VARCHAR(32) NOT NULL;
