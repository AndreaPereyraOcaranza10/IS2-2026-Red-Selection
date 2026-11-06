package com.dulcemayor.controller;

import com.dulcemayor.dto.EmpresaSucursalDTO;
import com.dulcemayor.enums.TipoEmpresa;
import com.dulcemayor.model.ContactoCorreoElectronico;
import com.dulcemayor.model.Empresa;
import com.dulcemayor.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/companies/branches")
@RequiredArgsConstructor
public class EmpresaPublicRestController extends ApiControllerTemplate {
    private final EmpresaRepository empresas;

    @GetMapping
    @Transactional(readOnly = true)
    public List<EmpresaSucursalDTO> listar() {
        return ejecutarEndpoint(() -> empresas
                .findByTipoEmpresaAndEliminadoFalseOrderByRazonSocialAsc(TipoEmpresa.SUCURSAL)
                .stream().map(this::toDto).toList());
    }

    private EmpresaSucursalDTO toDto(Empresa empresa) {
        var dto = new EmpresaSucursalDTO();
        var address = empresa.getDireccion();
        var locality = address.getLocalidad();
        dto.setId(empresa.getId());
        dto.setRazonSocial(empresa.getRazonSocial());
        dto.setCuit(empresa.getCuit());
        dto.setCalle(address.getCalle());
        dto.setNumeracion(address.getNumeracion());
        dto.setBarrio(address.getBarrio());
        dto.setLocalidadId(locality.getId());
        dto.setLocalidad(locality.getNombre());
        dto.setDepartamento(locality.getDepartamento().getNombre());
        dto.setProvincia(locality.getDepartamento().getProvincia().getNombre());
        if (empresa.getContacto() instanceof ContactoCorreoElectronico email) dto.setEmail(email.getEmail());
        return dto;
    }
}
