package com.example.demo.controller.view;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.business.domain.entity.Nacionalidad;
import com.example.demo.business.domain.entity.Pais;
import com.example.demo.business.logic.error.ErrorServiceException;
import com.example.demo.business.logic.service.NacionalidadService;

@Controller
@RequestMapping("/nacionalidad")
public class NacionalidadController {

	@Autowired
   	private NacionalidadService nacionalidadService;
   	
	private String viewList="view/nacionalidad/lNacionalidad.html";
	private String redirectList= "redirect:/nacionalidad/listNacionalidad";
	private String viewEdit="view/nacionalidad/eNacionalidad.html";
	
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	/////////// VIEW: lNacionalidad /////////////
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	
	@GetMapping("/listNacionalidad")
	public String listarNacionalidad(Model model) {
		try {
			  
		  List<Nacionalidad> listaNacionalidad = nacionalidadService.listarNacionalidadActivo();
		  model.addAttribute("listaNacionalidad", listaNacionalidad);

		}catch(ErrorServiceException e) {	
		  model.addAttribute("msgError", e.getMessage());  
		}catch(Exception e) {
		  model.addAttribute("msgError", "Error de Sistema");  
		}
		return viewList;
	}
	
	@GetMapping("/altaNacionalidad")
	public String alta(Nacionalidad nacionalidad, Model model) {
		model.addAttribute("isDisabled", false);
		return viewEdit;
	}
	
	@GetMapping("/consultar")
	public String consultar(@RequestParam(value="id") String idNacionalidad, Model model) {
		
		try {
			
		  Nacionalidad nacionalidad = nacionalidadService.buscarNacionalidad(idNacionalidad);		
		  model.addAttribute("nacionalidad", nacionalidad);
		  model.addAttribute("isDisabled", true);
		  
		  return viewEdit;
		 
		}catch(ErrorServiceException e) {	
		  model.addAttribute("msgError", e.getMessage());
		  return viewList;
		}		  
	}
	
	@GetMapping("/modificar")
	public String modificar(@RequestParam(value="id") String idNacionalidad, Model model) {
		
		try {
			
		  Nacionalidad nacionalidad = nacionalidadService.buscarNacionalidad(idNacionalidad);		
		  model.addAttribute("nacionalidad", nacionalidad);
		  model.addAttribute("isDisabled", false);
		  
		  return viewEdit;
		 
		}catch(ErrorServiceException e) {	
		  model.addAttribute("msgError", e.getMessage());
		  return viewList;
		}		  
	}
	
	@GetMapping("/baja")
	public String baja(@RequestParam(value="id") String idNacionalidad, RedirectAttributes attributes, Model model) {	
		
		try {
			
		  nacionalidadService.eliminarNacionalidad(idNacionalidad);		
		  attributes.addFlashAttribute("msgExito", "La acción fue realizada correctamente.");
		  return redirectList;
		  
		}catch(ErrorServiceException e) {	
		   model.addAttribute("msgError", e.getMessage());
		   return redirectList;
		} 
	}
	
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	//////////// VIEW: eNacionalidad ////////////
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	
	@PostMapping("/aceptarEditNacionalidad")
	public String aceptarEdit(@RequestParam(value="id") String idNacionalidad, @RequestParam(value="nombre") String nombreNacionalidad, RedirectAttributes attributes, Model model){
		
		try {
			
		  if (idNacionalidad == null || idNacionalidad.trim().isEmpty())
		   nacionalidadService.crearNacionalidad(nombreNacionalidad);
		  else 
		   nacionalidadService.modificarNacionalidad(idNacionalidad, nombreNacionalidad);
			  
		  attributes.addFlashAttribute("msgExito", "La acción fue realizada correctamente.");
		  return redirectList;
		  
		}catch(ErrorServiceException e) {
			  return error (e.getMessage(), model, idNacionalidad, nombreNacionalidad);
		}catch(Exception e) {
			  return error ("Error de Sistema", model, idNacionalidad, nombreNacionalidad);
		}
		
	}

	private String error (String mensaje, Model model, String id, String nombre) {
		try {
			
			model.addAttribute("msgError", mensaje);
			if (id != null && !id.trim().isEmpty()) {
			 model.addAttribute("nacionalidad", nacionalidadService.buscarNacionalidad(id));
			}else {
			  Nacionalidad nacionalidad = new Nacionalidad();
			  nacionalidad.setId("");
			  nacionalidad.setNombre(nombre);
			  model.addAttribute("nacionalidad",nacionalidad);	
			}
			
		}catch(Exception e) {}
		return viewEdit;
	}
	
	@GetMapping("/cancelarEditNacionalidad")
	public String cancelarEdit() {
		return redirectList;
	}
	

}
