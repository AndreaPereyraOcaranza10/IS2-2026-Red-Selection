package com.tienda.zero.config;

import com.tienda.zero.model.Categoria;
import com.tienda.zero.repository.CategoriaRepository;
import com.tienda.zero.repository.SubCategoriaRepository;
import com.tienda.zero.service.CategoriaService;
import com.tienda.zero.service.SubCategoriaService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * Carga las categorías y subcategorías base de la tienda según la especificación:
 * "Niños", "Niñas", "Mujeres" y "Hombres", cada una subdividida en
 * "Ropa", "Calzado" y "Accesorios".
 * Es idempotente: solo crea lo que todavía no existe.
 */
@Component
@Order(1)
public class CatalogoInicial implements CommandLineRunner {

    private static final List<String> CATEGORIAS = List.of("Niños", "Niñas", "Mujeres", "Hombres");
    private static final List<String> SUBCATEGORIAS = List.of("Ropa", "Calzado", "Accesorios");

    private final CategoriaService categoriaService;
    private final SubCategoriaService subCategoriaService;
    private final CategoriaRepository categoriaRepository;
    private final SubCategoriaRepository subCategoriaRepository;

    public CatalogoInicial(CategoriaService categoriaService, SubCategoriaService subCategoriaService,
                           CategoriaRepository categoriaRepository, SubCategoriaRepository subCategoriaRepository) {
        this.categoriaService = categoriaService;
        this.subCategoriaService = subCategoriaService;
        this.categoriaRepository = categoriaRepository;
        this.subCategoriaRepository = subCategoriaRepository;
    }

    @Override
    public void run(String... args) {
        for (String nombreCategoria : CATEGORIAS) {
            Categoria categoria = categoriaRepository.findByNombreIgnoreCase(nombreCategoria)
                    .orElseGet(() -> categoriaService.crearCategoria(nombreCategoria));

            for (String nombreSubCategoria : SUBCATEGORIAS) {
                boolean existe = subCategoriaRepository.findByCategoriaId(categoria.getId()).stream()
                        .anyMatch(s -> s.getNombre().equalsIgnoreCase(nombreSubCategoria));
                if (!existe) {
                    subCategoriaService.crearSubCategoria(nombreSubCategoria, categoria.getId());
                }
            }
        }
    }
}
