package redSelection.dulceMayor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import redSelection.dulceMayor.model.Base;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<E extends Base> extends JpaRepository<E, String> {
    List<E> findByEliminadoFalse();
    Optional<E> findByIdAndEliminadoFalse(String id);
}