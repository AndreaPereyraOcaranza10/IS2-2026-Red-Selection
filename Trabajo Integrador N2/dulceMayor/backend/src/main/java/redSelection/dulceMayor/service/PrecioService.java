package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.Producto;
import redSelection.dulceMayor.model.VigenciaPrecio;
import redSelection.dulceMayor.repository.VigenciaPrecioRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PrecioService {

    private final VigenciaPrecioRepository vigenciaRepository;

    public PrecioService(VigenciaPrecioRepository vigenciaRepository) { this.vigenciaRepository = vigenciaRepository; }

    @Transactional(readOnly = true)
    public Optional<BigDecimal> precioVigente(String productoId, LocalDate fecha) {
        return vigenciaRepository.vigentesEn(productoId, fecha).stream().findFirst().map(VigenciaPrecio::getPrecio);
    }

    /** Para armar pedidos: un producto sin precio vigente no se puede vender. */
    @Transactional(readOnly = true)
    public BigDecimal precioVigenteObligatorio(Producto producto, LocalDate fecha) {
        return precioVigente(producto.getId(), fecha).orElseThrow(() ->
                new ReglaNegocioException("El producto '" + producto.getNombre() + "' no tiene precio vigente."));
    }

    /** Fija el precio desde una fecha: cierra la vigencia abierta anterior o corrige la de hoy. */
    @Transactional
    public void fijarPrecio(Producto producto, BigDecimal precio, LocalDate desde) {
        if (precio == null || precio.signum() <= 0) throw new ReglaNegocioException("El precio debe ser mayor a cero.");
        for (VigenciaPrecio abierta : vigenciaRepository
                .findByProductoIdAndEliminadoFalseOrderByFechaDesdeDesc(producto.getId())) {
            if (abierta.getFechaHasta() != null) continue;
            if (abierta.getFechaDesde().equals(desde)) {          // mismo día: se corrige el precio
                abierta.setPrecio(precio);
                vigenciaRepository.save(abierta);
                return;
            }
            if (abierta.getFechaDesde().isAfter(desde)) {
                throw new ReglaNegocioException("Ya existe un precio que arranca el " + abierta.getFechaDesde()
                        + "; la nueva vigencia debe ser posterior.");
            }
            abierta.setFechaHasta(desde.minusDays(1));
            vigenciaRepository.save(abierta);
        }
        vigenciaRepository.save(VigenciaPrecio.builder().producto(producto).precio(precio).fechaDesde(desde).build());
    }

    /** Para el newsletter: las vigencias de precio de todos los productos en una fecha (la más reciente primero). */
    @Transactional(readOnly = true)
    public List<VigenciaPrecio> vigentesEn(LocalDate fecha) {
        return vigenciaRepository.vigentesEn(fecha);
    }

}