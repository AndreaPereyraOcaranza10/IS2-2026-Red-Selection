package redSelection.dulceMayor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redSelection.dulceMayor.service.CotizacionDolarService;

@RestController
@RequestMapping("/api/exchange-rate")
public class CotizacionPortalController {
    private final CotizacionDolarService cotizacion;

    public CotizacionPortalController(CotizacionDolarService cotizacion) {
        this.cotizacion = cotizacion;
    }

    @GetMapping("/usd")
    public CotizacionDolarService.DollarQuote usd() {
        return cotizacion.obtenerCotizacion();
    }
}