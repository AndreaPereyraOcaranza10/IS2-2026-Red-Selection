package redSelection.dulceMayor.service;

import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.model.Base;
import redSelection.dulceMayor.repository.BaseRepository;


import java.util.List;

/**
 * PATRÓN: Template Method (capa Service).
 * crear / obtener / listar / actualizar / eliminar son el ESQUELETO del algoritmo.
 * Cada servicio concreto completa los pasos abstractos y, si lo necesita, los hooks.
 * No son final a propósito: un método final no puede ser proxiado y @Transactional no se aplicaría.
 */
public abstract class BaseService<E extends Base, REQ, RES> {

    private final BaseRepository<E> repository;

    protected BaseService(BaseRepository<E> repository) {
        this.repository = repository;
    }

    // ---------- Esqueleto (template methods) ----------

    @Transactional
    public RES crear(REQ solicitud) {
        validarCreacion(solicitud);               // hook
        E entidad = aEntidad(solicitud);          // paso abstracto
        antesDeGuardar(entidad);                  // hook
        E guardada = repository.save(entidad);
        despuesDeGuardar(guardada);               // hook
        despuesDeCrear(guardada, solicitud);      // hook (necesita datos del request, ej. precio o stock inicial)
        return aRespuesta(guardada);              // paso abstracto           // paso abstracto
    }

    @Transactional(readOnly = true)
    public RES obtener(String id) {
        return aRespuesta(buscarEntidad(id));
    }

    @Transactional(readOnly = true)
    public List<RES> listar() {
        return buscarTodos().stream().map(this::aRespuesta).toList();
    }

    @Transactional
    public RES actualizar(String id, REQ solicitud) {
        E entidad = buscarEntidad(id);
        validarActualizacion(entidad, solicitud); // hook
        aplicarCambios(entidad, solicitud);       // paso abstracto
        antesDeGuardar(entidad);                  // hook
        E guardada = repository.save(entidad);
        despuesDeGuardar(guardada);               // hook
        return aRespuesta(guardada);
    }

    @Transactional
    public void eliminar(String id) {
        E entidad = buscarEntidad(id);
        validarBaja(entidad);                     // hook
        ejecutarBaja(entidad);                    // hook (baja lógica por defecto)
    }

    // ---------- Pasos obligatorios ----------

    protected abstract String nombreRecurso();
    protected abstract E aEntidad(REQ solicitud);
    protected abstract void aplicarCambios(E entidad, REQ solicitud);
    protected abstract RES aRespuesta(E entidad);

    // ---------- Hooks opcionales ----------

    protected void validarCreacion(REQ solicitud) { }
    protected void validarActualizacion(E entidad, REQ solicitud) { }
    protected void validarBaja(E entidad) { }
    protected void antesDeGuardar(E entidad) { }
    protected void despuesDeGuardar(E entidad) { }

    /** Baja lógica: la fila queda en la base para conservar la trazabilidad. */
    protected void ejecutarBaja(E entidad) {
        entidad.setEliminado(true);
        repository.save(entidad);
    }

    protected List<E> buscarTodos() {
        return repository.findByEliminadoFalse();
    }

    // ---------- Utilidad para las subclases ----------

    protected E buscarEntidad(String id) {
        return repository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(nombreRecurso(), id));
    }

    protected void despuesDeCrear(E entidad, REQ solicitud) { }

    @Transactional(readOnly = true)
    public E obtenerEntidad(String id) {
        return buscarEntidad(id);
    }

}