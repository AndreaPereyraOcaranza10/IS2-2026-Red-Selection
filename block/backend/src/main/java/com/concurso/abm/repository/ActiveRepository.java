package com.concurso.abm.repository;

import com.concurso.abm.entity.BaseEntity;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ActiveRepository<E extends BaseEntity> extends JpaRepository<E, Long> {
  List<E> findByEliminadoFalseOrderByIdAsc();

  Optional<E> findByIdAndEliminadoFalse(Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from #{#entityName} e where e.id=:id and e.eliminado=false")
  Optional<E> lockActiveById(@org.springframework.data.repository.query.Param("id") Long id);
}
