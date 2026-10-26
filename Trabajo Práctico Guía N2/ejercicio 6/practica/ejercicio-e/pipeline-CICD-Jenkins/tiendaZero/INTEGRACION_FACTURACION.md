# Integración pendiente: facturación

La implementación de las clases de factura queda a cargo del otro grupo. Este módulo no debe crear ni persistir `Factura`, `FacturaCliente`, `FacturaProveedor` ni `DetalleFactura`.

## Compra del cliente

1. El carrito persistido es una `OrdenCompra` en estado `PENDIENTE_COMPLETAR`, con sus `DetalleCompra`.
2. Al confirmar checkout, `FlujoCompraService.crearOrdenCliente` valida y reserva stock mediante movimientos negativos de `Stock`, y deja la orden en `PENDIENTE_PAGO`.
3. El módulo de facturación debe registrar la forma de pago elegida y crear una `FacturaCliente` asociada a la orden y al cliente. Debe copiar cada `DetalleCompra` activo a un `DetalleFactura`, conservando producto, cantidad y subtotal.
4. La factura debe guardar el importe total, fecha, número, forma de pago y estado pagado según el UML. Al aceptar el pago, avanzar la orden a `PENDIENTE_ENVIO`.
5. Si se anula una orden antes del envío, el flujo actual agrega movimientos positivos para reintegrar las cantidades reservadas. La factura asociada, si ya existe, debe anularse en la misma operación. No volver a descontar stock al emitir la factura: ya se reservó al confirmar checkout.
6. Para vincular auditoría de stock con la factura, agregar en `Stock` la asociación opcional a `DetalleFactura` y asociar allí el movimiento negativo de reserva. No generar un segundo movimiento por la venta.

## Compra al proveedor

El requerimiento de aprovisionamiento está separado del carrito del UML y se representa en `OrdenCompraProveedor`. Al recibirla, el flujo actual registra el movimiento positivo de stock y la marca `ENTREGADA`. Si se registra la factura del proveedor, crear `FacturaProveedor`/`DetalleFactura` y asociarla con esa orden recibida, sin volver a incrementar el stock.

## Estados y puntos de integración

- `OrdenCompra`: `PENDIENTE_COMPLETAR` → `PENDIENTE_PAGO` → `PENDIENTE_ENVIO` → `PENDIENTE_ENTREGA` → `ENTREGADO`; la anulación usa `ANULADA`.
- El checkout actual se detiene en `PENDIENTE_PAGO`; la interfaz lo informa y no afirma que se cobró ni que se emitió una factura.
- `FormaDePago` y `TipoPago` permanecen como clases del dominio disponibles para el módulo de facturación. El endpoint o servicio que confirma el pago debe ser implementado por ese grupo y llamado antes de pasar la orden a `PENDIENTE_ENVIO`.
