-- Migracion manual para bases MySQL/MariaDB existentes.
-- EnumType.STRING almacena nombres de EstadoPedido y EstadoStockPedido.
-- VARCHAR evita que un ENUM nativo antiguo rechace estados actuales o futuros.
ALTER TABLE pedido
    MODIFY COLUMN estado VARCHAR(32) NOT NULL,
    MODIFY COLUMN estado_stock VARCHAR(32) NOT NULL;
