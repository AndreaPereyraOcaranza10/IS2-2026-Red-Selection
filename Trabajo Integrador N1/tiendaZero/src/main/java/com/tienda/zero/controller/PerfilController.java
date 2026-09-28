package com.tienda.zero.controller;

import com.tienda.zero.dto.PerfilDTO;
import com.tienda.zero.enums.Sexo;
import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.enums.TipoTelefono;
import com.tienda.zero.model.Provincia;
import com.tienda.zero.repository.ProvinciaRepository;
import com.tienda.zero.service.PerfilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PerfilController {

    private final PerfilService perfilService;
    private final ProvinciaRepository provinciaRepository;

    /**
     * Muestra la pantalla de perfil del cliente con todos los campos especificados:
     * Nombre, Apellido, Sexo, Fecha de Nacimiento, Dirección (Provincia, Departamento,
     * Localidad, Código Postal, Calle, N° Calle, Manzana/Piso, N° Casa/Departamento) y Teléfono.
     */
    @GetMapping("/perfil")
    public String showProfile(@RequestParam(value = "primeraVez", required = false) String primeraVez,
                              Authentication authentication,
                              Model model) {
        String email = authentication.getName();
        PerfilDTO perfilDTO = perfilService.obtenerPerfilUsuario(email);

        model.addAttribute("perfil", perfilDTO);
        model.addAttribute("sexos", Sexo.values());
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        model.addAttribute("tiposTelefono", TipoTelefono.values());

        List<Provincia> provincias = provinciaRepository.findAll();
        model.addAttribute("provincias", provincias);

        if (primeraVez != null) {
            model.addAttribute("mensajeBienvenida",
                    "¡Bienvenido/a a Tienda Zero! Por favor completa tu información personal y domicilio para habilitar tus compras y envíos.");
        }

        return "tienda/cliente/perfil";
    }

    /**
     * Guarda o actualiza los datos personales y de contacto/dirección del cliente.
     */
    @PostMapping("/perfil")
    public String saveProfile(@ModelAttribute("perfil") PerfilDTO perfilDTO,
                              Authentication authentication,
                              Model model) {
        String email = authentication.getName();
        try {
            perfilService.guardarPerfilUsuario(email, perfilDTO);
            model.addAttribute("mensajeExito", "¡Tu información personal ha sido guardada exitosamente!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensajeError", e.getMessage());
        } catch (Exception e) {
            log.error("Error al guardar perfil de usuario: ", e);
            model.addAttribute("mensajeError", "Ocurrió un error al guardar los datos del perfil.");
        }

        model.addAttribute("sexos", Sexo.values());
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        model.addAttribute("tiposTelefono", TipoTelefono.values());
        model.addAttribute("provincias", provinciaRepository.findAll());

        return "tienda/cliente/perfil";
    }
}

