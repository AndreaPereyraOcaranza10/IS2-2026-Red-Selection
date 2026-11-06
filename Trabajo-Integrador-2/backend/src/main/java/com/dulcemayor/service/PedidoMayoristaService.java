package com.dulcemayor.service;

import com.dulcemayor.dto.OrderCreateDTO;
import com.dulcemayor.dto.OrderResponseDTO;
import com.dulcemayor.model.Pedido;
import com.dulcemayor.model.PedidoItem;
import com.dulcemayor.model.Producto;
import com.dulcemayor.model.VigenciaPrecio;
import com.dulcemayor.repository.PedidoRepository;
import com.dulcemayor.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoMayoristaService extends BusinessServiceTemplate {
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final VigenciaPrecioService precios;

    @Transactional
    public OrderResponseDTO crear(OrderCreateDTO dto) {
        return ejecutarCasoDeUso(() -> crearPedido(dto));
    }

    private OrderResponseDTO crearPedido(OrderCreateDTO dto) {
        Pedido pedido = Pedido.builder().cliente(dto.clientName().trim()).medioPago(
                dto.paymentMethod() == null || dto.paymentMethod().isBlank() ? "A_COORDINAR" : dto.paymentMethod())
                .estado("RECIBIDO").fechaCarga(LocalDateTime.now()).total(BigDecimal.ZERO).build();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderCreateDTO.Line line : dto.items()) {
            Producto product = productoRepository.findById(line.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + line.productId()));
            if (product.isEliminado() || product.getStock() < line.quantity())
                throw new IllegalArgumentException("Stock insuficiente para " + product.getNombre());
            VigenciaPrecio vigente = precios.buscarVigenciaPrecioVigente(product.getId());
            BigDecimal unit = BigDecimal.valueOf(vigente.getPrecio());
            BigDecimal row = unit.multiply(BigDecimal.valueOf(line.quantity()));
            PedidoItem item = PedidoItem.builder().pedido(pedido).producto(product).cantidad(line.quantity())
                    .precioUnitario(unit).total(row).build();
            pedido.getItems().add(item);
            product.setStock(product.getStock() - line.quantity());
            productoRepository.save(product);
            total = total.add(row);
        }
        pedido.setTotal(total);
        return toDto(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> listar() { return pedidoRepository.findAll().stream().map(this::toDto).toList(); }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> listarDeCliente(String cliente) {
        return pedidoRepository.findByClienteIgnoreCaseOrderByFechaCargaDesc(cliente).stream().map(this::toDto).toList();
    }

    @Transactional
    public OrderResponseDTO cambiarEstado(String id, String estado) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        String nextState = estado == null ? "" : estado.trim().toUpperCase();
        if (!List.of("RECIBIDO", "EN_PREPARACION", "LISTO_FACTURAR", "FACTURADO", "EN_CAMINO", "ENTREGADO", "CANCELADO").contains(nextState))
            throw new IllegalArgumentException("Estado de pedido no válido");
        pedido.setEstado(nextState);
        if ("FACTURADO".equals(pedido.getEstado())) pedido.setFechaFacturacion(LocalDateTime.now());
        if ("ENTREGADO".equals(pedido.getEstado())) pedido.setFechaEntrega(LocalDateTime.now());
        return toDto(pedidoRepository.save(pedido));
    }

    private OrderResponseDTO toDto(Pedido p) {
        List<OrderResponseDTO.Line> lines = p.getItems().stream().map(i -> new OrderResponseDTO.Line(
                i.getProducto().getId(), i.getProducto().getNombre(), i.getCantidad(), i.getPrecioUnitario(), i.getTotal())).toList();
        return new OrderResponseDTO(p.getId(), p.getCliente(), p.getEstado(), p.getMedioPago(), p.getFechaCarga(), p.getFechaFacturacion(), p.getTotal(), lines);
    }
}
