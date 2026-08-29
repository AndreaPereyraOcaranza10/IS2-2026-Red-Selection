package com.example.mascotas.controladores;

import com.example.mascotas.dto.UsuarioDTO;
import com.example.mascotas.entidades.Usuario;
import com.example.mascotas.entidades.Zona;
import com.example.mascotas.errores.ErrorServicio;
import com.example.mascotas.repositorios.ZonaRepositorio;
import com.example.mascotas.servicios.UsuarioServicio;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/")
public class PortalControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private ZonaRepositorio zonaRepositorio;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String logout,
                        HttpSession session,
                        HttpServletRequest request,
                        ModelMap modelo) throws ErrorServicio {

        // Si el usuario ya tiene una sesion activa, lo mandamos directo a inicio
        if (session.getAttribute("usuariosession") != null) {
            return "redirect:/inicio";
        }

        if (logout == null && request.getCookies() != null) {
            for (Cookie cookie: request.getCookies()) {
                if (cookie.getName().equals("usuarioRecordado")) {
                    try {
                        UsuarioDTO usuario = usuarioServicio.buscarUsuario(cookie.getValue());
                        if (usuario != null) {
                            session.setAttribute("usuarioSession", usuario);
                            return "redirect:/inicio";
                        }
                    } catch (ErrorServicio ex) {

                    }
                }
            }
        }

        if (logout != null) {
            modelo.put("logout", "Ha salido correctamente de la plataforma");
        }

        return "login";
    }

    @GetMapping("/registro")
    public String registro(ModelMap modelo) {
        List<Zona> zonas = zonaRepositorio.findAll();
        modelo.put("zonas", zonas);
        return "registro";
    }

    @GetMapping("/inicio")
    public String inicio(HttpSession session) {
        // Protegemos la pantalla de inicio: si no hay usuario logueado, se redirige al login
        if (session.getAttribute("usuariosession") == null) {
            return "redirect:/login";
        }
        return "inicio";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String clave,
                        @RequestParam(required = false) String recordarme,
                        HttpSession session,
                        HttpServletResponse response,
                        ModelMap modelo) {
        try {
            UsuarioDTO usuario = usuarioServicio.login(email, clave);
            session.setAttribute("usuariosession", usuario);

            if (recordarme != null){
                Cookie cookie = new Cookie("usuarioRecordado", usuario.getId());
                cookie.setMaxAge(60*60*24*2); //2 dias en segundos
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                response.addCookie(cookie);
            }

            return "redirect:/inicio";
        } catch (ErrorServicio ex) {
            modelo.put("error", ex.getMessage());
            modelo.put("email", email);
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session,
                         HttpServletRequest request,
                         HttpServletResponse response) {
        // Elimina toda la informacion guardada en la sesion (incluido el usuario logueado)
        session.invalidate();

        Cookie cookie = new Cookie("usuarioRecordado", null);
        cookie.setMaxAge(0);   // expira inmediatamente
        cookie.setPath("/");
        response.addCookie(cookie);

        return "redirect:/login?logout";
    }

    //@RequestParam indica que son parametros de la request HTTP

    @PostMapping("/registrar")
    public String registrar(ModelMap modelo, MultipartFile archivo, @RequestParam String nombre, @RequestParam String apellido, @RequestParam String mail, @RequestParam String clave1, @RequestParam String clave2, @RequestParam String idZona) {

        try {
            usuarioServicio.registrar(archivo, nombre, apellido, mail, clave1, clave2, idZona);
        } catch (ErrorServicio ex) {
            List<Zona> zonas = zonaRepositorio.findAll();
            modelo.put("zonas", zonas);

            modelo.put("error", ex.getMessage());
            modelo.put("nombre", nombre);
            modelo.put("apellido", apellido);
            modelo.put("mail", mail);
            modelo.put("clave1", clave1);
            modelo.put("clave2", clave2);
            return "registro";
        }

        modelo.put("titulo", "Bienvenido a Tinder de Mascotas");
        modelo.put("descripcion", "tu usuario ha sido registrado correctamente");
        return "exito";
    }
}