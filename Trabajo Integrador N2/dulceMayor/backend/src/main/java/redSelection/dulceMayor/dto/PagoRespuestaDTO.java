package redSelection.dulceMayor.dto;

import redSelection.dulceMayor.enums.EstadoPago;
import redSelection.dulceMayor.enums.TipoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoRespuestaDTO(
        String id,
        String pedidoId,
        String facturaId,   // null hasta que se emite la factura
        Long numeroFactura,
        TipoPago tipoPago,
        EstadoPago estado,
        BigDecimal monto,
        LocalDateTime fechaPago,
        String instrucciones,
        String codigoQr,
        String imagenQr,           // data URI PNG del QR (solo billetera virtual pendiente), generado con ZXing
        boolean pasarelaSimulada   // true en modo demo: el front ofrece "Simular pago"
) { }