package redSelection.dulceMayor.service;

/** Se publica al emitir una factura. Lo escucha CorreoFacturaService para mandar el correo con el PDF (BK-08). */
public record FacturaEmitidaEvento(String facturaId) { }
