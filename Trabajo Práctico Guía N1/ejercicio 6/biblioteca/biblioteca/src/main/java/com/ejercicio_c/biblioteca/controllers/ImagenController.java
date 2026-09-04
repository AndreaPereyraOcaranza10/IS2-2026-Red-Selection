
package com.ejercicio_c.biblioteca.controllers;


import com.ejercicio_c.biblioteca.entities.Imagen;
import com.ejercicio_c.biblioteca.entities.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ejercicio_c.biblioteca.services.ImagenService;
import com.ejercicio_c.biblioteca.services.UsuarioService;


@Controller
@RequestMapping("/imagen")
public class ImagenController {
    
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ImagenService imagenService;
    
    @GetMapping("/perfil/{id}")
    public ResponseEntity<byte[]> imagenUsuario (@PathVariable String id){
    	
      try {  
    	  
       Usuario usuario = usuarioService.buscarUsuario(id);
        
       byte[] imagen= usuario.getImagen().getContenido();
       
       HttpHeaders headers = new HttpHeaders();
       // Antes se forzaba siempre IMAGE_JPEG, sin importar el mime real guardado
       headers.setContentType(MediaType.parseMediaType(usuario.getImagen().getMime()));

       return new ResponseEntity<>(imagen,headers, HttpStatus.OK);
       
      }catch(Exception e) {
       return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    }

    // libro_list.html pedía la portada como /imagen/perfil/{libro.id}, pero
    // ese endpoint busca la imagen de un Usuario, no de un Libro, así que
    // nunca funcionaba. Este endpoint busca directamente por el id propio
    // de la Imagen (libro.imagen.id, usuario.imagen.id, etc.).
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> imagenPorId(@PathVariable String id){

      try {

       Imagen imagen = imagenService.buscarImagen(id);

       HttpHeaders headers = new HttpHeaders();
       headers.setContentType(MediaType.parseMediaType(imagen.getMime()));

       return new ResponseEntity<>(imagen.getContenido(), headers, HttpStatus.OK);

      }catch(Exception e) {
       return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    }

}
