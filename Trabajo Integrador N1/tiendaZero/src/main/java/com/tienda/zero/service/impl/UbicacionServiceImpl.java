package com.tienda.zero.service.impl;

import com.tienda.zero.dto.UbicacionDTO;
import com.tienda.zero.model.Departamento;
import com.tienda.zero.model.Localidad;
import com.tienda.zero.model.Pais;
import com.tienda.zero.model.Provincia;
import com.tienda.zero.service.DepartamentoService;
import com.tienda.zero.service.LocalidadService;
import com.tienda.zero.service.PaisService;
import com.tienda.zero.service.ProvinciaService;
import com.tienda.zero.service.UbicacionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UbicacionServiceImpl implements UbicacionService {

    private final PaisService paisService;
    private final ProvinciaService provinciaService;
    private final DepartamentoService departamentoService;
    private final LocalidadService localidadService;

    public UbicacionServiceImpl(PaisService paisService, ProvinciaService provinciaService,
                                DepartamentoService departamentoService, LocalidadService localidadService) {
        this.paisService = paisService;
        this.provinciaService = provinciaService;
        this.departamentoService = departamentoService;
        this.localidadService = localidadService;
    }

    @Override
    public UbicacionDTO obtenerUbicaciones() {
        List<UbicacionDTO.ProvinciaDTO> provincias = new ArrayList<>();
        List<UbicacionDTO.DepartamentoDTO> departamentos = new ArrayList<>();
        List<UbicacionDTO.LocalidadDTO> localidades = new ArrayList<>();

        List<Pais> paises = paisService.listarPaisActivo();
        if (paises.isEmpty()) {
            return new UbicacionDTO(provincias, departamentos, localidades);
        }

        for (Provincia provincia : provinciaService.listarProvinciaActivo(paises.get(0).getId())) {
            provincias.add(new UbicacionDTO.ProvinciaDTO(provincia.getId(), provincia.getNombre()));

            for (Departamento departamento : departamentoService.listarDepartamentoActivo(provincia.getId())) {
                departamentos.add(new UbicacionDTO.DepartamentoDTO(
                        departamento.getId(), departamento.getNombre(), provincia.getId()));

                for (Localidad localidad : localidadService.listarLocalidadActivo(departamento.getId())) {
                    localidades.add(new UbicacionDTO.LocalidadDTO(
                            localidad.getId(), localidad.getNombre(), departamento.getId()));
                }
            }
        }
        return new UbicacionDTO(provincias, departamentos, localidades);
    }
}