package redSelection.dulceMayor.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redSelection.dulceMayor.dto.PuntoVentaCrearDTO;
import redSelection.dulceMayor.dto.PuntoVentaRespuestaDTO;
import redSelection.dulceMayor.service.PuntoVentaService;

/** PATRÓN: Template Method (capa Controller). POST/GET/PUT/DELETE heredados de BaseController. */
@RestController
@RequestMapping("/api/puntos-venta")
public class PuntoVentaController extends BaseController<PuntoVentaCrearDTO, PuntoVentaRespuestaDTO> {

    public PuntoVentaController(PuntoVentaService puntoVentaService) {
        super(puntoVentaService);
    }

    @Override protected String idDe(PuntoVentaRespuestaDTO respuesta) { return respuesta.id(); }
}