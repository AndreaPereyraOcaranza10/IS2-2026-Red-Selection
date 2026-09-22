package com.example.mascotas.controladores;

import com.example.mascotas.entidades.Mascota;
import com.example.mascotas.entidades.Usuario;
import com.example.mascotas.entidades.Zona;
import com.example.mascotas.enumeracion.Sexo;
import com.example.mascotas.enumeracion.Tipo;
import com.example.mascotas.errores.ErrorServicio;
import com.example.mascotas.repositorios.MascotaRepositorio;
import com.example.mascotas.repositorios.ZonaRepositorio;
import com.example.mascotas.servicios.MascotaServicio;
import com.example.mascotas.servicios.UsuarioServicio;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping("/mascota")
public class MascotaControlador {
    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private MascotaServicio mascotaServicio;
    @Autowired
    private MascotaRepositorio mascotaRepositorio;

    @PostMapping("/eliminar-perfil")
    public String eliminar(HttpSession session, @RequestParam String id) {
        Usuario login = (Usuario)session.getAttribute("usuariosession");
        try {
            mascotaServicio.eliminar(login.getId(), id);
        } catch (ErrorServicio e) {
            throw new RuntimeException(e);
        }
        return "redirect:/mascota/mis-mascotas";
    }


    @GetMapping("/mis-mascotas")
    public String misMascotas(HttpSession session, ModelMap modelo){
        Usuario login = (Usuario)session.getAttribute("usuariosession");
        if (login == null) {
            return "redirect:/login";
        }

        List<Mascota> mascotas = mascotaServicio.buscarMascotasPorUsuario(login.getId());
        modelo.put("mascotas", mascotas);

        return "mascotas";
    }

    @GetMapping("/editar-perfil")
    public String editarPerfil(HttpSession session, ModelMap modelo, @RequestParam(required = false) String id, @RequestParam(required = false) String accion){

        if (accion == null) {
            accion = "Crear";
        }

        Usuario login = (Usuario)session.getAttribute("usuariosession");
        if (login == null) {
            return "redirect:/inicio";
        }

        Mascota mascota = new Mascota();

        if (id != null && !id.isEmpty()) {
            try {
                mascota = mascotaServicio.buscarMascota(id);
            } catch (ErrorServicio e) {
                throw new RuntimeException(e);
            }
        }

        modelo.put("perfil", mascota);
        modelo.put("nombre", mascota.getNombre());
        modelo.put("sexos", Sexo.values());
        modelo.put("tipos", Tipo.values());
        modelo.put("accion", accion);

        return "mascota";

    }

    @PostMapping("/actualizar-perfil")
    public String actualizar(HttpSession session, ModelMap modelo, MultipartFile archivo, @RequestParam String id, @RequestParam String nombre, @RequestParam Sexo sexo, @RequestParam Tipo tipo){

        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        try {

            if(id == null || id.isEmpty()) {
                mascotaServicio.agregarMascota(archivo,usuario.getId(),nombre,sexo,tipo);
            } else {
                mascotaServicio.modificar(archivo,usuario.getId(), id,nombre,sexo,tipo);
            }

            return "redirect:/inicio";
        } catch (ErrorServicio ex){

            Mascota mascota = new Mascota();

            mascota.setNombre(nombre);
            mascota.setSexo(sexo);
            mascota.setTipo(tipo);

            modelo.put("perfil", mascota);
            modelo.put("sexos", Sexo.values());
            modelo.put("tipos", Tipo.values());

            return "/mascota";
        }
    }

}