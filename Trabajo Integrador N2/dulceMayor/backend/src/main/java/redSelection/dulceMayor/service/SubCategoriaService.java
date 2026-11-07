package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import redSelection.dulceMayor.dto.SubCategoriaCrearDTO;
import redSelection.dulceMayor.dto.SubCategoriaRespuestaDTO;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.SubCategoria;
import redSelection.dulceMayor.repository.ProductoRepository;
import redSelection.dulceMayor.repository.SubCategoriaRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubCategoriaService extends BaseService<SubCategoria, SubCategoriaCrearDTO, SubCategoriaRespuestaDTO> {

    private final SubCategoriaRepository subCategoriaRepository;
    private final CategoriaService categoriaService;
    private final ProductoService productoService;

    /** @Lazy: ProductoService también depende de este service; se resuelve recién al usarlo (rompe el ciclo). */
    public SubCategoriaService(SubCategoriaRepository subCategoriaRepository,
                               CategoriaService categoriaService,
                               @Lazy ProductoService productoService) {
        super(subCategoriaRepository);
        this.subCategoriaRepository = subCategoriaRepository;
        this.categoriaService = categoriaService;
        this.productoService = productoService;
    }

    @Override protected String nombreRecurso() { return "Subcategoría"; }

    @Override
    protected SubCategoria aEntidad(SubCategoriaCrearDTO dto) {
        return SubCategoria.builder().nombre(dto.nombre().trim())
                .categoria(categoriaService.obtenerEntidad(dto.categoriaId())).build();
    }

    @Override
    protected void aplicarCambios(SubCategoria sub, SubCategoriaCrearDTO dto) {
        sub.setNombre(dto.nombre().trim());
        sub.setCategoria(categoriaService.obtenerEntidad(dto.categoriaId()));
    }

    @Override
    protected SubCategoriaRespuestaDTO aRespuesta(SubCategoria s) {
        return new SubCategoriaRespuestaDTO(s.getId(), s.getNombre(), s.getCategoria().getId(), s.getCategoria().getNombre());
    }

    @Override
    protected void validarCreacion(SubCategoriaCrearDTO dto) {
        subCategoriaRepository.findByNombreIgnoreCaseAndCategoriaId(dto.nombre().trim(), dto.categoriaId())
                .ifPresent(s -> { throw new ReglaNegocioException("Esa categoría ya tiene la subcategoría '" + dto.nombre().trim() + "'."); });
    }

    /** Usa ProductoRepository (no ProductoService) porque ProductoService depende de este service: evita el ciclo. */
    @Override
    protected void validarBaja(SubCategoria sub) {
        if (productoService.existenPorSubCategoria(sub.getId())) {
            throw new ReglaNegocioException("La subcategoría tiene productos activos.");
        }
    }

    /** Lo consulta CategoriaService antes de dar de baja una categoría. */
    @Transactional(readOnly = true)
    public boolean existenPorCategoria(String categoriaId) {
        return !subCategoriaRepository.findByCategoriaIdAndEliminadoFalse(categoriaId).isEmpty();
    }
}