package redSelection.dulceMayor.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Pedido tal como lo ven los frontends (contrato en inglés). trace es la trazabilidad completa:
 * cada cambio de estado (carga, stock, facturación, reparto, entrega) y el transportista que lo lleva.
 */
public record PedidoRespuestaDTO(String id, String clientName, String status, String paymentMethod,
                                 String createdAt, String invoicedAt, String invoiceId, BigDecimal total,
                                 List<Linea> items, Traza trace, BigDecimal discount, PuntoDeVenta pointOfSale) {

    public record Linea(String productId, String productName, int quantity, BigDecimal unitPrice, BigDecimal total) { }

    public record Traza(List<Evento> events, Transportista carrier) { }

    public record Evento(String state, String at, String user, String note) { }

    /** Quién lleva el pedido; null hasta que sale a reparto. */
    public record Transportista(String name, String vehicle) { }

    public record PuntoDeVenta(String id, String name, String address, Double lat, Double lng) { }
}
