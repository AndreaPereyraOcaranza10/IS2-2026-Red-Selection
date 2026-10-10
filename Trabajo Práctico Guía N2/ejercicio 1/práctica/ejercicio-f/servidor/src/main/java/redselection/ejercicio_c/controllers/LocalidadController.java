package redselection.ejercicio_c.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redselection.ejercicio_c.dto.LocalidadDTO;
import redselection.ejercicio_c.services.LocalidadServiceImpl;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/api/v1/localidades")
public class LocalidadController extends BaseControllerImpl<LocalidadDTO, LocalidadServiceImpl> {
}

