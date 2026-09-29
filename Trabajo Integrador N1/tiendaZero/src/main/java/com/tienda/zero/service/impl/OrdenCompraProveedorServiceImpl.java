package com.tienda.zero.service.impl;

import com.tienda.zero.model.OrdenCompraProveedor;
import com.tienda.zero.repository.OrdenCompraProveedorRepository;
import com.tienda.zero.service.OrdenCompraProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrdenCompraProveedorServiceImpl implements OrdenCompraProveedorService {

    private final OrdenCompraProveedorRepository ordenCompraProveedorRepository;

    @Override
    public OrdenCompraProveedor crearOrden(OrdenCompraProveedor ordenCompraProveedor) {
        ordenCompraProveedor.setFechaCreacion(new Date(System.currentTimeMillis()));
        ordenCompraProveedor.setEntregada(false);
        return ordenCompraProveedorRepository.save(ordenCompraProveedor);
    }

    @Override
    public OrdenCompraProveedor buscarOrden(String id) {
        Optional<OrdenCompraProveedor> o = ordenCompraProveedorRepository.findById(id);
        return o.orElse(null);
    }

    @Override
    public List<OrdenCompraProveedor> listarOrdenes() {
        return ordenCompraProveedorRepository.findAll();
    }

    @Override
    public List<OrdenCompraProveedor> listarOrdenesPendientes() {
        return ordenCompraProveedorRepository.findByEntregadaFalse();
    }

    @Override
    public OrdenCompraProveedor marcarEntregada(String id) {
        OrdenCompraProveedor o = buscarOrden(id);
        if (o != null) {
            o.setEntregada(true);
            return ordenCompraProveedorRepository.save(o);
        }
        return null;
    }
}
