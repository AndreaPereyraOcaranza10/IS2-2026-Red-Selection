package redselection.servidor.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redselection.servidor.dto.ConsultaRequest;
import redselection.servidor.model.DetalleHistoriaClinica;
import redselection.servidor.model.HistoriaClinica;
import redselection.servidor.model.Medico;
import redselection.servidor.services.HistoriaClinicaService;
import redselection.servidor.services.MedicoService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Patrón Facade: interfaz simple para las operaciones de "alto nivel" sobre una historia clínica
 * (agregar o quitar una consulta). Por dentro coordina varios servicios del subsistema
 * (HistoriaClinicaService y MedicoService) y las reglas de composición de la historia,
 * de modo que los clientes no tengan que conocer ese detalle.
 */
@Service
public class HistoriaClinicaFacade {

    private final HistoriaClinicaService historias;
    private final MedicoService medicos;

    public HistoriaClinicaFacade(HistoriaClinicaService historias, MedicoService medicos) {
        this.historias = historias;
        this.medicos = medicos;
    }

    @Transactional
    public HistoriaClinica agregarConsulta(String historiaId, ConsultaRequest pedido) {
        HistoriaClinica existente = historias.findById(historiaId);

        // Si no viene médico se deja en null: lo rechaza la cadena de validación (400).
        // Si viene un id que no existe, findById lanza NoSuchElementException (404).
        Medico medico = pedido.medicoId() == null ? null : medicos.findById(pedido.medicoId());

        List<DetalleHistoriaClinica> detalles = copiarDetalles(existente.getDetalles());
        detalles.add(nuevoDetalle(pedido, medico));

        return historias.update(historiaId, copiaConDetalles(existente, detalles));
    }

    @Transactional
    public HistoriaClinica quitarConsulta(String historiaId, String detalleId) {
        HistoriaClinica existente = historias.findById(historiaId);

        List<DetalleHistoriaClinica> restantes = existente.getDetalles().stream()
                .filter(d -> !detalleId.equals(d.getId()))
                .toList();
        if (restantes.size() == existente.getDetalles().size()) {
            throw new NoSuchElementException("No existe la consulta con id " + detalleId);
        }

        return historias.update(historiaId, copiaConDetalles(existente, copiarDetalles(restantes)));
    }

    // ---------- Ayudas privadas ----------

    // Se arma una historia nueva (no se modifica la que está en la base) porque
    // el servicio de historias vuelve a vincular y a regenerar los detalles al actualizar.
    private HistoriaClinica copiaConDetalles(HistoriaClinica original, List<DetalleHistoriaClinica> detalles) {
        HistoriaClinica copia = new HistoriaClinica();
        copia.setPaciente(original.getPaciente());
        copia.setUsuario(original.getUsuario());
        copia.setDetalles(detalles);
        return copia;
    }

    private List<DetalleHistoriaClinica> copiarDetalles(List<DetalleHistoriaClinica> origen) {
        List<DetalleHistoriaClinica> copias = new ArrayList<>();
        for (DetalleHistoriaClinica d : origen) {
            DetalleHistoriaClinica c = new DetalleHistoriaClinica();
            c.setFechaHistoria(d.getFechaHistoria());
            c.setDetalleHistoria(d.getDetalleHistoria());
            c.setMedico(d.getMedico());
            copias.add(c);
        }
        return copias;
    }

    private DetalleHistoriaClinica nuevoDetalle(ConsultaRequest pedido, Medico medico) {
        DetalleHistoriaClinica d = new DetalleHistoriaClinica();
        d.setFechaHistoria(pedido.fechaHistoria());
        d.setDetalleHistoria(pedido.detalleHistoria());
        d.setMedico(medico);
        return d;
    }
}