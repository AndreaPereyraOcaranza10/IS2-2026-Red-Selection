package com.RedSelection.videojuegos.controllers;

import com.RedSelection.videojuegos.entities.Videojuego;
import com.RedSelection.videojuegos.services.ServicioCategoria;
import com.RedSelection.videojuegos.services.ServicioEstudio;
import com.RedSelection.videojuegos.services.ServicioVideojuego;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;

@Controller
public class controladorVideojuego {
    @Autowired
    private ServicioVideojuego svcVideojuego;
    @Autowired
    private ServicioCategoria svcCategoria;
    @Autowired
    private ServicioEstudio svcEstudio;

    @GetMapping("/inicio")
    public String inicio(Model model) {
        try {
            List<Videojuego> videojuegos = this.svcVideojuego.findAllByActivo();
            model.addAttribute("videojuegos", videojuegos);

            return "views/inicio";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    @GetMapping("/detalle/{id}")
    public String detalleVideojuego(Model model, @PathVariable("id") long id) {
        try {
            Videojuego videojuego = this.svcVideojuego.findByIdAndActivo(id);
            model.addAttribute("videojuego",videojuego);
            return "views/detalle";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    @GetMapping("/abm")
    public String abmVideojuego(Model model){
        try {
            List<Videojuego> videojuegos = this.svcVideojuego.findAll();
            model.addAttribute("videojuegos",videojuegos);
            return "views/abm";
        }catch(Exception e){
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    @GetMapping(value = "/busqueda")
    public String busquedaVideojuego(Model model, @RequestParam(value ="query",required = false)String q){
        try {
            List<Videojuego> videojuegos = this.svcVideojuego.findByTitle(q);
            model.addAttribute("videojuegos", videojuegos);
            model.addAttribute("resultado",q);
            return "views/busqueda";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    @GetMapping("/formulario/videojuego/{id}")
    public String formularioVideojuego(Model model,@PathVariable("id")long id){
        try {
            model.addAttribute("categorias",this.svcCategoria.findAll());
            model.addAttribute("estudios",this.svcEstudio.findAll());
            if(id==0){
                model.addAttribute("videojuego",new Videojuego());
            }else{
                model.addAttribute("videojuego",this.svcVideojuego.findById(id));
            }
            return "views/formulario/videojuego";
        }catch(Exception e){
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    @PostMapping("/formulario/videojuego/{id}")
    public String guardarVideojuego(
            @RequestParam("archivo") MultipartFile archivo,
            @Valid @ModelAttribute("videojuego") Videojuego videojuego,
            BindingResult result,
            Model model,@PathVariable("id")long id
    ) {

        try {
            model.addAttribute("categorias",this.svcCategoria.findAll());
            model.addAttribute("estudios",this.svcEstudio.findAll());
            if(result.hasErrors()){
                return "views/formulario/videojuego";
            }
            String ruta = "C://Videojuegos/imagenes";
            Files.createDirectories(Paths.get(ruta));

            if(id==0){
                if(archivo.isEmpty()){
                    model.addAttribute("errorImagenMsg","La imagen es requerida");
                    return "views/formulario/videojuego";
                }
                if(!this.validarExtension(archivo)){
                    model.addAttribute("errorImagenMsg","La extension no es valida");
                    return "views/formulario/videojuego";
                }
                if(archivo.getSize() >= 15000000){
                    model.addAttribute("errorImagenMsg","El peso excede 15MB");
                    return "views/formulario/videojuego";
                }
                String nombreFoto = this.generarNombreArchivo(archivo);
                Path rutaAbsoluta = Paths.get(ruta+"//"+nombreFoto);
                Files.write(rutaAbsoluta,archivo.getBytes());
                videojuego.setImagen(nombreFoto);
                this.svcVideojuego.saveOne(videojuego);
            }else{
                if(!archivo.isEmpty()){
                    if(!this.validarExtension(archivo)){
                        model.addAttribute("errorImagenMsg","La extension no es valida");
                        return "views/formulario/videojuego";
                    }
                    if(archivo.getSize() >= 15000000){
                        model.addAttribute("errorImagenMsg","El peso excede 15MB");
                        return "views/formulario/videojuego";
                    }
                    String nombreFoto = this.generarNombreArchivo(archivo);
                    Path rutaAbsoluta = Paths.get(ruta+"//"+nombreFoto);
                    Files.write(rutaAbsoluta,archivo.getBytes());
                    videojuego.setImagen(nombreFoto);
                }
                this.svcVideojuego.updateOne(videojuego,id);
            }
            return "redirect:/abm";
        }catch(Exception e){
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    private String generarNombreArchivo(MultipartFile archivo) {
        String nombreOriginal = archivo.getOriginalFilename();
        int index = nombreOriginal.lastIndexOf(".");
        String extension = index >= 0 ? nombreOriginal.substring(index) : "";
        return Calendar.getInstance().getTimeInMillis() + extension;
    }
    private boolean validarExtension(MultipartFile archivo) {
        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null) {
            return false;
        }
        String nombreEnMinuscula = nombreOriginal.toLowerCase();
        return nombreEnMinuscula.endsWith(".jpg")
                || nombreEnMinuscula.endsWith(".jpeg")
                || nombreEnMinuscula.endsWith(".png")
                || nombreEnMinuscula.endsWith(".gif")
                || nombreEnMinuscula.endsWith(".webp");
    }

    @GetMapping("/eliminar/videojuego/{id}")
    public String eliminarVideojuego(Model model,@PathVariable("id")long id){
        try {
            model.addAttribute("videojuego",this.svcVideojuego.findById(id));
            return "views/formulario/eliminar";
        }catch(Exception e){
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/eliminar/videojuego/{id}")
    public String desactivarVideojuego(Model model,@PathVariable("id")long id){
        try {
            this.svcVideojuego.deleteById(id);
            return "redirect:/abm";
        }catch(Exception e){
            model.addAttribute("error", e.getMessage());
            System.out.println(e);
            return "error";
        }
    }


}