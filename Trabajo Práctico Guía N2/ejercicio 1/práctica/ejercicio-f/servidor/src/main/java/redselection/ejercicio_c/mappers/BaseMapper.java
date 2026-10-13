package redselection.ejercicio_c.mappers;

import redselection.ejercicio_c.dto.BaseDTO;
import redselection.ejercicio_c.entities.Base;

import java.util.List;

public interface BaseMapper<E extends Base, D extends BaseDTO> {
    E toEntity(D dto);

    D toDTO(E entity);

    List<D> toDTOList(List<E> entities);
}
