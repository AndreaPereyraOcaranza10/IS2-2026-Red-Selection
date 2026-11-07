package redSelection.dulceMayor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redSelection.dulceMayor.dto.TrayectoDTO;
import redSelection.dulceMayor.service.RuteoService;

@RestController
@RequestMapping("/api/ruteo")
public class RuteoController {

    private final RuteoService ruteoService;

    public RuteoController(RuteoService ruteoService) {
        this.ruteoService = ruteoService;
    }

    @GetMapping("/trayecto")
    public TrayectoDTO trayecto(@RequestParam double desdeLat,
                                @RequestParam double desdeLng,
                                @RequestParam double hastaLat,
                                @RequestParam double hastaLng) {
        return ruteoService.trayecto(desdeLat, desdeLng, hastaLat, hastaLng);
    }
}
