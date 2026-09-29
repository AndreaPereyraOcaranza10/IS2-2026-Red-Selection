package com.tienda.zero.service;

import com.tienda.zero.enums.EstadoOrdenCompra;
import com.tienda.zero.enums.EstadoRecepcionCompra;
import com.tienda.zero.model.*;
import com.tienda.zero.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service @RequiredArgsConstructor
public class FlujoCompraService {
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final PersonaRepository personas;
    private final ProveedorRepository proveedores;
    private final OrdenCompraRepository ordenesCliente;
    private final OrdenCompraProveedorRepository ordenesProveedor;
    private final StockService stockService;
    private final VigenciaPrecioService precios;

    @Transactional
    public Map<String, Object> verCarrito(String username) {
        Optional<OrdenCompra> existente = carritoActivo(username);
        if (existente.isEmpty()) return Map.of("items", List.of(), "total", BigDecimal.ZERO);
        OrdenCompra carrito = existente.get();
        List<Map<String, Object>> lineas = new ArrayList<>();
        for (DetalleCompra detalle : carrito.getDetalles()) {
            if (detalle.isEliminado()) continue;
            Producto producto = detalle.getProducto();
            int cantidad = detalle.getCantidad();
            BigDecimal subtotal = detalle.getSubtotal();
            BigDecimal precioUnitario = subtotal.divide(BigDecimal.valueOf(cantidad), 2, java.math.RoundingMode.HALF_UP);
            String imagen = producto.getImagen() == null ? "/assets/images/products/1.jpg" : "/imagen/" + producto.getImagen().getId();
            lineas.add(Map.ofEntries(
                    Map.entry("id", producto.getId()), Map.entry("productoId", producto.getId()),
                    Map.entry("name", producto.getNombre()), Map.entry("nombre", producto.getNombre()),
                    Map.entry("codigo", producto.getCodigo()), Map.entry("imageUrl", imagen),
                    Map.entry("stock", stockService.calcularStockActual(producto.getId())),
                    Map.entry("quantity", cantidad), Map.entry("cantidad", cantidad),
                    Map.entry("price", precioUnitario), Map.entry("lineTotal", subtotal), Map.entry("subtotal", subtotal)));
        }
        return Map.of("items", lineas, "total", carrito.getTotal());
    }

    @Transactional
    public Map<String, Object> agregarAlCarrito(String username, String productoId, int cantidad) {
        if (cantidad < 1) throw new IllegalArgumentException("La cantidad debe ser positiva");
        OrdenCompra carrito = obtenerOCrearCarrito(username);
        Producto producto = productoActivo(productoId);
        DetalleCompra detalle = carrito.getDetalles().stream().filter(d -> !d.isEliminado() && d.getProducto().getId().equals(productoId)).findFirst().orElse(null);
        if (detalle == null) {
            detalle = DetalleCompra.builder().ordenCompra(carrito).producto(producto).cantidad(cantidad)
                    .subtotal(precioActual(producto).multiply(BigDecimal.valueOf(cantidad))).build();
            carrito.getDetalles().add(detalle);
        } else {
            detalle.setCantidad(detalle.getCantidad() + cantidad);
            detalle.setSubtotal(precioActual(producto).multiply(BigDecimal.valueOf(detalle.getCantidad())));
        }
        recalcularTotal(carrito);
        ordenesCliente.save(carrito);
        return verCarrito(username);
    }

    @Transactional
    public Map<String, Object> actualizarCantidadCarrito(String username, String productoId, int cantidad) {
        if (cantidad < 1) throw new IllegalArgumentException("La cantidad debe ser positiva");
        OrdenCompra carrito = carritoEditable(username);
        DetalleCompra detalle = detalleActivo(carrito, productoId);
        detalle.setCantidad(cantidad);
        detalle.setSubtotal(precioActual(detalle.getProducto()).multiply(BigDecimal.valueOf(cantidad)));
        recalcularTotal(carrito);
        ordenesCliente.save(carrito);
        return verCarrito(username);
    }

    @Transactional
    public Map<String, Object> quitarDelCarrito(String username, String productoId) {
        OrdenCompra carrito = carritoEditable(username);
        detalleActivo(carrito, productoId).setEliminado(true);
        recalcularTotal(carrito);
        ordenesCliente.save(carrito);
        return verCarrito(username);
    }

    @Transactional
    public Map<String, Object> vaciarCarrito(String username) {
        Optional<OrdenCompra> carrito = carritoActivo(username);
        carrito.ifPresent(orden -> {
            orden.getDetalles().forEach(detalle -> detalle.setEliminado(true));
            recalcularTotal(orden);
            ordenesCliente.save(orden);
        });
        return verCarrito(username);
    }

    public List<OrdenCompra> listarPedidosCliente(String username) {
        return ordenesCliente.findByClienteUsuarioNombreUsuarioOrderByFechaDesc(username);
    }

    public List<OrdenCompraProveedor> listarOrdenesProveedor() { return ordenesProveedor.findAll(); }

    @Transactional
    public OrdenCompra crearOrdenCliente(String username, String direccion) {
        if (direccion == null || direccion.isBlank()) throw new IllegalArgumentException("La dirección de entrega es obligatoria");
        OrdenCompra orden = carritoEditable(username);
        List<DetalleCompra> detalles = orden.getDetalles().stream().filter(d -> !d.isEliminado()).toList();
        if (detalles.isEmpty()) throw new IllegalArgumentException("El carrito está vacío");
        orden.setDireccionEntrega(direccion.trim());
        orden.setFecha(LocalDate.now());
        orden.setEstadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_PAGO);
        recalcularTotal(orden);
        ordenesCliente.save(orden);
        for (DetalleCompra detalle : detalles) {
            stockService.registrarMovimiento(detalle.getProducto().getId(), -detalle.getCantidad(), "Reserva de stock para orden", null, detalle);
        }
        return orden;
    }

    @Transactional
    public OrdenCompra anularOrdenCliente(String ordenId, String username) {
        OrdenCompra orden = ordenClienteDeUsuario(ordenId, username);
        EstadoOrdenCompra estadoActual = orden.getEstadoOrdenCompra();
        if (estadoActual == EstadoOrdenCompra.ENTREGADO || estadoActual == EstadoOrdenCompra.ANULADA
                || estadoActual == EstadoOrdenCompra.PENDIENTE_ENTREGA)
            throw new IllegalStateException("La orden ya fue enviada y no puede anularse");
        boolean stockReservado = estadoActual == EstadoOrdenCompra.PENDIENTE_PAGO || estadoActual == EstadoOrdenCompra.PENDIENTE_ENVIO;
        if (stockReservado) {
            for (DetalleCompra detalle : orden.getDetalles()) {
                if (!detalle.isEliminado()) {
                    stockService.registrarMovimiento(detalle.getProducto().getId(), detalle.getCantidad(), "Reintegro por anulación de orden", null, detalle);
                }
            }
        }
        orden.setEstadoOrdenCompra(EstadoOrdenCompra.ANULADA);
        return ordenesCliente.save(orden);
    }

    @Transactional
    public OrdenCompra cambiarSeguimiento(String ordenId, EstadoOrdenCompra nuevoEstado) {
        OrdenCompra orden = ordenesCliente.findById(ordenId).orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        boolean permitido = (orden.getEstadoOrdenCompra() == EstadoOrdenCompra.PENDIENTE_ENVIO && nuevoEstado == EstadoOrdenCompra.PENDIENTE_ENTREGA)
                || (orden.getEstadoOrdenCompra() == EstadoOrdenCompra.PENDIENTE_ENTREGA && nuevoEstado == EstadoOrdenCompra.ENTREGADO);
        if (!permitido) throw new IllegalStateException("Transición de seguimiento inválida");
        orden.setEstadoOrdenCompra(nuevoEstado);
        return ordenesCliente.save(orden);
    }

    @Transactional
    public OrdenCompraProveedor crearOrdenProveedor(String proveedorId, String productoId, int cantidad, BigDecimal precioCompra) {
        if (cantidad < 1 || precioCompra == null || precioCompra.signum() <= 0) throw new IllegalArgumentException("Cantidad y precio deben ser mayores que cero");
        Proveedor proveedor = proveedores.findById(proveedorId).filter(p -> !p.isEliminado()).orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        Producto producto = productoActivo(productoId);
        return ordenesProveedor.save(OrdenCompraProveedor.builder().proveedor(proveedor).producto(producto).cantidad(cantidad)
                .precioCompra(precioCompra).total(precioCompra.multiply(BigDecimal.valueOf(cantidad)))
                .fechaCreacion(LocalDateTime.now()).build());
    }

    @Transactional
    public OrdenCompraProveedor recibirOrdenProveedor(String ordenId) {
        OrdenCompraProveedor orden = ordenesProveedor.findById(ordenId).orElseThrow(() -> new IllegalArgumentException("Orden de proveedor no encontrada"));
        if (orden.getEstado() != EstadoRecepcionCompra.PENDIENTE_RECEPCION) throw new IllegalStateException("La orden ya fue procesada");
        stockService.registrarMovimiento(orden.getProducto().getId(), orden.getCantidad(), "Recepción de compra a proveedor", orden, null);
        orden.setEstado(EstadoRecepcionCompra.ENTREGADA);
        orden.setFechaRecepcion(LocalDateTime.now());
        return ordenesProveedor.save(orden);
    }

    private OrdenCompra ordenClienteDeUsuario(String id, String username) {
        return ordenesCliente.findByIdAndClienteUsuarioNombreUsuario(id, username)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
    }

    private Producto productoActivo(String id) {
        return productos.findById(id).filter(p -> !p.isEliminado()).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }

    private BigDecimal precioActual(Producto producto) {
        VigenciaPrecio vigente = precios.buscarVigenciaPrecioVigente(producto.getId());
        if (vigente == null || vigente.getPrecio() <= 0) throw new IllegalStateException("El producto no tiene precio vigente: " + producto.getNombre());
        return BigDecimal.valueOf(vigente.getPrecio()).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private Optional<OrdenCompra> carritoActivo(String username) {
        return ordenesCliente.findFirstByClienteUsuarioNombreUsuarioAndEstadoOrdenCompraAndEliminadoFalseOrderByFechaDesc(
                username, EstadoOrdenCompra.PENDIENTE_COMPLETAR);
    }

    private OrdenCompra obtenerOCrearCarrito(String username) {
        Optional<OrdenCompra> existente = carritoActivo(username);
        if (existente.isPresent()) return existente.get();
        Usuario usuario = usuarios.findByNombreUsuario(username).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Persona persona = personas.findByUsuarioId(usuario.getId()).orElseThrow(() -> new IllegalArgumentException("El usuario debe completar su perfil de cliente"));
        if (!(persona instanceof Cliente cliente)) throw new IllegalArgumentException("El carrito está disponible para clientes");
        OrdenCompra nuevoCarrito = OrdenCompra.builder().identificadorCompra(UUID.randomUUID().toString())
                .fecha(LocalDate.now()).cliente(cliente).estadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_COMPLETAR)
                .direccionEntrega(cliente.getDireccionEstadia()).total(BigDecimal.ZERO).build();
        return ordenesCliente.save(nuevoCarrito);
    }

    private OrdenCompra carritoEditable(String username) {
        OrdenCompra carrito = carritoActivo(username).orElseThrow(() -> new IllegalArgumentException("No hay un carrito activo"));
        if (carrito.getEstadoOrdenCompra() != EstadoOrdenCompra.PENDIENTE_COMPLETAR)
            throw new IllegalStateException("El carrito ya fue confirmado");
        return carrito;
    }

    private DetalleCompra detalleActivo(OrdenCompra carrito, String productoId) {
        return carrito.getDetalles().stream()
                .filter(d -> !d.isEliminado() && d.getProducto().getId().equals(productoId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("El producto no está en el carrito"));
    }

    private void recalcularTotal(OrdenCompra orden) {
        BigDecimal total = orden.getDetalles().stream().filter(d -> !d.isEliminado())
                .map(DetalleCompra::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        orden.setTotal(total);
    }
}
