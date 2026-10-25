package com.example.mascotas.controladores;

import com.example.mascotas.entidades.Zona;
import com.example.mascotas.errores.ErrorServicio;
import com.example.mascotas.repositorios.ZonaRepositorio;
import com.example.mascotas.servicios.UsuarioServicio;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
                        @RequestParam(required = false) String error,
                        HttpSession session,
                        ModelMap modelo) throws ErrorServicio {

        // El filtro JWT carga estos datos sólo si el token es válido.
        if (session.getAttribute("usuariosession") != null) {
            return "redirect:/inicio";
        }

        if (error != null) {
            modelo.put("error", "El mail o la clave ingresados son incorrectos");
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

    @PreAuthorize("hasRole('USUARIO_REGISTRADO')")
    @GetMapping("/inicio")
    public String inicio(HttpSession session) {
        // Protegemos la pantalla de inicio: si no hay usuario logueado, se redirige al login
        if (session.getAttribute("usuariosession") == null) {
            return "redirect:/login";
        }
        return "inicio";
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
