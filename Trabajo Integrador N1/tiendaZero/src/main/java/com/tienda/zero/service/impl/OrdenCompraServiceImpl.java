package com.tienda.zero.service.impl;

import com.tienda.zero.model.OrdenCompra;
import com.tienda.zero.repository.OrdenCompraRepository;
import com.tienda.zero.service.OrdenCompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrdenCompraServiceImpl implements OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;

    @Override
    public OrdenCompra crearOrden(OrdenCompra ordenCompra) {
        ordenCompra.setFechaCreacion(Date.from(Instant.now()));
        ordenCompra.setEntregada(false);
        return ordenCompraRepository.save(ordenCompra);
    }

    @Override
    public OrdenCompra buscarOrden(String id) {
        Optional<OrdenCompra> o = ordenCompraRepository.findById(id);
        return o.orElse(null);
    }

    @Override
    public List<OrdenCompra> listarOrdenes() {
        return ordenCompraRepository.findAll();
    }

    @Override
    public List<OrdenCompra> listarOrdenesPendientes() {
        return ordenCompraRepository.findByEntregadaFalse();
    }

    @Override
    public OrdenCompra marcarEntregada(String id) {
        OrdenCompra o = buscarOrden(id);
        if (o != null) {
            o.setEntregada(true);
            return ordenCompraRepository.save(o);
        }
        return null;
    }
}
