-- Agrega la relación directa Pago -> Pedido usada por el flujo de pago del portal.
-- Es nullable para conservar pagos anteriores que solo estaban asociados a una factura.
ALTER TABLE pago
    ADD COLUMN pedido_id VARCHAR(36) NULL,
    ADD INDEX idx_pago_pedido_id (pedido_id),
    ADD CONSTRAINT fk_pago_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido(id);
