package redSelection.dulceMayor.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redSelection.dulceMayor.dto.PromocionCrearDTO;
import redSelection.dulceMayor.dto.PromocionRespuestaDTO;
import redSelection.dulceMayor.dto.PromocionValidadaDTO;
import redSelection.dulceMayor.dto.ValidarPromocionDTO;
import redSelection.dulceMayor.service.PromocionService;

/**
 * PATRÓN: Template Method (capa Controller). El ABM (POST/GET/PUT/DELETE) es de administración y lo hereda de
 * BaseController; validar un código lo usa el preventista al armar el pedido.
 */
@RestController
@RequestMapping("/api/promociones")
public class PromocionController extends BaseController<PromocionCrearDTO, PromocionRespuestaDTO> {

    private final PromocionService promociones;

    public PromocionController(PromocionService promociones) {
        super(promociones);
        this.promociones = promociones;
    }

    @Override protected String idDe(PromocionRespuestaDTO r) { return r.id(); }

    @PostMapping("/validar")
    public PromocionValidadaDTO validar(@RequestBody ValidarPromocionDTO dto) {
        return promociones.validar(dto.code());
    }
}
