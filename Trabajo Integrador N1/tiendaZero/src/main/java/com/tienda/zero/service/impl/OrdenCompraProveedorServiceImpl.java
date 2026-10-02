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
    public OrdenCompraProveedor buscarOrden(String id) {
        Optional<OrdenCompraProveedor> o = ordenCompraProveedorRepository.findById(id);
        return o.orElse(null);
    }

    @Override
    public List<OrdenCompraProveedor> listarOrdenes() {
        return ordenCompraProveedorRepository.findAllByOrderByFechaCreacionDescFechaHoraCreacionDescIdDesc();
    }

    @Override
    public List<OrdenCompraProveedor> listarOrdenesPendientes() {
        return ordenCompraProveedorRepository.findByEntregadaFalseOrderByFechaCreacionDescFechaHoraCreacionDescIdDesc();
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
