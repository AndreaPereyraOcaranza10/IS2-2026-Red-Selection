package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import redSelection.dulceMayor.dto.CategoriaCrearDTO;
import redSelection.dulceMayor.dto.CategoriaRespuestaDTO;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.Categoria;
import redSelection.dulceMayor.repository.CategoriaRepository;
import org.springframework.context.annotation.Lazy;

@Service
public class CategoriaService extends BaseService<Categoria, CategoriaCrearDTO, CategoriaRespuestaDTO> {

    private final CategoriaRepository categoriaRepository;
    private final SubCategoriaService subCategoriaService;

    /** @Lazy: SubCategoriaService también depende de este service; se resuelve recién al usarlo (rompe el ciclo). */
    public CategoriaService(CategoriaRepository categoriaRepository,
                            @Lazy SubCategoriaService subCategoriaService) {
        super(categoriaRepository);
        this.categoriaRepository = categoriaRepository;
        this.subCategoriaService = subCategoriaService;
    }

    @Override protected String nombreRecurso() { return "Categoría"; }

    @Override protected Categoria aEntidad(CategoriaCrearDTO dto) { return Categoria.builder().nombre(dto.nombre().trim()).build(); }

    @Override protected void aplicarCambios(Categoria c, CategoriaCrearDTO dto) { c.setNombre(dto.nombre().trim()); }

    @Override protected CategoriaRespuestaDTO aRespuesta(Categoria c) { return new CategoriaRespuestaDTO(c.getId(), c.getNombre()); }

    @Override
    protected void validarCreacion(CategoriaCrearDTO dto) {
        categoriaRepository.findByNombreIgnoreCase(dto.nombre().trim()).ifPresent(c -> {
            throw new ReglaNegocioException("Ya existe la categoría '" + dto.nombre().trim() + "'.");
        });
    }

    @Override
    protected void validarActualizacion(Categoria actual, CategoriaCrearDTO dto) {
        categoriaRepository.findByNombreIgnoreCase(dto.nombre().trim())
                .filter(otra -> !otra.getId().equals(actual.getId()))
                .ifPresent(c -> { throw new ReglaNegocioException("Ya existe otra categoría '" + dto.nombre().trim() + "'."); });
    }

    /** Usa el repository de subcategorías (no su service) porque SubCategoriaService ya depende de este: evita un ciclo. */
    @Override
    protected void validarBaja(Categoria categoria) {
        if (subCategoriaService.existenPorCategoria(categoria.getId())) {
            throw new ReglaNegocioException("La categoría tiene subcategorías activas.");
        }
    }
}
