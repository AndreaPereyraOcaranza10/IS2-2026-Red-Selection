-- Guarda el ID de la orden creada en Mercado Pago para consultar, cancelar y verificar el pago.
ALTER TABLE pago
    ADD COLUMN referencia_externa VARCHAR(100) NULL,
    ADD INDEX idx_pago_referencia_externa (referencia_externa);
