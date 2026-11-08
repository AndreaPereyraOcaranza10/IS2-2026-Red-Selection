package redSelection.dulceMayor.controller;

import org.springframework.web.bind.annotation.*;
import redSelection.dulceMayor.dto.UnidadTerritorialDTO;
import redSelection.dulceMayor.service.UbicacionService;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class PortalLocationController {
    private final UbicacionService ubicaciones;

    public PortalLocationController(UbicacionService ubicaciones) {
        this.ubicaciones = ubicaciones;
    }

    @GetMapping("/provinces")
    public List<LocationOption> provinces() {
        return ubicaciones.provincias().stream().map(this::map).toList();
    }

    @GetMapping("/provinces/{provinceId}/departments")
    public List<LocationOption> departments(@PathVariable String provinceId) {
        return ubicaciones.departamentos(provinceId).stream().map(this::map).toList();
    }

    @GetMapping("/departments/{departmentId}/localities")
    public List<LocationOption> localities(@PathVariable String departmentId) {
        return ubicaciones.localidades(departmentId).stream().map(this::map).toList();
    }

    private LocationOption map(UnidadTerritorialDTO unidad) {
        return new LocationOption(unidad.id(), unidad.nombre());
    }

    public record LocationOption(String id, String name) {}
}