package com.tienda.zero.service.impl;

import com.tienda.zero.enums.TipoEmpresa;
import com.tienda.zero.model.Contacto;
import com.tienda.zero.model.Direccion;
import com.tienda.zero.model.Empresa;
import com.tienda.zero.repository.EmpresaRepository;
import com.tienda.zero.service.ContactoService;
import com.tienda.zero.service.DireccionService;
import com.tienda.zero.service.EmpresaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final ContactoService contactoService;
    private final DireccionService direccionService;

    @Override
    @Transactional
    public Empresa crearEmpresa(String razonSocial, String cuit, TipoEmpresa tipoEmpresa,
                                Direccion direccion, Contacto contacto) {
        validar(razonSocial, cuit, tipoEmpresa, direccion, contacto);

        String cuitNormalizado = normalizarCuit(cuit);
        if (empresaRepository.existsByCuit(cuitNormalizado)) {
            throw new IllegalArgumentException("Ya existe una empresa con el CUIT " + cuitNormalizado);
        }

        Empresa empresa = Empresa.builder()
                .razonSocial(razonSocial.trim())
                .cuit(cuitNormalizado)
                .tipoEmpresa(tipoEmpresa)
                .direccion(direccionService.buscarDireccion(direccion.getId()))
                .contacto(contactoService.buscarContacto(contacto.getId()))
                .build();

        return empresaRepository.save(empresa);
    }

    @Override
    public void validar(String razonSocial, String cuit, TipoEmpresa tipoEmpresa,
                        Direccion direccion, Contacto contacto) {
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new IllegalArgumentException("La razón social es obligatoria");
        }
        if (cuit == null || cuit.isBlank()) {
            throw new IllegalArgumentException("El CUIT es obligatorio");
        }
        if (!normalizarCuit(cuit).matches("\\d{11}")) {
            throw new IllegalArgumentException("El CUIT debe tener 11 dígitos (con o sin guiones)");
        }
        if (tipoEmpresa == null) {
            throw new IllegalArgumentException("El tipo de empresa es obligatorio");
        }
        if (direccion == null || direccion.getId() == null) {
            throw new IllegalArgumentException("La dirección es obligatoria y debe estar creada previamente");
        }
        if (contacto == null || contacto.getId() == null) {
            throw new IllegalArgumentException("El contacto es obligatorio y debe estar creado previamente");
        }
        if (contacto.isEliminado()) {
            throw new IllegalArgumentException("No se puede asociar un contacto eliminado");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Empresa buscarEmpresa(String id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la empresa con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Empresa buscarEmpresaPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        return empresaRepository.findFirstByRazonSocialIgnoreCaseAndEliminadoFalse(nombre.trim())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la empresa: " + nombre));
    }

    @Override
    @Transactional
    public void modificarEmpresa(String id, String razonSocial, String cuit, TipoEmpresa tipoEmpresa,
                                 Direccion direccion, Contacto contacto) {
        Empresa empresa = buscarEmpresa(id);

        if (empresa.isEliminado()) {
            throw new IllegalArgumentException("No se puede modificar una empresa eliminada");
        }

        validar(razonSocial, cuit, tipoEmpresa, direccion, contacto);

        String cuitNormalizado = normalizarCuit(cuit);
        if (empresaRepository.existsByCuitAndIdNot(cuitNormalizado, id)) {
            throw new IllegalArgumentException("Ya existe otra empresa con el CUIT " + cuitNormalizado);
        }

        empresa.setRazonSocial(razonSocial.trim());
        empresa.setCuit(cuitNormalizado);
        empresa.setTipoEmpresa(tipoEmpresa);
        empresa.setDireccion(direccionService.buscarDireccion(direccion.getId()));
        empresa.setContacto(contactoService.buscarContacto(contacto.getId()));

        empresaRepository.save(empresa);
    }

    @Override
    @Transactional
    public void eliminarEmpresa(String id) {
        Empresa empresa = buscarEmpresa(id);
        empresa.setEliminado(true);
        empresaRepository.save(empresa);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empresa> listarEmpresa() {
        return empresaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empresa> listarEmpresaActiva() {
        return empresaRepository.findByEliminadoFalse();
    }

    private String normalizarCuit(String cuit) {
        return cuit.replaceAll("[\\s-]", "");
    }
}