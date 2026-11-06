package com.dulcemayor.controller;

import com.dulcemayor.dto.EmpresaSucursalDTO;
import com.dulcemayor.enums.TipoContacto;
import com.dulcemayor.enums.TipoEmpresa;
import com.dulcemayor.model.ContactoCorreoElectronico;
import com.dulcemayor.model.Empresa;
import com.dulcemayor.repository.EmpresaRepository;
import com.dulcemayor.service.ContactoCorreoElectronicoService;
import com.dulcemayor.service.ContactoService;
import com.dulcemayor.service.DireccionService;
import com.dulcemayor.service.EmpresaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/companies/branches")
@RequiredArgsConstructor
public class EmpresaAdminRestController extends ApiControllerTemplate {
    private final EmpresaRepository empresas;
    private final EmpresaService empresaService;
    private final DireccionService direccionService;
    private final ContactoCorreoElectronicoService emails;
    private final ContactoService contactos;

    @GetMapping
    @Transactional(readOnly = true)
    public List<EmpresaSucursalDTO> listar() {
        return ejecutarEndpoint(() -> empresas
                .findByTipoEmpresaAndEliminadoFalseOrderByRazonSocialAsc(TipoEmpresa.SUCURSAL)
                .stream().map(this::toDto).toList());
    }

    @PostMapping
    @Transactional
    public EmpresaSucursalDTO crear(@RequestBody EmpresaSucursalDTO input) {
        return ejecutarEndpoint(() -> {
            validar(input);
            var direccion = direccionService.crearDireccion(input.getCalle().trim(), input.getNumeracion().trim(),
                    limpio(input.getBarrio()), limpio(input.getManzanaPiso()), limpio(input.getCasaDepartamento()),
                    limpio(input.getReferencia()), input.getLocalidadId());
            var contacto = emails.crearContactoCorreoElectronico(input.getEmail().trim(), TipoContacto.EMPRESA, "Contacto de sucursal");
            Empresa empresa = empresaService.crearEmpresa(input.getRazonSocial(), input.getCuit(), TipoEmpresa.SUCURSAL,
                    direccion, contacto);
            return toDto(empresa);
        });
    }

    @PutMapping("/{id}")
    @Transactional
    public EmpresaSucursalDTO modificar(@PathVariable String id, @RequestBody EmpresaSucursalDTO input) {
        return ejecutarEndpoint(() -> {
            validar(input);
            Empresa empresa = buscarSucursal(id);
            direccionService.modificarDireccion(empresa.getDireccion().getId(), input.getCalle().trim(),
                    input.getNumeracion().trim(), limpio(input.getBarrio()), limpio(input.getManzanaPiso()),
                    limpio(input.getCasaDepartamento()), limpio(input.getReferencia()), input.getLocalidadId());

            var currentContact = empresa.getContacto();
            if (currentContact instanceof ContactoCorreoElectronico emailContact) {
                emails.modificarContactoCorreoElectronico(emailContact.getId(), input.getEmail().trim(),
                        TipoContacto.EMPRESA, "Contacto de sucursal");
            } else {
                var replacement = emails.crearContactoCorreoElectronico(input.getEmail().trim(), TipoContacto.EMPRESA,
                        "Contacto de sucursal");
                empresaService.modificarEmpresa(id, input.getRazonSocial(), input.getCuit(), TipoEmpresa.SUCURSAL,
                        empresa.getDireccion(), replacement);
                contactos.eliminarContacto(currentContact.getId());
                return toDto(buscarSucursal(id));
            }
            empresaService.modificarEmpresa(id, input.getRazonSocial(), input.getCuit(), TipoEmpresa.SUCURSAL,
                    empresa.getDireccion(), currentContact);
            return toDto(buscarSucursal(id));
        });
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void eliminar(@PathVariable String id) {
        ejecutarEndpoint(() -> {
            buscarSucursal(id);
            empresaService.eliminarEmpresa(id);
            return null;
        });
    }

    private Empresa buscarSucursal(String id) {
        Empresa empresa = empresaService.buscarEmpresa(id);
        if (empresa.getTipoEmpresa() != TipoEmpresa.SUCURSAL || empresa.isEliminado()) {
            throw new EntityNotFoundException("No se encontró la sucursal con id: " + id);
        }
        return empresa;
    }

    private void validar(EmpresaSucursalDTO input) {
        if (input == null) throw new IllegalArgumentException("Los datos de la sucursal son obligatorios.");
        if (input.getRazonSocial() == null || input.getRazonSocial().isBlank()) throw new IllegalArgumentException("El nombre de la sucursal es obligatorio.");
        if (input.getCuit() == null || input.getCuit().isBlank()) throw new IllegalArgumentException("El CUIT es obligatorio.");
        if (input.getCalle() == null || input.getCalle().isBlank()) throw new IllegalArgumentException("La calle es obligatoria.");
        if (input.getNumeracion() == null || input.getNumeracion().isBlank()) throw new IllegalArgumentException("La numeración es obligatoria.");
        if (input.getLocalidadId() == null || input.getLocalidadId().isBlank()) throw new IllegalArgumentException("La localidad es obligatoria.");
        if (input.getEmail() == null || !input.getEmail().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Ingresá un email válido para la sucursal.");
    }

    private String limpio(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private EmpresaSucursalDTO toDto(Empresa empresa) {
        var dto = new EmpresaSucursalDTO();
        var direccion = empresa.getDireccion();
        var localidad = direccion.getLocalidad();
        dto.setId(empresa.getId());
        dto.setRazonSocial(empresa.getRazonSocial());
        dto.setCuit(empresa.getCuit());
        dto.setCalle(direccion.getCalle());
        dto.setNumeracion(direccion.getNumeracion());
        dto.setBarrio(direccion.getBarrio());
        dto.setManzanaPiso(direccion.getManzanaPiso());
        dto.setCasaDepartamento(direccion.getCasaDepartamento());
        dto.setReferencia(direccion.getReferencia());
        dto.setLocalidadId(localidad.getId());
        dto.setLocalidad(localidad.getNombre());
        dto.setDepartamento(localidad.getDepartamento().getNombre());
        dto.setProvincia(localidad.getDepartamento().getProvincia().getNombre());
        if (empresa.getContacto() instanceof ContactoCorreoElectronico email) dto.setEmail(email.getEmail());
        return dto;
    }
}
