-- Migracion manual para bases MySQL/MariaDB existentes.
-- Un pago de pedido se crea antes de emitir la factura; la factura se vincula
-- al pago cuando se acredita o cuando corresponde facturar.
ALTER TABLE pago
    MODIFY COLUMN factura_id VARCHAR(255) NULL;
