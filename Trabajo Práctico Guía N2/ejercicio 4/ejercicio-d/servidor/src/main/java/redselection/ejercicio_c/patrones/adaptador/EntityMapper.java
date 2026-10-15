package redselection.ejercicio_c.patrones.adaptador;

import redselection.ejercicio_c.entities.Base;

import java.util.List;

// ADAPTADOR (interfaz objetivo): adapta una Entidad (lo que entiende la base de datos / el servicio)
// a un DTO (lo que viaja por la API) y viceversa. MapStruct genera la implementación de cada mapper.
public interface EntityMapper<E extends Base, D> {

    D toDto(E entidad);

    E toEntity(D dto);

    List<D> toDtoList(List<E> entidades);

    List<E> toEntityList(List<D> dtos);
}