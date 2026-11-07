package redSelection.dulceMayor.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redSelection.dulceMayor.dto.CategoriaCrearDTO;
import redSelection.dulceMayor.dto.CategoriaRespuestaDTO;
import redSelection.dulceMayor.service.CategoriaService;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController extends BaseController<CategoriaCrearDTO, CategoriaRespuestaDTO> {
    public CategoriaController(CategoriaService service) { super(service); }
    @Override protected String idDe(CategoriaRespuestaDTO r) { return r.id(); }
}
