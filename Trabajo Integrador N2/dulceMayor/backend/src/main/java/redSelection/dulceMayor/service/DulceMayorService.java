// DulceMayorService.java
package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.enums.TipoEmpresa;
import redSelection.dulceMayor.model.DulceMayor;
import redSelection.dulceMayor.repository.DulceMayorRepository;

import java.util.Optional;

/** Acceso a la empresa (casa central y sucursales) para el resto de los services. */
@Service
public class DulceMayorService {

    private final DulceMayorRepository dulceMayorRepository;

    public DulceMayorService(DulceMayorRepository dulceMayorRepository) {
        this.dulceMayorRepository = dulceMayorRepository;
    }

    /** La casa central: el depósito desde donde sale el reparto. */
    @Transactional(readOnly = true)
    public Optional<DulceMayor> sedeCentral() {
        return dulceMayorRepository.findByEliminadoFalse().stream()
                .filter(d -> d.getTipoEmpresa() == TipoEmpresa.SEDE_CENTRAL)
                .findFirst();
    }
}