package com.tienda.zero.config;

import com.tienda.zero.enums.TipoImagen;
import com.tienda.zero.model.Categoria;
import com.tienda.zero.model.Imagen;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.SubCategoria;
import com.tienda.zero.repository.CategoriaRepository;
import com.tienda.zero.repository.ProductoRepository;
import com.tienda.zero.repository.SubCategoriaRepository;
import com.tienda.zero.service.ImagenService;
import com.tienda.zero.service.ProductoService;
import com.tienda.zero.service.VigenciaPrecioService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final SubCategoriaRepository subCategoriaRepository;
    private final ProductoRepository productoRepository;
    private final ProductoService productoService;
    private final VigenciaPrecioService vigenciaPrecioService;
    private final ImagenService imagenService;

    @Override
    public void run(String... args) {
        System.out.println(">>> [DataInitializer] Iniciando verificación de datos iniciales...");
        inicializarCategorias();
        inicializarProductos();
        System.out.println(">>> [DataInitializer] Proceso de inicialización finalizado.");
    }

    private void inicializarCategorias() {
        if (categoriaRepository.count() == 0 || subCategoriaRepository.count() == 0) {
            List<String> categoriasBase = List.of("Hombres", "Mujeres", "Niños", "Niñas");
            List<String> subcategoriasBase = List.of("Ropa", "Calzado", "Accesorios");

            for (String nombreCat : categoriasBase) {
                Categoria cat = categoriaRepository.findByNombreIgnoreCase(nombreCat)
                        .orElseGet(() -> categoriaRepository.save(
                                Categoria.builder()
                                        .nombre(nombreCat)
                                        .eliminado(false)
                                        .build()
                        ));

                for (String nombreSub : subcategoriasBase) {
                    boolean yaExiste = subCategoriaRepository.findByCategoriaId(cat.getId()).stream()
                            .anyMatch(s -> s.getNombre().equalsIgnoreCase(nombreSub));

                    if (!yaExiste) {
                        SubCategoria sub = SubCategoria.builder()
                                .nombre(nombreSub)
                                .categoria(cat)
                                .eliminado(false)
                                .build();
                        subCategoriaRepository.save(sub);
                    }
                }
            }
            System.out.println(">>> [DataInitializer] Categorías y Subcategorías iniciales aseguradas en MySQL.");
        }
    }

    private void inicializarProductos() {
        record ProductoSeed(
                String nombre,
                String sku,
                String catNombre,
                String subCatNombre,
                String talle,
                boolean enOferta,
                double precio,
                String imagenArchivo,
                String descripcion
        ) {}

        List<ProductoSeed> seeds = List.of(
                new ProductoSeed(
                        "Remera Deportiva Training Zero", "REM-010", "Hombres", "Ropa",
                        "L", false, 18500.0, "1.jpg",
                        "Remera confeccionada con tejido transpirable Dry-Fit, ideal para entrenamientos de alta intensidad y running."
                ),
                new ProductoSeed(
                        "Calzas Deportivas High Waist", "CAL-002", "Mujeres", "Ropa",
                        "M", true, 24900.0, "2.jpg",
                        "Calza deportiva de compresión y cintura alta para máxima sujeción y comodidad en yoga y gimnasio."
                ),
                new ProductoSeed(
                        "Zapatillas Running Zero Speed", "ZAP-003", "Hombres", "Calzado",
                        "42", false, 65000.0, "3.jpg",
                        "Zapatillas con suela de amortiguación reactiva y capellada de malla transpirable para corredores exigentes."
                ),
                new ProductoSeed(
                        "Top Deportivo Pro Impact", "TOP-004", "Mujeres", "Ropa",
                        "S", false, 16200.0, "4.jpg",
                        "Sujetador deportivo con soporte óptimo, tejido elástico y espalda cruzada ergonómica."
                ),
                new ProductoSeed(
                        "Mochila Urbana Deportiva 25L", "ACC-005", "Hombres", "Accesorios",
                        "Único", true, 32000.0, "5.jpg",
                        "Mochila deportiva impermeable con compartimento acolchado para notebook y bolsillos térmicos para botellas."
                ),
                new ProductoSeed(
                        "Conjunto Deportivo Kids Active", "CON-006", "Niños", "Ropa",
                        "M", false, 28500.0, "6.jpg",
                        "Conjunto de campera y pantalón deportivo infantil, confeccionado en algodón frizado de alta resistencia."
                ),
                new ProductoSeed(
                        "Zapatillas Training Air Fit", "ZAP-007", "Mujeres", "Calzado",
                        "38", true, 58000.0, "7.jpg",
                        "Calzado deportivo ultraliviano diseñado especialmente para entrenamientos funcionales, cross-training y caminatas."
                ),
                new ProductoSeed(
                        "Gorra Deportiva UV Protect", "GOR-008", "Mujeres", "Accesorios",
                        "Único", false, 12500.0, "8.jpg",
                        "Gorra deportiva con visera curva, microperforaciones láser para ventilación y protección contra rayos UV."
                )
        );

        System.out.println(">>> [DataInitializer] Verificando productos a inicializar...");

        for (ProductoSeed seed : seeds) {
            try {
                if (productoRepository.findByCodigoIgnoreCase(seed.sku()).isPresent()) {
                    System.out.println(">>> [DataInitializer] Producto " + seed.sku() + " ya existe en la BD. Omitiendo.");
                    continue;
                }

                String idImagen = null;
                try {
                    ClassPathResource resource = new ClassPathResource("static/tienda/images/products/" + seed.imagenArchivo());
                    if (resource.exists()) {
                        byte[] bytes = resource.getInputStream().readAllBytes();
                        Imagen img = imagenService.crearImagen(seed.imagenArchivo(), "image/jpeg", bytes, TipoImagen.PRODUCTO);
                        idImagen = img.getId();
                    } else {
                        System.err.println(">>> [DataInitializer] Imagen no encontrada: " + seed.imagenArchivo());
                    }
                } catch (Exception e) {
                    System.err.println(">>> [DataInitializer] Error al procesar imagen " + seed.imagenArchivo() + ": " + e.getMessage());
                }

                SubCategoria subCat = subCategoriaRepository.findByEliminadoFalse().stream()
                        .filter(s -> s.getNombre().equalsIgnoreCase(seed.subCatNombre())
                                && s.getCategoria() != null
                                && s.getCategoria().getNombre().equalsIgnoreCase(seed.catNombre()))
                        .findFirst()
                        .orElse(null);

                if (subCat == null) {
                    List<SubCategoria> todas = subCategoriaRepository.findByEliminadoFalse();
                    if (!todas.isEmpty()) {
                        subCat = todas.get(0);
                    }
                }

                if (subCat == null) {
                    System.err.println(">>> [DataInitializer] No hay subcategorías en la BD para " + seed.sku());
                    continue;
                }

                Producto prod = productoService.crearProducto(
                        seed.sku(),
                        seed.nombre(),
                        seed.descripcion(),
                        seed.talle(),
                        seed.enOferta(),
                        idImagen,
                        subCat.getId()
                );

                vigenciaPrecioService.crearVigenciaPrecio(
                        new Date(System.currentTimeMillis()),
                        null,
                        seed.precio(),
                        prod.getId()
                );

                System.out.println(">>> [DataInitializer] Creado producto: " + prod.getNombre() + " (" + seed.sku() + ") - Precio: $" + seed.precio());

            } catch (Exception e) {
                System.err.println(">>> [DataInitializer] Error al inicializar producto " + seed.sku() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println(">>> [DataInitializer] Verificación completada. Total de productos en BD: " + productoRepository.count());
    }
}

