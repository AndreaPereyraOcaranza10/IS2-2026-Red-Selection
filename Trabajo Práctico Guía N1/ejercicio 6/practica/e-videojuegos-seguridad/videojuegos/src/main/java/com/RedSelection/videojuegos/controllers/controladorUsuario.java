package com.RedSelection.videojuegos.controllers;

import com.RedSelection.videojuegos.services.ServicioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/usuario")
public class controladorUsuario {

    @Autowired
    private ServicioUsuario svcUsuario;

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }
        return "views/login";
    }

    @GetMapping("/registrar")
    public String registrar() {
        return "views/registro";
    }

    @PostMapping("/registro")
    public String registro(@RequestParam String nombre,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String password2,
                           Model model) {
        try {
            svcUsuario.crearUsuario(nombre, email, password, password2);
            return "redirect:/usuario/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("nombre", nombre);
            model.addAttribute("email", email);
            return "views/registro";
        }
    }
}
