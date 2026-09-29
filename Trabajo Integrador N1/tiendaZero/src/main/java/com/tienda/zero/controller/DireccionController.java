package com.tienda.zero.controller;

import com.tienda.zero.model.Pais;
import com.tienda.zero.service.DepartamentoService;
import com.tienda.zero.service.LocalidadService;
import com.tienda.zero.service.PaisService;
import com.tienda.zero.service.ProvinciaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DireccionController {

    private final PaisService paisService;
    private final ProvinciaService provinciaService;
    private final DepartamentoService departamentoService;
    private final LocalidadService localidadService;

    public DireccionController(PaisService paisService, ProvinciaService provinciaService,
                               DepartamentoService departamentoService, LocalidadService localidadService) {
        this.paisService = paisService;
        this.provinciaService = provinciaService;
        this.departamentoService = departamentoService;
        this.localidadService = localidadService;
    }

    public record OpcionDto(String id, String nombre) {}

    @GetMapping("/api/ubicacion/provincias")
    public List<OpcionDto> provincias() {
        List<Pais> paises = paisService.listarPaisActivo();
        if (paises.isEmpty()) {
            return List.of();
        }
        return provinciaService.listarProvinciaActivo(paises.get(0).getId()).stream()
                .map(p -> new OpcionDto(p.getId(), p.getNombre()))
                .toList();
    }

    @GetMapping("/api/ubicacion/departamentos")
    public List<OpcionDto> departamentos(@RequestParam String idProvincia) {
        return departamentoService.listarDepartamentoActivo(idProvincia).stream()
                .map(d -> new OpcionDto(d.getId(), d.getNombre()))
                .toList();
    }

    @GetMapping("/api/ubicacion/localidades")
    public List<OpcionDto> localidades(@RequestParam String idDepartamento) {
        return localidadService.listarLocalidadActivo(idDepartamento).stream()
                .map(l -> new OpcionDto(l.getId(), l.getNombre()))
                .toList();
    }
}