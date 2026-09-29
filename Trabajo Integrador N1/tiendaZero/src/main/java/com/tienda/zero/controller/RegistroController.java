package com.tienda.zero.controller;

import com.tienda.zero.enums.Sexo;
import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.enums.TipoUsuario;
import com.tienda.zero.model.Usuario;
import com.tienda.zero.service.NacionalidadService;
import com.tienda.zero.service.PersonaService;
import com.tienda.zero.service.RegistroService;
import com.tienda.zero.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Date;

@Controller
public class RegistroController {

    private final RegistroService registroService;
    private final UsuarioService usuarioService;
    private final PersonaService personaService;
    private final NacionalidadService nacionalidadService;

    public RegistroController(RegistroService registroService, UsuarioService usuarioService,
                              PersonaService personaService, NacionalidadService nacionalidadService) {
        this.registroService = registroService;
        this.usuarioService = usuarioService;
        this.personaService = personaService;
        this.nacionalidadService = nacionalidadService;
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarCliente(@RequestParam String correo, @RequestParam String clave,
                                   @RequestParam String repetirClave, Model model) {
        try {
            if (!clave.equals(repetirClave)) {
                throw new IllegalArgumentException("Las contraseñas no coinciden");
            }
            registroService.registrarCliente(correo, clave);
            model.addAttribute("mensaje", "¡Listo! Te enviamos un mail a " + correo + " con el código de activación.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "registro";
    }

    @GetMapping("/activar-cuenta")
    public String mostrarFormularioActivacion() {
        return "activar-cuenta";
    }

    @PostMapping("/activar-cuenta")
    public String activarCuenta(@RequestParam String correo, @RequestParam String codigo, Model model) {
        try {
            registroService.activarCuentaCliente(correo, codigo);
            model.addAttribute("activada", true);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("correo", correo);
        }
        return "activar-cuenta";
    }

    @GetMapping("/completar-perfil")
    public String mostrarFormularioPerfil(Authentication authentication, Model model) {
        Usuario usuario = usuarioService.buscarUsuarioPorNombreUsuario(authentication.getName());

        if (usuario.getRol() != TipoUsuario.CLIENTE) {
            return "redirect:/";
        }
        if (personaService.buscarPersonaPorUsuario(usuario.getId()).isPresent()) {
            return "redirect:/perfil-completo";
        }

        model.addAttribute("nacionalidades", nacionalidadService.listarNacionalidadActiva());
        return "completar-perfil";
    }

    @PostMapping("/completar-perfil")
    public String completarPerfil(Authentication authentication,
                                  @RequestParam String nombre, @RequestParam String apellido,
                                  @RequestParam Sexo sexo, @RequestParam String fechaNacimiento,
                                  @RequestParam TipoDocumento tipoDocumento, @RequestParam String numeroDocumento,
                                  @RequestParam String direccionEstadia, @RequestParam String idNacionalidad,
                                  @RequestParam String calle, @RequestParam String numeracion,
                                  @RequestParam(required = false) String barrio,
                                  @RequestParam(required = false) String manzanaPiso,
                                  @RequestParam(required = false) String casaDepartamento,
                                  @RequestParam(required = false) String referencia,
                                  @RequestParam String idLocalidad,
                                  @RequestParam(required = false) String telefono,
                                  Model model) {

        Usuario usuario = usuarioService.buscarUsuarioPorNombreUsuario(authentication.getName());

        try {
            registroService.completarPerfilCliente(usuario.getId(), nombre, apellido, sexo,
                    Date.valueOf(fechaNacimiento), tipoDocumento, numeroDocumento,
                    direccionEstadia, idNacionalidad,
                    calle, numeracion, barrio, manzanaPiso, casaDepartamento, referencia, idLocalidad,
                    telefono);
            return "redirect:/perfil-completo";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("nacionalidades", nacionalidadService.listarNacionalidadActiva());
            return "completar-perfil";
        }
    }

    @GetMapping("/perfil-completo")
    public String perfilCompleto() {
        return "perfil-completo";
    }
}