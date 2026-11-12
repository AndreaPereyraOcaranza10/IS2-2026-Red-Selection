-- Migracion manual para bases MySQL/MariaDB existentes.
-- EnumType.STRING almacena los nombres de EstadoPedido; VARCHAR evita que
-- un ENUM nativo antiguo rechace estados actuales o futuros.
ALTER TABLE historial_estado_pedido
    MODIFY COLUMN estado_anterior VARCHAR(32) NULL,
    MODIFY COLUMN estado_nuevo VARCHAR(32) NOT NULL;
