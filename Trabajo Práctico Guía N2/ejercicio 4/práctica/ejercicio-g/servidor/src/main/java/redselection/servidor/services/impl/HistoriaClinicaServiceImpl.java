package redselection.servidor.services.impl;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import redselection.servidor.model.DetalleHistoriaClinica;
import redselection.servidor.model.HistoriaClinica;
import redselection.servidor.patrones.builder.HistoriaClinicaBuilderImpl;
import redselection.servidor.patrones.builder.HistoriaClinicaDirector;
import redselection.servidor.repositories.HistoriaClinicaRepository;
import redselection.servidor.services.HistoriaClinicaService;
import redselection.servidor.validaciones.ValidaFechaDetalles;
import redselection.servidor.validaciones.ValidaMedicoDetalles;
import redselection.servidor.validaciones.ValidaPaciente;
import redselection.servidor.validaciones.ValidaUsuario;
import redselection.servidor.validaciones.ValidadorHistoria;

import java.util.List;

@Service
public class HistoriaClinicaServiceImpl extends BaseServiceImpl<HistoriaClinica> implements HistoriaClinicaService {

    private final HistoriaClinicaRepository historiaRepository;

    // Primer eslabón de la cadena de validación (patrón Chain of Responsibility)
    private final ValidadorHistoria validador;

    public HistoriaClinicaServiceImpl(HistoriaClinicaRepository repository) {
        super(repository);
        this.historiaRepository = repository;

        ValidadorHistoria primero = new ValidaPaciente();
        primero.setSiguiente(new ValidaUsuario())
                .setSiguiente(new ValidaFechaDetalles())
                .setSiguiente(new ValidaMedicoDetalles());
        this.validador = primero;
    }

    /**
     * La entidad recibida desde el controlador se reconstruye en el servidor con Builder
     * antes de ejecutar el flujo estándar de validación y persistencia.
     */
    @Override
    @Transactional
    public HistoriaClinica save(HistoriaClinica historia) {
        return super.save(construirConBuilder(historia));
    }

    @Override
    @Transactional
    public HistoriaClinica update(String id, HistoriaClinica historia) {
        return super.update(id, construirConBuilder(historia));
    }

    private HistoriaClinica construirConBuilder(HistoriaClinica origen) {
        HistoriaClinicaBuilderImpl builder = new HistoriaClinicaBuilderImpl();
        HistoriaClinicaDirector director = new HistoriaClinicaDirector(builder);
        return director.construirDesde(origen);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriaClinica> findByPaciente(String pacienteId) {
        return historiaRepository.findByPacienteIdAndEliminadoFalse(pacienteId);
    }

    // Paso abstracto del Template Method (BaseServiceImpl): acá se delega en la cadena.
    @Override
    protected void validar(HistoriaClinica h) {
        validador.validar(h);
    }

    // Hook: como en DetalleHistoriaClinica la referencia a la historia tiene @JsonIgnore,
    // al llegar el JSON viene en null. Hay que reasignarla a mano antes de guardar.
    @Override
    protected void antesDeGuardar(HistoriaClinica h) {
        vincularDetalles(h);
    }

    @Override
    protected void antesDeActualizar(HistoriaClinica h) {
        vincularDetalles(h);
    }

    private void vincularDetalles(HistoriaClinica h) {
        for (DetalleHistoriaClinica d : h.getDetalles()) {
            d.setId(null);
            d.setEliminado(false);
            d.setHistoriaClinica(h);
        }
    }
}