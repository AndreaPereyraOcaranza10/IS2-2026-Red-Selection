package redselection.servidor.controllers;

import org.springframework.web.bind.annotation.*;
import redselection.servidor.model.Paciente;
import redselection.servidor.services.PacienteService;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController extends BaseControllerImpl<Paciente, PacienteService> {
    public PacienteController(PacienteService servicio) {
        super(servicio);
    }
}
