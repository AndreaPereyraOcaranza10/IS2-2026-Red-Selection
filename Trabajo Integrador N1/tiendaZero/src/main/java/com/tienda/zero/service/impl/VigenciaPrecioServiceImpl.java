package com.tienda.zero.service.impl;

import com.tienda.zero.model.Producto;
import com.tienda.zero.model.VigenciaPrecio;
import com.tienda.zero.repository.VigenciaPrecioRepository;
import com.tienda.zero.service.ProductoService;
import com.tienda.zero.service.VigenciaPrecioService;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class VigenciaPrecioServiceImpl implements VigenciaPrecioService {

    @Value("${tienda.inflacion.bimestral:0}")
    private double inflacionBimestral;

    private final VigenciaPrecioRepository vigenciaPrecioRepository;
    private final ProductoService productoService;

    public VigenciaPrecioServiceImpl(VigenciaPrecioRepository vigenciaPrecioRepository,
                                     ProductoService productoService) {
        this.vigenciaPrecioRepository = vigenciaPrecioRepository;
        this.productoService = productoService;
    }

    @Override
    public void crearVigenciaPrecio(Date fechaDesde, Date fechaHasta, double precio, String idProducto) {
        validar(fechaDesde, fechaHasta, precio, idProducto);

        Producto producto = productoService.buscarProducto(idProducto);

        VigenciaPrecio vigenciaActual = vigenciaPrecioRepository
                .findByProductoIdAndFechaHastaIsNullAndEliminadoFalse(idProducto);

        if (vigenciaActual != null) {
            vigenciaActual.setFechaHasta(fechaDesde);
            vigenciaPrecioRepository.save(vigenciaActual);
        }

        VigenciaPrecio vigenciaPrecio = VigenciaPrecio.builder()
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .precio(precio)
                .producto(producto)
                .eliminado(false)
                .build();

        vigenciaPrecioRepository.save(vigenciaPrecio);
    }

    @Override
    public void validar(Date fechaDesde, Date fechaHasta, double precio, String idProducto) {
        if (fechaDesde == null) {
            throw new IllegalArgumentException("La fecha desde es obligatoria");
        }
        if (fechaHasta != null && fechaHasta.before(fechaDesde)) {
            throw new IllegalArgumentException("La fecha hasta no puede ser anterior a la fecha desde");
        }
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
        if (idProducto == null || idProducto.isBlank()) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
    }

    @Override
    public VigenciaPrecio buscarVigenciaPrecio(String id) {
        return vigenciaPrecioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la vigencia de precio con id: " + id));
    }

    @Override
    public void modificarVigenciaPrecio(String id, Date fechaDesde, Date fechaHasta, double precio, String idProducto) {
        validar(fechaDesde, fechaHasta, precio, idProducto);

        VigenciaPrecio vigenciaPrecio = buscarVigenciaPrecio(id);
        Producto producto = productoService.buscarProducto(idProducto);

        vigenciaPrecio.setFechaDesde(fechaDesde);
        vigenciaPrecio.setFechaHasta(fechaHasta);
        vigenciaPrecio.setPrecio(precio);
        vigenciaPrecio.setProducto(producto);

        vigenciaPrecioRepository.save(vigenciaPrecio);
    }

    @Override
    public void eliminarVigenciaPrecio(String id) {
        VigenciaPrecio vigenciaPrecio = buscarVigenciaPrecio(id);
        vigenciaPrecio.setEliminado(true);
        vigenciaPrecioRepository.save(vigenciaPrecio);
    }

    @Override
    public List<VigenciaPrecio> listarVigenciaPrecio() {
        return vigenciaPrecioRepository.findAll();
    }

    @Override
    public List<VigenciaPrecio> listarVigenciaPrecioActivo() {
        return vigenciaPrecioRepository.findByEliminadoFalse();
    }

    @Override
    public VigenciaPrecio buscarVigenciaPrecioVigente(String idProducto) {
        VigenciaPrecio vigente = vigenciaPrecioRepository
                .findByProductoIdAndFechaHastaIsNullAndEliminadoFalse(idProducto);

        if (vigente == null) {
            throw new IllegalArgumentException("No hay una vigencia de precio activa para el producto: " + idProducto);
        }

        return vigente;
    }

    @Override
    @jakarta.transaction.Transactional
    public int actualizarPreciosPorInflacion(double porcentaje) {
        if (!Double.isFinite(porcentaje) || porcentaje < 0) {
            throw new IllegalArgumentException("El porcentaje de inflación debe ser un número mayor o igual a cero");
        }
        Date hoy = new Date(System.currentTimeMillis());
        int actualizados = 0;
        for (Producto producto : productoService.listarProductoActivo()) {
            VigenciaPrecio vigente = vigenciaPrecioRepository
                    .findByProductoIdAndFechaHastaIsNullAndEliminadoFalse(producto.getId());
            if (vigente == null) continue;
            // Evita cerrar una vigencia en la misma fecha en que comenzó.
            if (vigente.getFechaDesde().before(hoy)) {
                double nuevoPrecio = BigDecimal.valueOf(vigente.getPrecio())
                        .multiply(BigDecimal.ONE.add(BigDecimal.valueOf(porcentaje).movePointLeft(2)))
                        .setScale(2, RoundingMode.HALF_UP).doubleValue();
                crearVigenciaPrecio(hoy, null, nuevoPrecio, producto.getId());
                actualizados++;
            }
        }
        return actualizados;
    }

    @Scheduled(cron = "0 0 0 1 */2 *", zone = "America/Argentina/Buenos_Aires")
    public void actualizarPreciosBimestralmente() {
        if (inflacionBimestral > 0) actualizarPreciosPorInflacion(inflacionBimestral);
    }
}
