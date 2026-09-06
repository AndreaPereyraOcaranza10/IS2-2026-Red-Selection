package com.ejercicio_c.biblioteca.controllers;

import com.ejercicio_c.biblioteca.entities.Usuario;
import com.ejercicio_c.biblioteca.exceptions.ErrorServiceException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.ejercicio_c.biblioteca.services.UsuarioService;

import java.util.List;

// Faltaba @Controller (Spring nunca registraba esta clase como bean, así que
// ninguna de estas rutas existía) y el @RequestMapping("/usuario") que los
// templates (login.html, index.html, navbar) ya esperaban con ese prefijo.
@Controller
@RequestMapping("/usuario")
public class PortalController {

    @Autowired
    UsuarioService userService;

    @GetMapping("/registrar")
    public String registrar() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registro(@RequestParam String nombre,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String password2,
                           ModelMap model) {
        try{
            // El servicio se llama crearUsuario, no registrar (no compilaba)
            userService.crearUsuario(nombre, email, password, password2, null);
            model.put("exito", "Usuario registrado correctamente");
            return "index";

        } catch (Exception e) {

            model.put("error", e.getMessage());
            model.put("nombre", nombre);
            model.put("email", email);

            return "registro";
        }
    }

    @GetMapping("/login")
    public String login(@RequestParam (required = false) String error,
                        @RequestParam (required = false) String logout,
                        ModelMap model) {
        if (error != null) {
            model.put("error", "Usuario o contraseña incorrectos");
        }
        return "login";
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/inicio")
    public String inicio(HttpSession session) {

        Usuario logueado = (Usuario) session.getAttribute("usuariosession");

        // Evita NullPointerException si se llega acá sin sesión iniciada
        if (logueado == null) {
            return "redirect:/usuario/login";
        }

        if (logueado.getRol().toString().equals("ADMIN")) {
            return "redirect:/admin/dashboard";
        }

        return "inicio";
    }

    //////////////////////////////////////////
    //////////////////////////////////////////
    ///////////// VIEW: LISTA USUARIOS ////////
    //////////////////////////////////////////
    //////////////////////////////////////////
    // usuario_list.html ya esperaba esta ruta (link "Modificar" y "Cambiar Rol"),
    // pero no existía ningún controller que la sirviera.
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/lista")
    public String listar(ModelMap modelo) {

        List<Usuario> usuarios = userService.listarUsuario();
        modelo.addAttribute("usuarios", usuarios);

        return "usuario_list";
    }

    //////////////////////////////////////////
    //////////////////////////////////////////
    ///////////// VIEW: MODIFICAR USUARIO /////
    //////////////////////////////////////////
    //////////////////////////////////////////
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/modificar/{id}")
    public String irEditModificar(@PathVariable String id, ModelMap modelo) {

        try {
            Usuario usuario = userService.buscarUsuario(id);
            modelo.put("usuario", usuario);
            modelo.put("id", id);

            return "usuario_modificar";

        } catch (ErrorServiceException e) {
            modelo.put("error", e.getMessage());
            return "usuario_list";
        }
    }

    // El formulario de usuario_modificar.html postea a /usuario/perfil/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/perfil/{id}")
    public String aceptarEditModificar(@PathVariable String id,
                                       @RequestParam String nombre,
                                       @RequestParam String email,
                                       @RequestParam(required = false) String password,
                                       @RequestParam(required = false) String password2,
                                       @RequestParam(required = false) MultipartFile archivo,
                                       ModelMap modelo) {

        try {
            userService.modificarUsuario(id, nombre, email, password, password2, archivo);
            modelo.put("exito", "El usuario fue modificado correctamente");

            return "redirect:/usuario/lista";

        } catch (ErrorServiceException e) {

            modelo.put("error", e.getMessage());
            modelo.put("id", id);
            try {
                modelo.put("usuario", userService.buscarUsuario(id));
            } catch (ErrorServiceException ignored) {}

            return "usuario_modificar";
        }
    }
}
