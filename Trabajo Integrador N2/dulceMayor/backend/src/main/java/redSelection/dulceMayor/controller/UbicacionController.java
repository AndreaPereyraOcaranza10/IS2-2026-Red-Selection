package redSelection.dulceMayor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redSelection.dulceMayor.dto.UbicacionInversaDTO;
import redSelection.dulceMayor.dto.UnidadTerritorialDTO;
import redSelection.dulceMayor.service.UbicacionService;

import java.util.List;

/** Desplegables en cascada: el front pide al backend, nunca a la API externa. */
@RestController
@RequestMapping("/api/ubicacion")
public class UbicacionController {

    private final UbicacionService ubicacionService;

    public UbicacionController(UbicacionService ubicacionService) {
        this.ubicacionService = ubicacionService;
    }

    @GetMapping("/provincias")
    public List<UnidadTerritorialDTO> provincias() {
        return ubicacionService.provincias();
    }

    @GetMapping("/departamentos")
    public List<UnidadTerritorialDTO> departamentos(@RequestParam String provinciaId) {
        return ubicacionService.departamentos(provinciaId);
    }

    @GetMapping("/localidades")
    public List<UnidadTerritorialDTO> localidades(@RequestParam String departamentoId) {
        return ubicacionService.localidades(departamentoId);
    }

    /** "Usar mi ubicación": provincia y departamento de unas coordenadas. 204 si no se pudieron determinar. */
    @GetMapping("/inversa")
    public ResponseEntity<UbicacionInversaDTO> inversa(@RequestParam double lat, @RequestParam double lon) {
        return ubicacionService.inversa(lat, lon)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
