package com.dulcemayor.controller;

import com.dulcemayor.service.CotizacionDolarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchange-rate")
@RequiredArgsConstructor
public class CotizacionRestController extends ApiControllerTemplate {
    private final CotizacionDolarService cotizacion;

    @GetMapping("/usd")
    public CotizacionDolarService.DollarQuote usd() {
        return ejecutarEndpoint(cotizacion::obtenerCotizacion);
    }
}
