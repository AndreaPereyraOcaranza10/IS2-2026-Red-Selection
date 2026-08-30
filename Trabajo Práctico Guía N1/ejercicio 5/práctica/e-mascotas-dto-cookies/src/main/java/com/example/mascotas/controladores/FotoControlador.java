package com.example.mascotas.controladores;

import com.example.mascotas.dto.MascotaDTO;
import com.example.mascotas.dto.UsuarioDTO;
import com.example.mascotas.entidades.Foto;
import com.example.mascotas.entidades.Mascota;
import com.example.mascotas.entidades.Usuario;
import com.example.mascotas.errores.ErrorServicio;
import com.example.mascotas.servicios.FotoServicio;
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

    @Autowired
    private FotoServicio fotoServicio;

    //@PatchVariable le indica que el id lo va a sacar de la url
    @GetMapping("/usuario/{id}")
    public ResponseEntity<byte[]> fotoUsuario(@PathVariable String id) {
        try {
            UsuarioDTO usuario = usuarioServicio.buscarUsuario(id);

            if (usuario.getIdFoto() == null) {
                throw new ErrorServicio("El usuario no tiene foto asignada");
            }

            Foto foto = fotoServicio.buscarFoto(usuario.getIdFoto());
            byte[] contenido = foto.getContenido();

            HttpHeaders headers = new HttpHeaders();
            //Setea en la cabecera de la respuesta que el tipo de contenido a devolver es imagen jpeg
            headers.setContentType(MediaType.IMAGE_JPEG);

            return new ResponseEntity<>(contenido, headers, HttpStatusCode.valueOf(200));
        } catch (ErrorServicio ex) {
            Logger.getLogger(FotoControlador.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        }

    }

    @GetMapping("/mascota/{id}")
    public ResponseEntity<byte[]> fotoMascota(@PathVariable String id) {
        try {
            MascotaDTO mascota = mascotaServicio.buscarMascota(id);


            if (mascota.getIdFoto() == null) {
                throw new ErrorServicio("La mascota no tiene foto asignada");
            }

            Foto foto = fotoServicio.buscarFoto(mascota.getIdFoto());
            byte[] contenido = foto.getContenido();

            HttpHeaders headers = new HttpHeaders();
            //Setea en la cabecera de la respuesta que el tipo de contenido a devolver es imagen jpeg
            headers.setContentType(MediaType.IMAGE_JPEG);

            return new ResponseEntity<>(contenido, headers, HttpStatusCode.valueOf(200));
        } catch (ErrorServicio ex) {
            Logger.getLogger(FotoControlador.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        }

    }
}
