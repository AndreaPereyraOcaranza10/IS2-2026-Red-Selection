package redselection.cliente.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/** Pone el nombre del usuario logueado disponible en todas las vistas (para el navbar). */
@ControllerAdvice
public class UsuarioActualAdvice {

    @ModelAttribute("usuarioActual")
    public String usuarioActual(Principal principal) {
        return principal == null ? null : principal.getName();
    }
}
