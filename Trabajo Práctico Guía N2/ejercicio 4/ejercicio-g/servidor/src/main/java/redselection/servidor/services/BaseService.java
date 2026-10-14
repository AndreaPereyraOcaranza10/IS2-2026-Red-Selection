package redselection.servidor.services;

import redselection.servidor.model.Base;

import java.util.List;

public interface BaseService<E extends Base> {
    List<E> findAll();
    E findById(String id);
    E save(E entity);
    E update(String id, E entity);
    void delete(String id);
}
