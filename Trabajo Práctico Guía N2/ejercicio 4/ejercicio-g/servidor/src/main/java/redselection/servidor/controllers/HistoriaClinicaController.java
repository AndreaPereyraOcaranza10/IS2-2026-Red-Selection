package redselection.servidor.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redselection.servidor.model.HistoriaClinica;
import redselection.servidor.services.HistoriaClinicaService;

import java.util.List;

@RestController
@RequestMapping("/api/historias-clinicas")
public class HistoriaClinicaController extends BaseControllerImpl<HistoriaClinica, HistoriaClinicaService> {

    public HistoriaClinicaController(HistoriaClinicaService servicio) {
        super(servicio);
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<HistoriaClinica>> getPorPaciente(@PathVariable String pacienteId) {
        return ResponseEntity.ok(servicio.findByPaciente(pacienteId));
    }
}