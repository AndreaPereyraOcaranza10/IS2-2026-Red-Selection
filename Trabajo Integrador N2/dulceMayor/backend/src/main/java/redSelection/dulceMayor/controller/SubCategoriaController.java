package redSelection.dulceMayor.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redSelection.dulceMayor.dto.SubCategoriaCrearDTO;
import redSelection.dulceMayor.dto.SubCategoriaRespuestaDTO;
import redSelection.dulceMayor.service.SubCategoriaService;

@RestController
@RequestMapping("/api/subcategorias")
public class SubCategoriaController extends BaseController<SubCategoriaCrearDTO, SubCategoriaRespuestaDTO> {
    public SubCategoriaController(SubCategoriaService service) { super(service); }
    @Override protected String idDe(SubCategoriaRespuestaDTO r) { return r.id(); }
}