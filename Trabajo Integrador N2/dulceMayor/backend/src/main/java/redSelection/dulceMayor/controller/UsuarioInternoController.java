package redSelection.dulceMayor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redSelection.dulceMayor.dto.ClaveRestablecidaDTO;
import redSelection.dulceMayor.dto.UsuarioInternoCrearDTO;
import redSelection.dulceMayor.dto.UsuarioInternoRespuestaDTO;
import redSelection.dulceMayor.service.UsuarioInternoService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioInternoController extends BaseController<UsuarioInternoCrearDTO, UsuarioInternoRespuestaDTO> {

    private final UsuarioInternoService usuarios;

    public UsuarioInternoController(UsuarioInternoService usuarios) {
        super(usuarios);
        this.usuarios = usuarios;
    }

    @Override
    protected String idDe(UsuarioInternoRespuestaDTO respuesta) {
        return respuesta.id();
    }

    @PostMapping("/{id}/reiniciar-clave")
    public ResponseEntity<ClaveRestablecidaDTO> reiniciarClave(@PathVariable String id) {
        return ResponseEntity.ok(usuarios.reiniciarClave(id));
    }
}
