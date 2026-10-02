package com.tienda.zero.service.impl;

import com.tienda.zero.model.OrdenCompraProveedor;
import com.tienda.zero.repository.OrdenCompraProveedorRepository;
import com.tienda.zero.service.OrdenCompraProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrdenCompraProveedorServiceImpl implements OrdenCompraProveedorService {

    private final OrdenCompraProveedorRepository ordenCompraProveedorRepository;

    @Override
    @Transactional
    public OrdenCompraProveedor crearOrden(OrdenCompraProveedor ordenCompraProveedor) {
        LocalDateTime fechaHora = LocalDateTime.now();
        ordenCompraProveedor.setFechaCreacion(Date.valueOf(fechaHora.toLocalDate()));
        ordenCompraProveedor.setFechaHoraCreacion(fechaHora);
        ordenCompraProveedor.setEntregada(false);
        return ordenCompraProveedorRepository.saveAndFlush(ordenCompraProveedor);
    }

    @Override
    @Transactional
    public OrdenCompraProveedor modificarOrden(OrdenCompraProveedor ordenCompraProveedor) {
        return ordenCompraProveedorRepository.saveAndFlush(ordenCompraProveedor);
    }

    @Override
    @Transactional
    public void eliminarOrden(String id) {
        OrdenCompraProveedor orden = buscarOrden(id);
        if (orden == null) {
            throw new IllegalArgumentException("No existe la orden de compra con id: " + id);
        }
        if (orden.isEntregada()) {
            throw new IllegalArgumentException(
                    "No se puede eliminar una orden ya recibida. Anulá primero su factura.");
        }
        orden.setEliminado(true);
        ordenCompraProveedorRepository.saveAndFlush(orden);
    }

    @Override
    public OrdenCompraProveedor buscarOrden(String id) {
        return ordenCompraProveedorRepository.findById(id)
                .filter(o -> !o.isEliminado())
                .orElse(null);
    }

    @Override
    public List<OrdenCompraProveedor> listarOrdenes() {
        return ordenCompraProveedorRepository.findByEliminadoFalseOrderByFechaCreacionDesc();
    }

    @Override
    public List<OrdenCompraProveedor> listarOrdenesPendientes() {
        return ordenCompraProveedorRepository.findByEntregadaFalseAndEliminadoFalse();
    }

    @Override
    @Transactional
    public OrdenCompraProveedor marcarEntregada(String id) {
        OrdenCompraProveedor o = buscarOrden(id);
        if (o != null) {
            o.setEntregada(true);
            return ordenCompraProveedorRepository.saveAndFlush(o);
        }
        return null;
    }

    @Override
    @Transactional
    public OrdenCompraProveedor marcarPendiente(String id) {
        OrdenCompraProveedor o = buscarOrden(id);
        if (o != null) {
            o.setEntregada(false);
            return ordenCompraProveedorRepository.saveAndFlush(o);
        }
        return null;
    }

}
