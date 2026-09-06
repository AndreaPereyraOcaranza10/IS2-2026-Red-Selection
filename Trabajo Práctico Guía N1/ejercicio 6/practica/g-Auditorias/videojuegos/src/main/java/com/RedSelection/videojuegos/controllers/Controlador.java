package com.RedSelection.videojuegos.controllers;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Controlador {
    @GetMapping(value = "/")
    public String index(Model model){
        String saludo = "hola thy";
        model.addAttribute("saludo", saludo);
        return "index";
    }
}
