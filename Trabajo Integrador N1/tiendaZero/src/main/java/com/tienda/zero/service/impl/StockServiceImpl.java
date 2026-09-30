package com.tienda.zero.service.impl;

import com.tienda.zero.enums.EstadoFactura;
import com.tienda.zero.model.DetalleFactura;
import com.tienda.zero.model.DetalleCompra;
import com.tienda.zero.model.Factura;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.Stock;
import com.tienda.zero.repository.DetalleFacturaRepository;
import com.tienda.zero.repository.FacturaRepository;
import com.tienda.zero.repository.ProductoRepository;
import com.tienda.zero.repository.StockRepository;
import com.tienda.zero.service.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final DetalleFacturaRepository detalleFacturaRepository;
    private final FacturaRepository facturaRepository;
    private final ProductoRepository productoRepository;

    public StockServiceImpl(StockRepository stockRepository,
                            DetalleFacturaRepository detalleFacturaRepository,
                            FacturaRepository facturaRepository,
                            ProductoRepository productoRepository) {
        this.stockRepository = stockRepository;
        this.detalleFacturaRepository = detalleFacturaRepository;
        this.facturaRepository = facturaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional
    public Stock crearStock(String idDetalleFactura) {
        validar(idDetalleFactura);
        DetalleFactura detalle = buscarDetalleFactura(idDetalleFactura);
        Factura factura = buscarFacturaDelDetalle(idDetalleFactura);

        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new IllegalArgumentException("No se puede mover el stock con una factura anulada");
        }
        if (stockRepository.countByDetalleFacturaIdAndEliminadoFalse(idDetalleFactura) > 0) {
            throw new IllegalArgumentException("Ese detalle de factura ya movió el stock");
        }

        int movimiento = factura.signoStock() * detalle.getCantidad();
        String observacion = "Factura N° " + factura.getNumeroFactura();
        return registrarMovimiento(detalle.getProducto(), detalle, null, movimiento, observacion, "");
    }

    @Override
    public void validar(String idDetalleFactura) {
        if (idDetalleFactura == null || idDetalleFactura.isBlank()) {
            throw new IllegalArgumentException("El detalle de factura es obligatorio");
        }
    }

    @Override
    @Transactional
    public Stock revertirStock(String idDetalleFactura, String observacion) {
        validar(idDetalleFactura);
        DetalleFactura detalle = buscarDetalleFactura(idDetalleFactura);
        Factura factura = buscarFacturaDelDetalle(idDetalleFactura);

        long movimientos = stockRepository.countByDetalleFacturaIdAndEliminadoFalse(idDetalleFactura);
        if (movimientos == 0) {
            throw new IllegalArgumentException("Ese detalle de factura todavía no movió el stock");
        }
        if (movimientos > 1) {
            throw new IllegalArgumentException("Ese detalle de factura ya fue revertido");
        }

        int movimiento = -factura.signoStock() * detalle.getCantidad();
        String texto = "Anulación de factura N° " + factura.getNumeroFactura();
        if (observacion != null && !observacion.isBlank()) {
            texto = texto + ": " + observacion.trim();
        }
        return registrarMovimiento(detalle.getProducto(), detalle, null, movimiento, texto,
                "No se puede anular la factura. ");
    }

    @Override
    @Transactional
    public Stock registrarMovimiento(String idProducto, int movimiento, String observacion,
                                     DetalleCompra detalleCompra) {
        validarProducto(idProducto);
        if (movimiento == 0) {
            throw new IllegalArgumentException("El movimiento de stock no puede ser cero");
        }
        Producto producto = productoRepository.findByIdForUpdate(idProducto)
                .filter(item -> !item.isEliminado())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        return registrarMovimiento(producto, null, detalleCompra, movimiento, observacion, "");
    }

    @Override
    public Stock buscarStock(String id) {
        Optional<Stock> resultado = stockRepository.findById(id);
        if (resultado.isEmpty()) {
            throw new IllegalArgumentException("No existe el movimiento de stock con id: " + id);
        }
        return resultado.get();
    }

    @Override
    public Stock buscarStockActual(String idProducto) {
        validarProducto(idProducto);
        Optional<Stock> resultado = stockRepository
                .findFirstByProductoIdAndEliminadoFalseOrderByFechaDesc(idProducto);
        if (resultado.isEmpty()) {
            throw new IllegalArgumentException("El producto todavía no tiene movimientos de stock");
        }
        return resultado.get();
    }

    @Override
    public int calcularStockActual(String idProducto) {
        validarProducto(idProducto);
        Long suma = stockRepository.calcularStockActual(idProducto);
        if (suma == null) {
            return 0;
        }
        return suma.intValue();
    }

    @Override
    public List<Stock> listarStock(String idProducto) {
        validarProducto(idProducto);
        return stockRepository.findByProductoIdAndEliminadoFalseOrderByFechaDesc(idProducto);
    }

    /** Aplica el movimiento sobre el saldo actual y lo guarda. Nunca deja el saldo por debajo de cero. */
    private Stock registrarMovimiento(Producto producto, DetalleFactura detalleFactura,
                                      DetalleCompra detalleCompra, int movimiento,
                                      String observacion, String prefijoError) {
        String idProducto = producto.getId();
        int saldoAnterior = calcularStockActual(idProducto);
        int saldoNuevo = saldoAnterior + movimiento;

        if (saldoNuevo < 0) {
            throw new IllegalArgumentException(prefijoError + "Stock insuficiente de \""
                    + producto.getNombre() + "\": hay " + saldoAnterior
                    + " y se necesitan " + (-movimiento));
        }

        Stock stock = new Stock();
        stock.setMovimiento(movimiento);
        stock.setCantidadActual(saldoNuevo);
        stock.setObservacion(observacion);
        stock.setFecha(LocalDateTime.now());
        stock.setEliminado(false);
        stock.setProducto(producto);
        stock.setDetalleFactura(detalleFactura);
        stock.setDetalleCompra(detalleCompra);
        return stockRepository.save(stock);
    }

    private DetalleFactura buscarDetalleFactura(String idDetalleFactura) {
        Optional<DetalleFactura> resultado = detalleFacturaRepository.findById(idDetalleFactura);
        if (resultado.isEmpty()) {
            throw new IllegalArgumentException("No existe el detalle de factura con id: " + idDetalleFactura);
        }
        return resultado.get();
    }

    private Factura buscarFacturaDelDetalle(String idDetalleFactura) {
        Optional<Factura> resultado = facturaRepository.findByDetallesId(idDetalleFactura);
        if (resultado.isEmpty()) {
            throw new IllegalArgumentException("El detalle de factura no pertenece a ninguna factura");
        }
        return resultado.get();
    }

    private void validarProducto(String idProducto) {
        if (idProducto == null || idProducto.isBlank()) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
    }
}
