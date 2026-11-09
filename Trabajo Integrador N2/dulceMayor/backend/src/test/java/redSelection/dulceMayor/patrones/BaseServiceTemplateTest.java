package redSelection.dulceMayor.patrones;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import redSelection.dulceMayor.dto.CategoriaCrearDTO;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.Categoria;
import redSelection.dulceMayor.repository.BaseRepository;
import redSelection.dulceMayor.repository.CategoriaRepository;
import redSelection.dulceMayor.service.BaseService;
import redSelection.dulceMayor.service.CategoriaService;
import redSelection.dulceMayor.service.SubCategoriaService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * BK-14: patrón Template Method en BaseService. El esqueleto (crear, eliminar...) siempre corre los pasos en el
 * mismo orden; las subclases solo completan los pasos. Un duplicado se rechaza en el hook de validación, ANTES de
 * armar la entidad y de guardar nada.
 */
class BaseServiceTemplateTest {

    /** Subclase mínima que anota cada paso que el esqueleto le pide. "repetida" ya existe. */
    static final class ServicioDePrueba extends BaseService<Categoria, String, String> {
        final List<String> pasos = new ArrayList<>();
        boolean bajaPermitida = true;

        ServicioDePrueba(BaseRepository<Categoria> repositorio) {
            super(repositorio);
        }

        @Override protected String nombreRecurso() { return "Categoría"; }
        @Override protected void validarCreacion(String nombre) {
            pasos.add("validarCreacion");
            if (nombre.equalsIgnoreCase("repetida")) throw new ReglaNegocioException("Ya existe la categoría 'repetida'.");
        }
        @Override protected Categoria aEntidad(String nombre) { pasos.add("aEntidad"); return Categoria.builder().nombre(nombre).build(); }
        @Override protected void antesDeGuardar(Categoria c) { pasos.add("antesDeGuardar"); }
        @Override protected void despuesDeGuardar(Categoria c) { pasos.add("despuesDeGuardar"); }
        @Override protected void despuesDeCrear(Categoria c, String nombre) { pasos.add("despuesDeCrear"); }
        @Override protected String aRespuesta(Categoria c) { pasos.add("aRespuesta"); return c.getNombre(); }
        @Override protected void aplicarCambios(Categoria c, String nombre) { c.setNombre(nombre); }
        @Override protected void validarBaja(Categoria c) {
            if (!bajaPermitida) throw new ReglaNegocioException("La categoría tiene subcategorías activas.");
        }
    }

    private BaseRepository<Categoria> repositorio;
    private ServicioDePrueba servicio;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void preparar() {
        repositorio = mock(BaseRepository.class);
        when(repositorio.save(any())).thenAnswer(inv -> {
            servicio.pasos.add("save");
            return inv.getArgument(0);
        });
        servicio = new ServicioDePrueba(repositorio);
    }

    @Test
    @DisplayName("crear corre los pasos del esqueleto siempre en el mismo orden")
    void ordenDelEsqueleto() {
        assertEquals("Alfajores", servicio.crear("Alfajores"));
        assertEquals(List.of("validarCreacion", "aEntidad", "antesDeGuardar", "save", "despuesDeGuardar",
                "despuesDeCrear", "aRespuesta"), servicio.pasos);
    }

    @Test
    @DisplayName("Un duplicado se rechaza en la validación: no se arma la entidad ni se guarda nada")
    void duplicadoSeRechazaAntesDeGuardar() {
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> servicio.crear("Repetida"));

        assertEquals("Ya existe la categoría 'repetida'.", e.getMessage());
        assertEquals(List.of("validarCreacion"), servicio.pasos);
        verify(repositorio, never()).save(any());
    }

    @Test
    @DisplayName("eliminar es una baja lógica: la fila queda marcada como eliminada (se conserva la trazabilidad)")
    void bajaLogica() {
        Categoria c = Categoria.builder().nombre("Snacks").build();
        when(repositorio.findByIdAndEliminadoFalse("cat-1")).thenReturn(Optional.of(c));

        servicio.eliminar("cat-1");

        assertTrue(c.isEliminado());
        verify(repositorio).save(c);
        verify(repositorio, never()).delete(any());
    }

    @Test
    @DisplayName("El hook validarBaja puede impedir la baja")
    void bajaImpedida() {
        when(repositorio.findByIdAndEliminadoFalse("cat-1")).thenReturn(Optional.of(Categoria.builder().build()));
        servicio.bajaPermitida = false;

        assertThrows(ReglaNegocioException.class, () -> servicio.eliminar("cat-1"));
        verify(repositorio, never()).save(any());
    }

    @Test
    @DisplayName("Pedir algo que no existe (o que fue dado de baja) responde 'no encontrado'")
    void inexistente() {
        RecursoNoEncontradoException e = assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener("x"));
        assertEquals("Categoría no encontrado: x", e.getMessage());
    }

    @Test
    @DisplayName("Caso real: CategoriaService rechaza un nombre repetido sin importar mayúsculas")
    void duplicadoEnCategoriaService() {
        CategoriaRepository categorias = mock(CategoriaRepository.class);
        when(categorias.findByNombreIgnoreCase("alfajores")).thenReturn(Optional.of(Categoria.builder().nombre("Alfajores").build()));
        CategoriaService real = new CategoriaService(categorias, mock(SubCategoriaService.class));

        assertThrows(ReglaNegocioException.class, () -> real.crear(new CategoriaCrearDTO(" alfajores ")));
        verify(categorias, never()).save(any());
    }
}
