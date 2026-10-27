package com.tienda.zero.service.impl;

import com.tienda.zero.dto.EmpresaFormDTO;
import com.tienda.zero.model.Contacto;
import com.tienda.zero.model.ContactoCorreoElectronico;
import com.tienda.zero.model.ContactoTelefonico;
import com.tienda.zero.model.Direccion;
import com.tienda.zero.model.Empresa;
import com.tienda.zero.repository.EmpleadoRepository;
import com.tienda.zero.service.ContactoCorreoElectronicoService;
import com.tienda.zero.service.ContactoService;
import com.tienda.zero.service.ContactoTelefonicoService;
import com.tienda.zero.service.DireccionService;
import com.tienda.zero.service.EmpresaService;
import com.tienda.zero.service.GestionEmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GestionEmpresaServiceImpl implements GestionEmpresaService {

    private static final String CORREO = "CORREO";
    private static final String TELEFONO = "TELEFONO";

    private final EmpresaService empresaService;
    private final DireccionService direccionService;
    private final ContactoService contactoService;
    private final ContactoCorreoElectronicoService correoService;
    private final ContactoTelefonicoService telefonoService;
    private final EmpleadoRepository empleadoRepository;

    @Override
    @Transactional
    public Empresa crearEmpresa(EmpresaFormDTO formulario) {
        validarFormulario(formulario);
        Direccion direccion = crearDireccion(formulario);
        Contacto contacto = crearContacto(formulario);
        return empresaService.crearEmpresa(formulario.getRazonSocial(), formulario.getCuit(),
                formulario.getTipoEmpresa(), direccion, contacto);
    }

    @Override
    @Transactional
    public void modificarEmpresa(String id, EmpresaFormDTO formulario) {
        validarFormulario(formulario);
        Empresa empresa = empresaService.buscarEmpresa(id);
        if (empresa.isEliminado()) {
            throw new IllegalArgumentException("No se puede modificar una empresa eliminada");
        }

        Direccion direccion = empresa.getDireccion();
        direccionService.modificarDireccion(direccion.getId(), formulario.getCalle(), formulario.getNumeracion(),
                formulario.getBarrio(), formulario.getManzanaPiso(), formulario.getCasaDepartamento(),
                formulario.getReferencia(), formulario.getIdLocalidad());

        Contacto contactoAnterior = empresa.getContacto();
        Contacto contactoActualizado = actualizarContacto(contactoAnterior, formulario);
        empresaService.modificarEmpresa(id, formulario.getRazonSocial(), formulario.getCuit(),
                formulario.getTipoEmpresa(), direccion, contactoActualizado);

        if (!contactoAnterior.getId().equals(contactoActualizado.getId())) {
            contactoService.eliminarContacto(contactoAnterior.getId());
        }
    }

    @Override
    @Transactional
    public void eliminarEmpresa(String id) {
        Empresa empresa = empresaService.buscarEmpresa(id);
        if (empresa.isEliminado()) {
            throw new IllegalArgumentException("La empresa ya se encuentra eliminada");
        }
        if (empleadoRepository.existsByEmpresaIdAndEliminadoFalse(id)) {
            throw new IllegalArgumentException("No se puede eliminar la empresa porque tiene empleados activos");
        }
        empresaService.eliminarEmpresa(id);
        direccionService.eliminarDireccion(empresa.getDireccion().getId());
        contactoService.eliminarContacto(empresa.getContacto().getId());
    }

    private Direccion crearDireccion(EmpresaFormDTO formulario) {
        return direccionService.crearDireccion(formulario.getCalle(), formulario.getNumeracion(),
                formulario.getBarrio(), formulario.getManzanaPiso(), formulario.getCasaDepartamento(),
                formulario.getReferencia(), formulario.getIdLocalidad());
    }

    private Contacto crearContacto(EmpresaFormDTO formulario) {
        if (CORREO.equals(formulario.getMedioContacto())) {
            return correoService.crearContactoCorreoElectronico(formulario.getEmail(),
                    formulario.getTipoContacto(), formulario.getObservacion());
        }
        return telefonoService.crearContactoTelefonico(formulario.getTelefono(), formulario.getTipoTelefono(),
                formulario.getTipoContacto(), formulario.getObservacion());
    }

    private Contacto actualizarContacto(Contacto contacto, EmpresaFormDTO formulario) {
        if (CORREO.equals(formulario.getMedioContacto()) && contacto instanceof ContactoCorreoElectronico) {
            correoService.modificarContactoCorreoElectronico(contacto.getId(), formulario.getEmail(),
                    formulario.getTipoContacto(), formulario.getObservacion());
            return contacto;
        }
        if (TELEFONO.equals(formulario.getMedioContacto()) && contacto instanceof ContactoTelefonico) {
            telefonoService.modificarContactoTelefonico(contacto.getId(), formulario.getTelefono(),
                    formulario.getTipoTelefono(), formulario.getTipoContacto(), formulario.getObservacion());
            return contacto;
        }
        return crearContacto(formulario);
    }

    private void validarFormulario(EmpresaFormDTO formulario) {
        if (formulario == null) {
            throw new IllegalArgumentException("Los datos de la empresa son obligatorios");
        }
        if (!CORREO.equals(formulario.getMedioContacto()) && !TELEFONO.equals(formulario.getMedioContacto())) {
            throw new IllegalArgumentException("El medio de contacto no es valido");
        }
    }
}
