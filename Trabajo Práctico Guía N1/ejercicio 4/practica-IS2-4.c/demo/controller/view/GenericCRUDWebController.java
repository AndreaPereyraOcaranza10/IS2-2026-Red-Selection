package com.example.demo.controller.view;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public abstract class GenericCRUDWebController <T extends Object>{

   	private String nameClass;
   	protected boolean campoDesactivado;
    protected Object object;
    
    //Vistas de rotorno para navegabilidad
	private String viewList;
	private String redirectList;
	private String viewEdit; 
   	
	//Constructor
    public GenericCRUDWebController(T object){
    	nameClass= getNameObject(object);
    	viewList= "view/l"+ nameClass +".html";
    	redirectList= "redirect:/list"+ nameClass;
    	viewEdit= "view/e"+ nameClass +".html";
    }
    
	private String getNameObject(T object){
        return ((((T) object).getClass()).getSimpleName());
    }

    private String getNameClass() {
        return nameClass;
    }
    
    private String getValueIdFieldObject(T object) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	Field field = null;
        try {
            field = object.getClass().getDeclaredField("id");
        } catch (Exception e) {
            field = object.getClass().getSuperclass().getDeclaredField("id");
        }
        field.setAccessible(true);
        String id= (String) field.get(object);
        return id;
    }
	
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	///////////////// VIEW: Lista ///////////////
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	
	@GetMapping("/list")
	public String listTemplateMethod(Model model) {
		try {
			  
		  List<T> list = listObject();
		  model.addAttribute("list"+ getNameClass(), list);
 
		}catch(Exception e) {
		  model.addAttribute("msgError", e.getMessage());  
		}
		return viewList;
	}
	
	protected abstract List<T> listObject();
	
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	////////////// VIEW: NAVEGACION /////////////
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	
	@GetMapping("/edit")
	public String browsePageEdit(T object, Model model) {
		model.addAttribute("isDisabled", false);
		return viewEdit;
	}
	
	@GetMapping("")
	public String editTemplateMethod(@PathVariable("id") String id, Model model) {
		
		try {
			
		  T object = getObjectById(id);		
		  model.addAttribute("object"+ nameClass, object);
		  model.addAttribute("isDisabled", true);
		  
		  return viewEdit;
		 
		}catch(Exception e) {	
		  model.addAttribute("msgError", e.getMessage());
		  return viewList;
		}		  
	}
	
	protected abstract T getObjectById(String id);
	
	@GetMapping("")
	public String browsePageEditTemplateMethod(@PathVariable("id") String id, Model model) {
		
		try {
			
		  T object = getObjectById(id);		
	      model.addAttribute("object"+ nameClass, object);
		  model.addAttribute("isDisabled", false);
		  
		  return viewEdit;
		 
		}catch(Exception e) {	
		  model.addAttribute("msgError", e.getMessage());
		  return viewList;
		}		  
	}
	
	@GetMapping("")
	public String eliminateTemplateMethod(@PathVariable("id") String id, RedirectAttributes attributes, Model model) {	
		
		try {
			
		  eliminate(id);		
		  attributes.addFlashAttribute("msgExito", "La acción fue realizada correctamente.");
		  return redirectList;
		  
		}catch(Exception e) {	
		   model.addAttribute("msgError", e.getMessage());
		   return redirectList;
		} 
	}
	
	protected abstract void eliminate(String id);
	
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	///////////////// VIEW: Edit ////////////////
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	
	@PostMapping("")
	public String acceptEditTemplateMethod(T object, BindingResult result, RedirectAttributes attributes, Model model){
		
		try {
			
		  if (result.hasErrors()){		
			model.addAttribute("msgError", "Error de Sistema");
			return viewEdit;
		  }
		 
		  executeUseCase(object);
			  
		  attributes.addFlashAttribute("msgExito", "La acción fue realizada correctamente.");
		  return redirectList;
		  
		}catch(Exception e) {
			  model.addAttribute("msgError", "Error de Sistema");
			  return viewEdit;
		}
		
	}
	
	protected abstract void executeUseCase(T object);
	
	@GetMapping("/cancelEdit")
	public String cancelEdit() {
		return redirectList;
	}
	

}
