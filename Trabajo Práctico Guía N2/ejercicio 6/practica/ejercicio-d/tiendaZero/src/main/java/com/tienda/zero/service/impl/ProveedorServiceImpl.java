package com.tienda.zero.service.impl;

import com.tienda.zero.model.Contacto;
import com.tienda.zero.model.Direccion;
import com.tienda.zero.model.Proveedor;
import com.tienda.zero.repository.ProveedorRepository;
import com.tienda.zero.service.DireccionService;
import com.tienda.zero.service.ProveedorService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorServiceImpl(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @Override
    @Transactional
    public Proveedor crearProveedor(String razonSocial, List<Contacto> contactos) {
        validar(razonSocial);
        //trim se encarga de limpiar el formato cuando el usuario carga los datos, borra los espacios demás
        String razonSocialLimpia = razonSocial.trim();

        if (proveedorRepository.findByRazonSocialIgnoreCaseAndEliminadoFalse(razonSocialLimpia).isPresent()){
            throw new IllegalArgumentException("Ya existe un proveedor con esa razón social");
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial(razonSocialLimpia);
        proveedor.setEliminado(false);
        if (contactos != null){
            proveedor.getContactos().addAll(contactos);
        }

        return proveedorRepository.save(proveedor);
    }

    @Override
    public void validar(String razonSocial) {
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new IllegalArgumentException("La razón social es obligatoria");
        }
    }

    @Override
    public Proveedor buscarProveedor(String id) {
        Optional<Proveedor> resultado = proveedorRepository.findById(id);
        if (resultado.isEmpty()){
            throw new IllegalArgumentException("No existe proveedor con id: " + id);
        }
        return resultado.get();
    }

    @Override
    public Proveedor buscarProveedorPorRazonSocial(String razonSocial) {
        Optional<Proveedor> resultado = proveedorRepository.findByRazonSocialIgnoreCaseAndEliminadoFalse(razonSocial);
        if (resultado.isEmpty()){
            throw new IllegalArgumentException("No existe el proveedor con razón social: " + razonSocial);
        }
        return resultado.get();
    }

    @Override
    @Transactional
    public Proveedor modificarProveedor(String id, String razonSocial, List<Contacto> contactos) {
        validar(razonSocial);
        Proveedor proveedor = buscarProveedor(id);
        String razonSocialLimpia = razonSocial.trim();

        Optional<Proveedor> existente = proveedorRepository.findByRazonSocialIgnoreCaseAndEliminadoFalse(razonSocialLimpia);

        if (existente.isPresent() && !existente.get().getId().equals(id)) {
            throw new IllegalArgumentException("Ya existe otro proveedor con esa razón social");
        }

        proveedor.setRazonSocial(razonSocialLimpia);
        if (contactos != null){
            proveedor.getContactos().clear();
            proveedor.getContactos().addAll(contactos);
        }

        return proveedorRepository.save(proveedor);
    }

    @Override
    @Transactional
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

}