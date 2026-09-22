package com.tienda.zero.service.impl;

import com.tienda.zero.model.Direccion;
import com.tienda.zero.model.Proveedor;
import com.tienda.zero.repository.ProveedorRepository;
import com.tienda.zero.service.DireccionService;
import com.tienda.zero.service.ProveedorService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final DireccionService direccionService;

    public ProveedorServiceImpl(ProveedorRepository proveedorRepository,
                                DireccionService direccionService) {
        this.proveedorRepository = proveedorRepository;
        this.direccionService = direccionService;
    }

    @Override
    public Proveedor crearProveedor(String razonSocial, String cuit, String email,
                                    String telefono, String idDireccion) {
        validar(razonSocial, cuit);
        String cuitLimpio = normalizarCuit(cuit);
        proveedorRepository.findByCuit(cuitLimpio).ifPresent(p -> {
            throw new IllegalArgumentException("Ya existe un proveedor con ese CUIT");
        });

        Proveedor proveedor = Proveedor.builder()
                .razonSocial(razonSocial)
                .cuit(cuitLimpio)
                .email(email)
                .telefono(telefono)
                .direccion(buscarDireccionOpcional(idDireccion))
                .eliminado(false)
                .build();

        return proveedorRepository.save(proveedor);
    }

    @Override
    public void validar(String razonSocial, String cuit) {
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new IllegalArgumentException("La razón social es obligatoria");
        }
        if (cuit == null || cuit.isBlank()) {
            throw new IllegalArgumentException("El CUIT es obligatorio");
        }
        if (normalizarCuit(cuit).length() != 11) {
            throw new IllegalArgumentException("El CUIT debe tener 11 dígitos");
        }
    }

    @Override
    public Proveedor buscarProveedor(String id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el proveedor con id: " + id));
    }

    @Override
    public Proveedor buscarProveedorPorCuit(String cuit) {
        return proveedorRepository.findByCuit(normalizarCuit(cuit))
                .orElseThrow(() -> new IllegalArgumentException("No existe el proveedor con CUIT: " + cuit));
    }

    @Override
    public Proveedor modificarProveedor(String id, String razonSocial, String cuit, String email,
                                        String telefono, String idDireccion) {
        validar(razonSocial, cuit);
        Proveedor proveedor = buscarProveedor(id);
        String cuitLimpio = normalizarCuit(cuit);

        proveedorRepository.findByCuit(cuitLimpio)
                .filter(p -> !p.getId().equals(id))
                .ifPresent(p -> {
                    throw new IllegalArgumentException("Ya existe otro proveedor con ese CUIT");
                });

        proveedor.setRazonSocial(razonSocial);
        proveedor.setCuit(cuitLimpio);
        proveedor.setEmail(email);
        proveedor.setTelefono(telefono);
        proveedor.setDireccion(buscarDireccionOpcional(idDireccion));

        return proveedorRepository.save(proveedor);
    }

    @Override
    public void eliminarProveedor(String id) {
        Proveedor proveedor = buscarProveedor(id);
        proveedor.setEliminado(true);
        proveedorRepository.save(proveedor);
    }

    @Override
    public List<Proveedor> listarProveedor() {
        return proveedorRepository.findAll();
    }

    @Override
    public List<Proveedor> listarProveedorActivo() {
        return proveedorRepository.findByEliminadoFalse();
    }

    private String normalizarCuit(String cuit) {
        return cuit.replaceAll("\\D", "");
    }

    private Direccion buscarDireccionOpcional(String idDireccion) {
        if (idDireccion == null || idDireccion.isBlank()) {
            return null;
        }
        return direccionService.buscarDireccion(idDireccion);
    }
}