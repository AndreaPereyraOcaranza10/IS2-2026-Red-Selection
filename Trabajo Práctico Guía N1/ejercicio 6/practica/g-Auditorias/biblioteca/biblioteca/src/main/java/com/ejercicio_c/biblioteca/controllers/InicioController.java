package com.ejercicio_c.biblioteca.controllers;


import com.ejercicio_c.biblioteca.entities.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


import jakarta.servlet.http.HttpSession;
import com.ejercicio_c.biblioteca.services.InicioAplicacionService;

@Controller
public class InicioController {

    @Autowired
    private InicioAplicacionService inicioAplicacionService;

    @GetMapping("/")
    public String index() {

        //Creo el usuario por defecto
        //try {
        //inicioAplicacionService.iniciarAplicacion();
        //}catch(Exception e) {}

        return "index";
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/inicio")
    public String inicio(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");

        if (usuario != null) {
            if (usuario.getRol().toString().equals("ADMIN")) {
                return "redirect:/admin/dashboard";
            }else {
                return "inicio";
            }
        }else {
            return "index";
        }
    }

    @GetMapping("/regresoPage")
    public String regreso() {
        return "redirect:/inicio";
    }
}
