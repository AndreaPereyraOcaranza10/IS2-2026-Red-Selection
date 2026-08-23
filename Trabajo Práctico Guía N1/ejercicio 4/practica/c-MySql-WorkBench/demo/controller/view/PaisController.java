package com.example.demo.controller.view;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.business.domain.entity.Pais;
import com.example.demo.business.logic.error.ErrorServiceException;
import com.example.demo.business.logic.service.PaisService;

/*¿Qué es un controlador?
En el contexto de una aplicación web,
un controlador es una clase que maneja las solicitudes HTTP entrantes y determina cómo responder a ellas.
Los controladores son parte del patrón MVC (Modelo-Vista-Controlador) y son responsables de:
* Recibir las solicitudes HTTP.
* Llamar a los servicios  procesar la lógica de negocio.
* Devolver una respuesta HTTP adecuada al cliente.*/

/*@Controller: Indica a Spring que esta clase manejará las solicitudes HTTP entrantes
y actuará como intermediario entre el cliente y la lógica de negocio.*/

@Controller
public class PaisController {

	@Autowired
   	private PaisService paisService;
	
	/* redirect: En Spring Boot, la redirección es un mecanismo que redirige el navegador de un usuario 
	   a una URL diferente. Esto se suele emplear después de acciones como el envío de formularios, 
	   cuando se mueve un recurso o para dirigir a los usuarios a páginas específicas según la lógica. 
	 */
	
	private String viewList= "view/pais/lPais.html";
	private String redirectList= "redirect:/pais/listPais";
	private String viewEdit= "view/pais/ePais.html"; 
   	
	
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	///////////////// VIEW: lPais ///////////////
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	
	/*@GetMapping, @PostMapping, @PutMapping, @DeleteMapping
    Propósito: Mapean los métodos de la clase a las solicitudes HTTP GET, POST, PUT y DELETE respectivamente.
    Función: Permiten definir rutas específicas y asociar los métodos del controlador a estas rutas.
    Esto facilita la creación de endpoints RESTful.*/
	
	/*@GetMapping se utiliza para asignar solicitudes 
	 HTTP GET a métodos específicos de un controlador.*/
	
	@GetMapping("/pais/listPais")
	public String listarPais(Model model) {
		try {
			  
		  Collection<Pais> listaPais = paisService.listarPaisActivo();
		  
		  /*
		   * Transferencia de datos a la vista: En Spring MVC, la interfaz Model (de org.springframework.ui.Model), las clases ModelMap y ModelAndView 
		   * se utilizan para transferir datos del controlador a la vista.
           * Model: Esta interfaz proporciona una forma sencilla de añadir atributos (pares clave-valor) al modelo, a los que se puede acceder desde la vista.
           * ModelMap: Esta clase extiende Model y ofrece una funcionalidad similar, pero con una estructura similar a la de un mapa, lo que permite una 
           *           gestión de atributos más flexible.
           * ModelAndView: Esta clase actúa como contenedor de los datos del modelo y del nombre de la vista, lo que permite que se devuelvan juntos desde 
           *               un método del controlador.
		   */
		  model.addAttribute("listaPais", listaPais);

		}catch(ErrorServiceException e) {	
		  model.addAttribute("msgError", e.getMessage());  
		}catch(Exception e) {
		  model.addAttribute("msgError", "Error de Sistema");  
		}
		return viewList;    //"redirect:/pais/listPais"
	}
	
	/*@GetMapping se utiliza para asignar solicitudes 
	 HTTP GET a métodos específicos de un controlador.*/
	
	@GetMapping("/pais/altaPais")
	public String alta(Pais pais, Model model) {
		model.addAttribute("isDisabled", false);
		return viewEdit;                            //"view/pais/ePais.html"
	}
	
	/* @PathVariable: Vincula una variable de ruta en la URL de la solicitud con un parámetro del método.
     * Permite extraer valores de la URL y usarlos en el método del controlador.
     * Por ejemplo, @PathVariable ID id vincula el valor del segmento {id} de la URL al parámetro id
     */
	
	/* @GetMapping se utiliza para asignar solicitudes 
	 * HTTP GET a métodos específicos de un controlador.
	 */
	
	/* Model: En el contexto de Spring Boot y su framework web, Spring MVC, el "Modelo" se refiere al componente 
	 * responsable de transportar los datos y la lógica de negocio entre el controlador y la vista. Es un componente fundamental 
	 * del patrón arquitectónico Modelo-Vista-Controlador (MVC).
	 */
	
	@GetMapping("/pais/consultar/{id}")
	public String consultar(@PathVariable("id") String idPais, Model model) {
		
		try {
			
		  Pais pais = paisService.buscarPais(idPais);		
		  model.addAttribute("pais", pais);
		  model.addAttribute("isDisabled", true);
		  
		  return viewEdit;                        //"view/pais/ePais.html"
		 
		}catch(ErrorServiceException e) {	
		  model.addAttribute("msgError", e.getMessage());
		  return viewList;                       //"redirect:/pais/listPais"
		}		  
	}
	
	/* @GetMapping se utiliza para asignar solicitudes 
	 * HTTP GET a métodos específicos de un controlador.
	 */
	
	@GetMapping("/pais/modificar/{id}")
	public String modificar(@PathVariable("id") String idPais, Model model) {
		
		try {
			
		  Pais pais = paisService.buscarPais(idPais);		
		  model.addAttribute("pais", pais);
		  model.addAttribute("isDisabled", false);
		  
		  return viewEdit;                      //"view/pais/ePais.html"
		 
		}catch(ErrorServiceException e) {	
		  model.addAttribute("msgError", e.getMessage());
		  return viewList;                     //"redirect:/pais/listPais"
		}		  
	}
	
	/* @GetMapping se utiliza para asignar solicitudes 
	 * HTTP GET a métodos específicos de un controlador.
	 */
	
	@GetMapping("/pais/baja/{id}")
	public String baja(@PathVariable("id") String idPais, RedirectAttributes attributes, Model model) {	
		
		try {
			
		  paisService.eliminarPais(idPais);		
		  attributes.addFlashAttribute("msgExito", "La acción fue realizada correctamente.");
		  return redirectList;                                         //"redirect:/pais/listPais"
		  
		}catch(ErrorServiceException e) {	
		   model.addAttribute("msgError", e.getMessage());
		   return redirectList;                                        //"redirect:/pais/listPais"
		} 
	}
	
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	///////////////// VIEW: ePais ///////////////
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	
	/* @PostMapping es una anotación específica de Spring utilizada para mapear solicitudes HTTP POST 
	 * en métodos de un controlador. Esta anotación simplifica la configuración de rutas y es útil 
	 * para operaciones que implican la creación o el envío de datos desde el cliente al servidor. 
	 * A diferencia de @GetMapping, que se usa para recuperar información, @PostMapping suele utilizarse 
	 * cuando se envían datos para ser procesados, como por ejemplo el envío de formularios o la creación de registros en bases de datos, 
	 */
	
	@PostMapping("/pais/aceptarEditPais")
	public String aceptarEdit(Pais pais, BindingResult result, RedirectAttributes attributes, Model model){
		
		try {
			
		  if (result.hasErrors()){		
			model.addAttribute("msgError", "Error de Sistema");
			return viewEdit;       //"view/pais/ePais.html"
		  }
		 
		  if (pais.getId() == null || pais.getId().trim().isEmpty())
		   paisService.crearPais(pais.getNombre());
		  else 
		   paisService.modificarPais(pais.getId(), pais.getNombre());
			  
		  attributes.addFlashAttribute("msgExito", "La acción fue realizada correctamente.");
		  return redirectList;      //"redirect:/pais/listPais"
		  
		}catch(ErrorServiceException e) {	
			  model.addAttribute("msgError", e.getMessage());
			  return viewEdit; //"view/pais/ePais.html"
		}catch(Exception e) {
			  model.addAttribute("msgError", "Error de Sistema");
			  return viewEdit; //"view/pais/ePais.html"
		}
		
	}
	
	/* @GetMapping se utiliza para asignar solicitudes 
	 * HTTP GET a métodos específicos de un controlador.
	 */
	
	@GetMapping("/pais/cancelarEditPais")
	public String cancelarEdit() {
		return redirectList;                       //"redirect:/pais/listPais"
	}
	

}
