package com.tienda.zero.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.tienda.zero.enums.EstadoOrdenCompra;
import com.tienda.zero.enums.TipoPago;
import com.tienda.zero.enums.TipoUsuario;
import com.tienda.zero.model.Cliente;
import com.tienda.zero.model.DetalleCompra;
import com.tienda.zero.model.Empleado;
import com.tienda.zero.model.OrdenCompra;
import com.tienda.zero.model.Persona;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.Usuario;
import com.tienda.zero.model.VigenciaPrecio;
import com.tienda.zero.repository.OrdenCompraRepository;
import com.tienda.zero.repository.PersonaRepository;
import com.tienda.zero.repository.ProductoRepository;
import com.tienda.zero.repository.UsuarioRepository;
import com.tienda.zero.service.FlujoCompraService;
import com.tienda.zero.service.StockService;
import com.tienda.zero.service.VigenciaPrecioService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FlujoCompraServiceImpl implements FlujoCompraService {
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final PersonaRepository personas;
    private final OrdenCompraRepository ordenesCliente;
    private final StockService stockService;
    private final VigenciaPrecioService precios;

    @Override
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

    @Override
    @Transactional
    public Map<String, Object> agregarAlCarrito(String username, String productoId, int cantidad) {
        if (cantidad < 1) throw new IllegalArgumentException("La cantidad debe ser positiva");
        OrdenCompra carrito = obtenerOCrearCarrito(username);
        Producto producto = productoActivo(productoId);
        DetalleCompra detalle = carrito.getDetalles().stream()
                .filter(d -> !d.isEliminado() && d.getProducto().getId().equals(productoId))
                .findFirst().orElse(null);
        int cantidadActual = detalle == null ? 0 : detalle.getCantidad();
        int stockDisponible = stockService.calcularStockActual(productoId);
        if (cantidadActual + cantidad > stockDisponible) {
            throw new IllegalArgumentException("Stock insuficiente de \"" + producto.getNombre()
                    + "\": hay " + stockDisponible + " unidades disponibles");
        }
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

    @Override
    @Transactional
    public Map<String, Object> actualizarCantidadCarrito(String username, String productoId, int cantidad) {
        if (cantidad < 1) throw new IllegalArgumentException("La cantidad debe ser positiva");
        OrdenCompra carrito = carritoEditable(username);
        DetalleCompra detalle = detalleActivo(carrito, productoId);
        int stockDisponible = stockService.calcularStockActual(productoId);
        if (cantidad > stockDisponible) {
            throw new IllegalArgumentException("Stock insuficiente de \"" + detalle.getProducto().getNombre()
                    + "\": hay " + stockDisponible + " unidades disponibles");
        }
        detalle.setCantidad(cantidad);
        detalle.setSubtotal(precioActual(detalle.getProducto()).multiply(BigDecimal.valueOf(cantidad)));
        recalcularTotal(carrito);
        ordenesCliente.save(carrito);
        return verCarrito(username);
    }

    @Override
    @Transactional
    public Map<String, Object> quitarDelCarrito(String username, String productoId) {
        OrdenCompra carrito = carritoEditable(username);
        detalleActivo(carrito, productoId).setEliminado(true);
        recalcularTotal(carrito);
        ordenesCliente.save(carrito);
        return verCarrito(username);
    }

    @Override
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

    @Override
    public List<OrdenCompra> listarPedidosCliente(String username) {
        return ordenesCliente.findByClienteUsuarioNombreUsuarioOrderByFechaDesc(username).stream()
                .filter(orden -> !orden.isEliminado())
                .filter(orden -> orden.getEstadoOrdenCompra() != EstadoOrdenCompra.PENDIENTE_COMPLETAR)
                .toList();
    }

    @Override
    public List<OrdenCompra> listarPedidosUsuario(String username) {
        return ordenesCliente.findByPropietarioNombreUsuarioOrderByFechaDesc(username).stream()
                .filter(orden -> !orden.isEliminado())
                .filter(orden -> orden.getEstadoOrdenCompra() != EstadoOrdenCompra.PENDIENTE_COMPLETAR)
                .toList();
    }

    @Override
    public List<OrdenCompra> listarPedidosAdministracion() {
        return ordenesCliente.findByEstadoOrdenCompraNotAndEliminadoFalseOrderByFechaDesc(
                EstadoOrdenCompra.PENDIENTE_COMPLETAR);
    }

    @Override
    public List<EstadoOrdenCompra> estadosSiguientes(EstadoOrdenCompra estadoActual) {
        if (estadoActual == null) return List.of();
        return switch (estadoActual) {
            case PENDIENTE_PAGO -> List.of(EstadoOrdenCompra.PENDIENTE_ENVIO, EstadoOrdenCompra.ANULADA);
            case PENDIENTE_ENVIO -> List.of(EstadoOrdenCompra.PENDIENTE_ENTREGA, EstadoOrdenCompra.ANULADA);
            case PENDIENTE_ENTREGA -> List.of(EstadoOrdenCompra.ENTREGADO, EstadoOrdenCompra.ANULADA);
            default -> List.of();
        };
    }

    @Override
    public List<EstadoOrdenCompra> estadosAdministracion() {
        return List.of(EstadoOrdenCompra.values());
    }

    @Override
    @Transactional
    public OrdenCompra crearOrdenCliente(String username, String direccion, TipoPago formaPago) {
        if (direccion == null || direccion.isBlank()) throw new IllegalArgumentException("La direccion de entrega es obligatoria");
        if (formaPago == null) throw new IllegalArgumentException("La forma de pago es obligatoria");
        Usuario usuario = usuario(username);
        if (usuario.getRol() == TipoUsuario.CLIENTE && formaPago != TipoPago.BILLETERA_VIRTUAL)
            throw new IllegalArgumentException("Los clientes solo pueden pagar con billetera virtual. Para efectivo o transferencia debe intervenir un empleado.");
        OrdenCompra orden = carritoEditable(username);
        List<DetalleCompra> detalles = orden.getDetalles().stream().filter(d -> !d.isEliminado()).toList();
        if (detalles.isEmpty()) throw new IllegalArgumentException("El carrito esta vacio");
        orden.setDireccionEntrega(direccion.trim());
        orden.setFormaPago(formaPago);
        orden.setFecha(LocalDate.now());
        orden.setEstadoOrdenCompra(formaPago == TipoPago.BILLETERA_VIRTUAL
                ? EstadoOrdenCompra.PENDIENTE_ENVIO
                : EstadoOrdenCompra.PENDIENTE_PAGO);
        recalcularTotal(orden);
        ordenesCliente.save(orden);
        for (DetalleCompra detalle : detalles) {
            stockService.registrarMovimiento(detalle.getProducto().getId(), -detalle.getCantidad(),
                    "Reserva de stock para orden", detalle);
        }
        return orden;
    }

    @Override
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
                    stockService.registrarMovimiento(detalle.getProducto().getId(), detalle.getCantidad(),
                            "Reintegro por anulacion de orden", detalle);
                }
            }
        }
        orden.setEstadoOrdenCompra(EstadoOrdenCompra.ANULADA);
        return ordenesCliente.save(orden);
    }

    @Override
    @Transactional
    public OrdenCompra cambiarSeguimiento(String ordenId, EstadoOrdenCompra nuevoEstado) {
        OrdenCompra orden = ordenesCliente.findById(ordenId).orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        EstadoOrdenCompra estadoActual = orden.getEstadoOrdenCompra();
        if (!estadosSiguientes(estadoActual).contains(nuevoEstado))
            throw new IllegalStateException("Transicion de seguimiento invalida");
        if (nuevoEstado == EstadoOrdenCompra.ANULADA) {
            for (DetalleCompra detalle : orden.getDetalles()) {
                if (!detalle.isEliminado()) {
                    stockService.registrarMovimiento(detalle.getProducto().getId(), detalle.getCantidad(),
                            "Reintegro por anulacion de orden", detalle);
                }
            }
        }
        orden.setEstadoOrdenCompra(nuevoEstado);
        return ordenesCliente.save(orden);
    }

    @Override
    @Transactional
    public OrdenCompra cambiarEstadoAdministracion(String ordenId, EstadoOrdenCompra nuevoEstado) {
        if (nuevoEstado == null) throw new IllegalArgumentException("El estado es obligatorio");
        OrdenCompra orden = ordenesCliente.findById(ordenId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        EstadoOrdenCompra estadoActual = orden.getEstadoOrdenCompra();
        if (estadoActual == EstadoOrdenCompra.ENTREGADO || estadoActual == EstadoOrdenCompra.ANULADA)
            throw new IllegalStateException("Las ordenes entregadas o anuladas no pueden cambiar de estado");
        if (nuevoEstado == EstadoOrdenCompra.ANULADA && estadoActual != EstadoOrdenCompra.ANULADA
                && stockReservado(estadoActual)) {
            for (DetalleCompra detalle : orden.getDetalles()) {
                if (!detalle.isEliminado()) {
                    stockService.registrarMovimiento(detalle.getProducto().getId(), detalle.getCantidad(),
                            "Reintegro por anulacion de orden", detalle);
                }
            }
        }
        orden.setEstadoOrdenCompra(nuevoEstado);
        return ordenesCliente.save(orden);
    }

    private boolean stockReservado(EstadoOrdenCompra estado) {
        return estado == EstadoOrdenCompra.PENDIENTE_PAGO
                || estado == EstadoOrdenCompra.PENDIENTE_ENVIO
                || estado == EstadoOrdenCompra.PENDIENTE_ENTREGA;
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
        BigDecimal precio = BigDecimal.valueOf(vigente.getPrecio());
        if (producto.isEnOferta() && producto.getPorcentajeDescuento() > 0) {
            precio = precio.multiply(BigDecimal.ONE.subtract(
                    BigDecimal.valueOf(producto.getPorcentajeDescuento()).movePointLeft(2)));
        }
        return precio.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private Optional<OrdenCompra> carritoActivo(String username) {
        Optional<OrdenCompra> porPropietario = ordenesCliente
                .findFirstByPropietarioNombreUsuarioAndEstadoOrdenCompraAndEliminadoFalseOrderByFechaDesc(
                        username, EstadoOrdenCompra.PENDIENTE_COMPLETAR);
        if (porPropietario.isPresent()) return porPropietario;

        Optional<Persona> persona = personas.findByUsuarioId(usuario(username).getId());
        if (persona.isEmpty()) return Optional.empty();
        if (persona.get() instanceof Cliente) {
            return ordenesCliente.findFirstByClienteUsuarioNombreUsuarioAndEstadoOrdenCompraAndEliminadoFalseOrderByFechaDesc(
                    username, EstadoOrdenCompra.PENDIENTE_COMPLETAR);
        }
        if (persona.get() instanceof Empleado) {
            return ordenesCliente.findFirstByEmpleadoUsuarioNombreUsuarioAndEstadoOrdenCompraAndEliminadoFalseOrderByFechaDesc(
                    username, EstadoOrdenCompra.PENDIENTE_COMPLETAR);
        }
        return Optional.empty();
    }

    private OrdenCompra obtenerOCrearCarrito(String username) {
        Optional<OrdenCompra> existente = carritoActivo(username);
        if (existente.isPresent()) return existente.get();
        Usuario usuario = usuario(username);
        Optional<Persona> persona = personas.findByUsuarioId(usuario.getId());
        OrdenCompra.OrdenCompraBuilder carrito = OrdenCompra.builder()
                .identificadorCompra(UUID.randomUUID().toString())
                .fecha(LocalDate.now()).estadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_COMPLETAR)
                .direccionEntrega("A confirmar").total(BigDecimal.ZERO).propietario(usuario);
        if (persona.orElse(null) instanceof Cliente cliente) {
            carrito.cliente(cliente).direccionEntrega(cliente.getDireccionEstadia());
        } else if (persona.orElse(null) instanceof Empleado empleado) {
            carrito.empleado(empleado);
        }
        OrdenCompra nuevoCarrito = carrito.build();
        return ordenesCliente.save(nuevoCarrito);
    }

    private Usuario usuario(String username) {
        return usuarios.findByNombreUsuario(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
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
                .findFirst().orElseThrow(() -> new IllegalArgumentException("El producto no esta en el carrito"));
    }

    private void recalcularTotal(OrdenCompra orden) {
        BigDecimal total = orden.getDetalles().stream().filter(d -> !d.isEliminado())
                .map(DetalleCompra::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        orden.setTotal(total);
    }
}
