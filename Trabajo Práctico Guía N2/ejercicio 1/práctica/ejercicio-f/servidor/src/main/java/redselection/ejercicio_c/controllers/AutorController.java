package redselection.ejercicio_c.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redselection.ejercicio_c.dto.AutorDTO;
import redselection.ejercicio_c.services.AutorServiceImpl;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/api/v1/autores")
public class AutorController extends BaseControllerImpl<AutorDTO, AutorServiceImpl> {
}

