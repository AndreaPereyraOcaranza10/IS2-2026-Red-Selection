package com.example.mascotas.controladores;

import com.example.mascotas.entidades.Mascota;
import com.example.mascotas.entidades.Usuario;
import com.example.mascotas.errores.ErrorServicio;
import com.example.mascotas.servicios.MascotaServicio;
import com.example.mascotas.servicios.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
@RequestMapping("/foto")
public class FotoControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private MascotaServicio mascotaServicio;

    //@PatchVariable le indica que el id lo va a sacar de la url
    @GetMapping("/usuario/{id}")
    public ResponseEntity<byte[]> fotoUsuario(@PathVariable String id) {
        try {
            Usuario usuario = usuarioServicio.buscarUsuario(id);

            if (usuario.getFoto() == null) {
                throw new ErrorServicio("El usuario no tiene foto asignada");
            }

            byte[] foto = usuario.getFoto().getContenido();

            HttpHeaders headers = new HttpHeaders();
            //Setea en la cabecera de la respuesta que el tipo de contenido a devolver es imagen jpeg
            headers.setContentType(MediaType.IMAGE_JPEG);

            return new ResponseEntity<>(foto, headers, HttpStatusCode.valueOf(200));
        } catch (ErrorServicio ex) {
            Logger.getLogger(FotoControlador.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        }

    }

    @GetMapping("/mascota/{id}")
    public ResponseEntity<byte[]> fotoMascota(@PathVariable String id) {
        try {
            Mascota mascota = mascotaServicio.buscarMascota(id);

            if (mascota.getFoto() == null) {
                throw new ErrorServicio("La mascota no tiene foto asignada");
            }

            byte[] foto = mascota.getFoto().getContenido();

            HttpHeaders headers = new HttpHeaders();
            //Setea en la cabecera de la respuesta que el tipo de contenido a devolver es imagen jpeg
            headers.setContentType(MediaType.IMAGE_JPEG);

            return new ResponseEntity<>(foto, headers, HttpStatusCode.valueOf(200));
        } catch (ErrorServicio ex) {
            Logger.getLogger(FotoControlador.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        }

    }
}
