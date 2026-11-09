package redSelection.dulceMayor.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import redSelection.dulceMayor.enums.*;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.repository.*;
import redSelection.dulceMayor.security.UsuarioActual;
import redSelection.dulceMayor.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class PortalOrderController {
    private final PedidoRepository pedidos;
    private final ClienteMinoristaRepository clientes;
    private final ResponsableComercioRepository responsables;
    private final PreventistaRepository preventistas;
    private final ProductoService productos;
    private final PrecioService precios;
    private final StockService stock;
    private final FacturaClienteRepository facturas;
    private final UsuarioActual usuarioActual;

    public PortalOrderController(PedidoRepository pedidos, ClienteMinoristaRepository clientes,
                                 ResponsableComercioRepository responsables, PreventistaRepository preventistas,
                                 ProductoService productos, PrecioService precios, StockService stock,
                                 FacturaClienteRepository facturas, UsuarioActual usuarioActual) {
        this.pedidos = pedidos;
        this.clientes = clientes;
        this.responsables = responsables;
        this.preventistas = preventistas;
        this.productos = productos;
        this.precios = precios;
        this.stock = stock;
        this.facturas = facturas;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping
    @Transactional
    public List<OrderResponse> list(Authentication authentication) {
        if (isStaff(authentication)) {
            return pedidos.findByEliminadoFalse().stream().map(this::toResponse).toList();
        }
        ResponsableComercio responsable = responsableFor(authentication.getName());
        if (responsable == null) return List.of();
        return pedidos.findByClienteResponsableIdOrderByFechaHoraCargaDesc(responsable.getId()).stream()
                .filter(p -> !p.isEliminado()).map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public OrderResponse create(@Valid @RequestBody OrderRequest request, Authentication authentication) {
        ClienteMinorista cliente;
        if (isStaff(authentication)) {
            String name = request.clientName() == null ? "" : request.clientName().trim();
            cliente = clientes.findByEliminadoFalse().stream()
                    .filter(c -> c.getNombreComercio().equalsIgnoreCase(name)
                            || c.getRazonSocial().equalsIgnoreCase(name))
                    .findFirst().orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "No existe un comercio con ese nombre."));
        } else {
            ResponsableComercio responsable = responsableFor(authentication.getName());
            if (responsable == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Tu cuenta todavía no está vinculada a un comercio. Contactá a un administrador para asociarla.");
            }
            cliente = clientes.findByResponsableIdAndEliminadoFalse(responsable.getId()).stream().findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                            "Tu cuenta todavía no está vinculada a un comercio. Contactá a un administrador para asociarla."));
        }

        Preventista asignado = cliente.getPreventistaAsignado();
        if (asignado == null) {
            asignado = preventistas.findByEliminadoFalse().stream().findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                            "No hay un preventista asignado para procesar pedidos."));
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido debe incluir productos.");
        }

        Pedido pedido = Pedido.builder().fechaHoraCarga(LocalDateTime.now()).cliente(cliente)
                .preventista(asignado).estado(EstadoPedido.CARGADO)
                .estadoStock(EstadoStockPedido.CONFIRMADO)
                .subtotal(BigDecimal.ZERO).descuento(BigDecimal.ZERO).total(BigDecimal.ZERO)
                .items(new ArrayList<>()).build();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderLineRequest line : request.items()) {
            if (line.quantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las cantidades deben ser mayores a cero.");
            }
            Producto producto = productos.obtenerConBloqueo(line.productId());
            BigDecimal unitPrice = precios.precioVigenteObligatorio(producto, LocalDate.now());
            if (stock.stockActual(producto.getId()) < line.quantity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No hay stock suficiente para " + producto.getNombre() + ".");
            }
            BigDecimal row = unitPrice.multiply(BigDecimal.valueOf(line.quantity()));
            PedidoItem item = PedidoItem.builder().pedido(pedido).producto(producto)
                    .cantidad(line.quantity()).precioUnitario(unitPrice).subtotal(row).build();
            pedido.getItems().add(item);
            subtotal = subtotal.add(row);
        }
        pedido.setSubtotal(subtotal);
        pedido.setTotal(subtotal);
        pedido.registrarAlta(usuarioActual.nombreOSistema());
        return toResponse(pedidos.save(pedido));
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public OrderResponse updateStatus(@PathVariable String id, @RequestBody StatusUpdate update) {
        Pedido pedido = pedidos.buscarConBloqueo(id).filter(p -> !p.isEliminado())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el pedido."));
        EstadoPedido next;
        try {
            next = EstadoPedido.valueOf(update.status().trim().toUpperCase());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado solicitado no es válido.");
        }
        // El estado actual (patrón State) valida la transición y la registra en el historial.
        // Si después falla la factura, la transacción se revierte y el pedido queda como estaba.
        pedido.avanzarA(next, usuarioActual.nombreOSistema(), update.note());
        if (next == EstadoPedido.FACTURADO) emitirFactura(pedido);
        return toResponse(pedidos.save(pedido));
    }

    private void emitirFactura(Pedido pedido) {
        if (facturas.findByPedidoId(pedido.getId()).isPresent()) return;
        FacturaCliente factura = FacturaCliente.builder()
                .numeroFactura(facturas.ultimoNumero() + 1)
                .fechaFactura(LocalDateTime.now())
                .estado(EstadoFactura.EMITIDA)
                .subtotal(pedido.getSubtotal()).descuento(pedido.getDescuento()).total(pedido.getTotal())
                .pedido(pedido).detalles(new ArrayList<>()).build();
        for (PedidoItem item : pedido.getItems()) {
            Producto locked = productos.obtenerConBloqueo(item.getProducto().getId());
            if (stock.stockActual(locked.getId()) < item.getCantidad()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Stock insuficiente para facturar " + locked.getNombre() + ".");
            }
            DetalleFactura detail = DetalleFactura.builder().factura(factura).producto(locked)
                    .cantidad(item.getCantidad()).precioUnitario(item.getPrecioUnitario())
                    .subtotal(item.getSubtotal()).build();
            factura.getDetalles().add(detail);
            stock.registrar(locked, TipoMovimientoStock.VENTA, -item.getCantidad(), detail);
        }
        facturas.save(factura);
    }

    private ResponsableComercio responsableFor(String username) {
        return responsables.findByEliminadoFalse().stream()
                .filter(r -> r.getUsuario() != null
                        && r.getUsuario().getNombreUsuario().equalsIgnoreCase(username))
                .findFirst().orElse(null);
    }

    private boolean isStaff(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATIVO")
                        || a.getAuthority().equals("ROLE_JEFE_ADMINISTRATIVO"));
    }

    private OrderResponse toResponse(Pedido pedido) {
        List<OrderLineResponse> lines = pedido.getItems().stream()
                .map(i -> new OrderLineResponse(i.getProducto().getId(), i.getProducto().getNombre(),
                        i.getCantidad(), i.getPrecioUnitario(), i.getSubtotal())).toList();
        FacturaCliente factura = facturas.findByPedidoId(pedido.getId()).orElse(null);
        String invoicedAt = factura == null ? null : factura.getFechaFactura().toString();
        String invoiceId = factura == null ? null : factura.getId();
        return new OrderResponse(pedido.getId(), pedido.getCliente().getNombreComercio(),
                pedido.getEstado().name(), "A_COORDINAR", pedido.getFechaHoraCarga().toString(),
                invoicedAt, invoiceId, pedido.getTotal(), lines, new OrderTrace(eventos(pedido)));
    }

    /** Historial con la misma forma que usa el frontend ({ state, at }). */
    private List<OrderEvent> eventos(Pedido pedido) {
        if (pedido.getHistorial().isEmpty()) {
            // Pedidos creados antes de BK-04: solo se conoce la fecha de carga.
            return List.of(new OrderEvent(EstadoPedido.CARGADO.name(), pedido.getFechaHoraCarga().toString(),
                    null, null));
        }
        return pedido.getHistorial().stream()
                .map(h -> new OrderEvent(h.getEstadoNuevo().name(), h.getFechaHora().toString(),
                        h.getUsuario(), h.getObservacion()))
                .toList();
    }

    public record OrderRequest(String clientName, String paymentMethod,
                               @NotEmpty List<@Valid OrderLineRequest> items) {}
    public record OrderLineRequest(@NotBlank String productId, @Min(1) int quantity) {}
    /** note es opcional: queda como observación en el historial. */
    public record StatusUpdate(@NotBlank String status, String note) {}
    public record OrderLineResponse(String productId, String productName, int quantity,
                                    BigDecimal unitPrice, BigDecimal total) {}
    public record OrderResponse(String id, String clientName, String status, String paymentMethod,
                                String createdAt, String invoicedAt, String invoiceId, BigDecimal total,
                                List<OrderLineResponse> items, OrderTrace trace) {}
    public record OrderTrace(List<OrderEvent> events) {}
    public record OrderEvent(String state, String at, String user, String note) {}
}