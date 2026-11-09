package redSelection.dulceMayor.dto;

import redSelection.dulceMayor.enums.EstadoPago;
import redSelection.dulceMayor.enums.EstadoPedido;
import redSelection.dulceMayor.enums.TipoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Detalle de una entrega: qué se lleva, cuánto y cómo se cobra, y la trazabilidad del pedido. */
public record EntregaDetalleDTO(
        String pedidoId,
        Long numeroFactura,           // null si el pedido todavía no tiene factura
        String comercio,
        String responsable,
        String direccion,
        EstadoPedido estado,
        List<ItemEntregaDTO> items,
        BigDecimal subtotal,
        BigDecimal descuento,
        BigDecimal total,
        TipoPago medioPago,           // null si todavía no se acordó ninguno
        EstadoPago estadoPago,
        String instruccionesPago,
        LocalDateTime fechaCarga,
        LocalDateTime fechaFactura,
        LocalDateTime fechaDespacho,
        LocalDateTime fechaEntrega,
        String transportista,
        String patenteVehiculo
) { }
