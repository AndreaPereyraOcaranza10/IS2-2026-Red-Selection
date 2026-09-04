package com.ejercicio_c.biblioteca.controllers;

import com.ejercicio_c.biblioteca.exceptions.ErrorServiceException;
import com.ejercicio_c.biblioteca.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/dashboard")
    public String panelAdministrativo() {
         return "panel";
    }

    // usuario_list.html ya tenía el link "Cambiar Rol" apuntando acá, pero
    // no existía ningún método que lo manejara.
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/modificarRol/{id}")
    public String modificarRol(@PathVariable String id, ModelMap modelo) {
        try {
            usuarioService.cambiarRol(id);
        } catch (ErrorServiceException e) {
            modelo.put("error", e.getMessage());
        }
        return "redirect:/usuario/lista";
    }
}
