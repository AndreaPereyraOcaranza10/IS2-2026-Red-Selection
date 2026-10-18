package redselection.servidor.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redselection.servidor.dto.ConsultaRequest;
import redselection.servidor.facade.HistoriaClinicaFacade;
import redselection.servidor.model.HistoriaClinica;
import redselection.servidor.services.HistoriaClinicaService;

import java.util.List;

@RestController
@RequestMapping("/api/historias-clinicas")
public class HistoriaClinicaController extends BaseControllerImpl<HistoriaClinica, HistoriaClinicaService> {

    private final HistoriaClinicaFacade facade;

    public HistoriaClinicaController(HistoriaClinicaService servicio, HistoriaClinicaFacade facade) {
        super(servicio);
        this.facade = facade;
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<HistoriaClinica>> getPorPaciente(@PathVariable String pacienteId) {
        return ResponseEntity.ok(servicio.findByPaciente(pacienteId));
    }

    // Operaciones de alto nivel resueltas por la fachada
    @PostMapping("/{id}/detalles")
    public ResponseEntity<HistoriaClinica> agregarConsulta(@PathVariable String id,
                                                           @RequestBody ConsultaRequest pedido) {
        return ResponseEntity.ok(facade.agregarConsulta(id, pedido));
    }

    @DeleteMapping("/{id}/detalles/{detalleId}")
    public ResponseEntity<HistoriaClinica> quitarConsulta(@PathVariable String id,
                                                          @PathVariable String detalleId) {
        return ResponseEntity.ok(facade.quitarConsulta(id, detalleId));
    }
}