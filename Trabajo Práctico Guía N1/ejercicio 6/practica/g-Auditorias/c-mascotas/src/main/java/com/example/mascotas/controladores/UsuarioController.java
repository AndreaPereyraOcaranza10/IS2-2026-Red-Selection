package com.example.mascotas.controladores;

import com.example.mascotas.entidades.Usuario;
import com.example.mascotas.entidades.Zona;
import com.example.mascotas.errores.ErrorServicio;
import com.example.mascotas.repositorios.ZonaRepositorio;
import com.example.mascotas.servicios.UsuarioServicio;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {
    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private ZonaRepositorio zonaRepositorio;


    @GetMapping("/editar-perfil")
    public String editarPerfil(HttpSession session, ModelMap modelo, @RequestParam String id){

        List<Zona> zonas = zonaRepositorio.findAll();
        modelo.put("zonas", zonas);

        Usuario logeado = (Usuario) session.getAttribute("usuariosession");
        if(logeado == null || !logeado.getId().equals(id)){
            return "/index";
        }
        try {
            Usuario usuario = usuarioServicio.buscarUsuario(id);
            modelo.addAttribute("perfil", usuario);
        }catch (ErrorServicio ex){
            modelo.addAttribute("error", ex.getMessage());
        }
        return "perfil";
    }

    @PostMapping("/actualizar-perfil")
    public String actualizar(HttpSession session, ModelMap modelo, MultipartFile archivo, @RequestParam String id, @RequestParam String nombre, @RequestParam String apellido, @RequestParam String mail, @RequestParam String clave1, @RequestParam String clave2, @RequestParam String idZona){
        Usuario usuario = null;
        try {
            usuario = usuarioServicio.buscarUsuario(id);
            usuarioServicio.modificar(archivo,id,nombre,apellido,mail,clave1,clave2, idZona);
            session.setAttribute("usuariosession", usuario);
            return "redirect:/inicio";
        }catch (ErrorServicio ex){
            List<Zona> zonas = zonaRepositorio.findAll();
            modelo.put("zonas", zonas);
            modelo.put("error", ex.getMessage());
            modelo.put("perfil", usuario);
            return "/perfil";
        }
    }

}