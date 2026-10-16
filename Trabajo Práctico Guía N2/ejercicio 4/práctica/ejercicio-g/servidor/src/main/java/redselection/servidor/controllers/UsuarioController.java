package redselection.servidor.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redselection.servidor.model.Usuario;
import redselection.servidor.services.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController extends BaseControllerImpl<Usuario, UsuarioService> {
    public UsuarioController(UsuarioService servicio) {
        super(servicio);
    }
}
