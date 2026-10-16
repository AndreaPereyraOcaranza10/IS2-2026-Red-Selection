package redselection.servidor.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redselection.servidor.model.Medico;
import redselection.servidor.services.MedicoService;

@RestController
@RequestMapping("/api/medicos")
public class MedicoController extends BaseControllerImpl<Medico, MedicoService> {
    public MedicoController(MedicoService servicio) {
        super(servicio);
    }
}