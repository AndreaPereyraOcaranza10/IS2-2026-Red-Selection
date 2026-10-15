package redselection.servidor.services.impl;

import org.springframework.transaction.annotation.Transactional;
import redselection.servidor.model.Base;
import redselection.servidor.repositories.BaseRepository;
import redselection.servidor.services.BaseService;

import java.util.List;
import java.util.NoSuchElementException;

public abstract class BaseServiceImpl<E extends Base> implements BaseService<E> {

    protected final BaseRepository<E, String> repository;

    protected BaseServiceImpl(BaseRepository<E, String> repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<E> findAll() {
        return repository.findAll().stream()
                .filter(e -> !e.isEliminado())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public E findById(String id) {
        return repository.findById(id)
                .filter(e -> !e.isEliminado())
                .orElseThrow(() -> new NoSuchElementException("No existe el registro con id " + id));
    }


    @Override
    @Transactional
    public E save(E entity) {
        entity.setId(null);
        entity.setEliminado(false);
        validar(entity);
        antesDeGuardar(entity);
        E guardado = repository.save(entity);
        despuesDeGuardar(guardado);
        return guardado;
    }


    @Override
    @Transactional
    public E update(String id, E entity) {
        findById(id);
        entity.setId(id);
        entity.setEliminado(false);
        validar(entity);
        antesDeActualizar(entity);
        E actualizado = repository.save(entity);
        despuesDeActualizar(actualizado);
        return actualizado;
    }


    @Override
    @Transactional
    public void delete(String id) {
        E entity = findById(id);
        antesDeEliminar(entity);
        entity.setEliminado(true);
        repository.save(entity);
    }


    protected abstract void validar(E entity);

    protected void antesDeGuardar(E entity) { }
    protected void despuesDeGuardar(E entity) { }
    protected void antesDeActualizar(E entity) { }
    protected void despuesDeActualizar(E entity) { }
    protected void antesDeEliminar(E entity) { }
}