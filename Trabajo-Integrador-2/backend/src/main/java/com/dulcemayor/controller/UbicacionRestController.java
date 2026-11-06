package com.dulcemayor.controller;

import com.dulcemayor.model.Departamento;
import com.dulcemayor.model.Localidad;
import com.dulcemayor.model.Pais;
import com.dulcemayor.model.Provincia;
import com.dulcemayor.repository.DepartamentoRepository;
import com.dulcemayor.repository.LocalidadRepository;
import com.dulcemayor.repository.PaisRepository;
import com.dulcemayor.repository.ProvinciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class UbicacionRestController extends ApiControllerTemplate {
    private final PaisRepository paises;
    private final ProvinciaRepository provincias;
    private final DepartamentoRepository departamentos;
    private final LocalidadRepository localidades;

    @GetMapping("/provinces")
    public List<LocationOption> provinces() {
        return ejecutarEndpoint(() -> {
            Pais argentina = paises.findByNombre("Argentina")
                    .orElseThrow(() -> new IllegalArgumentException("No hay ubicaciones configuradas."));
            return provincias.findByPaisIdAndEliminadoFalse(argentina.getId()).stream()
                    .sorted(Comparator.comparing(Provincia::getNombre))
                    .map(p -> new LocationOption(p.getId(), p.getNombre())).toList();
        });
    }

    @GetMapping("/provinces/{provinceId}/departments")
    public List<LocationOption> departments(@PathVariable String provinceId) {
        return ejecutarEndpoint(() -> departamentos.findByProvinciaIdAndEliminadoFalse(provinceId).stream()
                .sorted(Comparator.comparing(Departamento::getNombre))
                .map(d -> new LocationOption(d.getId(), d.getNombre())).toList());
    }

    @GetMapping("/departments/{departmentId}/localities")
    public List<LocationOption> localities(@PathVariable String departmentId) {
        return ejecutarEndpoint(() -> localidades.findByDepartamentoIdAndEliminadoFalse(departmentId).stream()
                .sorted(Comparator.comparing(Localidad::getNombre))
                .map(l -> new LocationOption(l.getId(), l.getNombre())).toList());
    }

    public record LocationOption(String id, String name) {}
}
