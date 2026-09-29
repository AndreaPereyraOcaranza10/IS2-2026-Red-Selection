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
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/*
 * Carga productos de ejemplo para poder probar el catálogo, las ofertas y el panel de inventario.
 * Cada producto se crea con su imagen y con dos vigencias de precio: una anterior (ya cerrada)
 * y la vigente (fechaHasta nula), como describe la nota del diagrama de clases.
 * Es idempotente: si el código del producto ya existe, lo omite.
 * Requiere que CatalogoInicial (Order 1) haya creado antes las categorías.
 */
@Component
@Order(2)
public class ProductosDemo implements CommandLineRunner {

    private record ProductoEjemplo(String codigo, String nombre, String descripcion, String talle,
                                   boolean enOferta, double precio, String categoria,
                                   String subCategoria, String archivoImagen) {
    }

    private static final List<ProductoEjemplo> PRODUCTOS = List.of(
            new ProductoEjemplo("REM-010", "Remera Deportiva Training Zero",
                    "Remera de tejido transpirable Dry-Fit, ideal para entrenamientos de alta intensidad y running.",
                    "L", false, 18500.0, "Hombres", "Ropa", "1.jpg"),
            new ProductoEjemplo("CAL-002", "Calzas Deportivas High Waist",
                    "Calza de compresión y cintura alta para máxima sujeción y comodidad en yoga y gimnasio.",
                    "M", true, 24900.0, "Mujeres", "Ropa", "2.jpg"),
            new ProductoEjemplo("ZAP-003", "Zapatillas Running Zero Speed",
                    "Suela de amortiguación reactiva y capellada de malla transpirable para corredores exigentes.",
                    "42", false, 65000.0, "Hombres", "Calzado", "3.jpg"),
            new ProductoEjemplo("TOP-004", "Top Deportivo Pro Impact",
                    "Top deportivo con soporte óptimo, tejido elástico y espalda cruzada ergonómica.",
                    "S", false, 16200.0, "Mujeres", "Ropa", "4.jpg"),
            new ProductoEjemplo("ACC-005", "Mochila Urbana Deportiva 25L",
                    "Mochila impermeable con compartimento acolchado para notebook y bolsillos para botellas.",
                    "Único", true, 32000.0, "Hombres", "Accesorios", "5.jpg"),
            new ProductoEjemplo("CON-006", "Conjunto Deportivo Kids Active",
                    "Conjunto infantil de campera y pantalón deportivo, en algodón frizado de alta resistencia.",
                    "M", false, 28500.0, "Niños", "Ropa", "6.jpg"),
            new ProductoEjemplo("ZAP-007", "Zapatillas Training Air Fit",
                    "Calzado ultraliviano para entrenamientos funcionales, cross-training y caminatas.",
                    "38", true, 58000.0, "Mujeres", "Calzado", "7.jpg"),
            new ProductoEjemplo("GOR-008", "Gorra Deportiva UV Protect",
                    "Gorra con visera curva y microperforaciones para ventilación, con protección UV.",
                    "Único", false, 12500.0, "Mujeres", "Accesorios", "8.jpg")
    );

    private final ProductoService productoService;
    private final ImagenService imagenService;
    private final VigenciaPrecioService vigenciaPrecioService;
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubCategoriaRepository subCategoriaRepository;

    public ProductosDemo(ProductoService productoService, ImagenService imagenService,
                         VigenciaPrecioService vigenciaPrecioService, ProductoRepository productoRepository,
                         CategoriaRepository categoriaRepository, SubCategoriaRepository subCategoriaRepository) {
        this.productoService = productoService;
        this.imagenService = imagenService;
        this.vigenciaPrecioService = vigenciaPrecioService;
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.subCategoriaRepository = subCategoriaRepository;
    }

    @Override
    public void run(String... args) {
        for (ProductoEjemplo ejemplo : PRODUCTOS) {
            if (productoRepository.findByCodigoIgnoreCase(ejemplo.codigo()).isPresent()) {
                continue;
            }
            try {
                crearProducto(ejemplo);
            } catch (Exception e) {
                // Un producto de ejemplo fallido no debe impedir el arranque de la aplicación
                System.err.println("No se pudo crear el producto de ejemplo " + ejemplo.codigo() + ": " + e.getMessage());
            }
        }
    }

    private void crearProducto(ProductoEjemplo ejemplo) throws Exception {
        Categoria categoria = categoriaRepository.findByNombreIgnoreCase(ejemplo.categoria())
                .orElseThrow(() -> new IllegalStateException("Falta la categoría " + ejemplo.categoria()));
        SubCategoria subCategoria = subCategoriaRepository.findByCategoriaId(categoria.getId()).stream()
                .filter(s -> s.getNombre().equalsIgnoreCase(ejemplo.subCategoria()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Falta la subcategoría " + ejemplo.subCategoria()));

        Producto producto = productoService.crearProducto(ejemplo.codigo(), ejemplo.nombre(), ejemplo.descripcion(),
                ejemplo.talle(), ejemplo.enOferta(), crearImagen(ejemplo.archivoImagen()), subCategoria.getId());

        // Precio anterior (ya cerrado) y precio vigente (fechaHasta nula), actualizados cada dos meses
        LocalDate hoy = LocalDate.now();
        Date inicioAnterior = Date.valueOf(hoy.minusMonths(4));
        Date inicioVigente = Date.valueOf(hoy.minusMonths(2));
        double precioAnterior = Math.round(ejemplo.precio() * 0.85);
        vigenciaPrecioService.crearVigenciaPrecio(inicioAnterior, inicioVigente, precioAnterior, producto.getId());
        vigenciaPrecioService.crearVigenciaPrecio(inicioVigente, null, ejemplo.precio(), producto.getId());
    }

    // Devuelve el id de la imagen creada, o null si el archivo no está disponible (el producto se crea igual)
    private String crearImagen(String archivo) {
        ClassPathResource recurso = new ClassPathResource("static/tienda/images/products/" + archivo);
        if (!recurso.exists()) {
            return null;
        }
        try (InputStream in = recurso.getInputStream()) {
            Imagen imagen = imagenService.crearImagen(archivo, "image/jpeg", in.readAllBytes(), TipoImagen.PRODUCTO);
            return imagen.getId();
        } catch (Exception e) {
            return null;
        }
    }
}
