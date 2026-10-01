package com.tienda.zero.config;

import com.tienda.zero.enums.TipoImagen;
import com.tienda.zero.enums.TipoContacto;
import com.tienda.zero.enums.TipoTelefono;
import com.tienda.zero.model.Categoria;
import com.tienda.zero.model.Contacto;
import com.tienda.zero.model.ContactoTelefonico;
import com.tienda.zero.model.Imagen;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.Proveedor;
import com.tienda.zero.model.SubCategoria;
import com.tienda.zero.repository.CategoriaRepository;
import com.tienda.zero.repository.ProductoRepository;
import com.tienda.zero.repository.SubCategoriaRepository;
import com.tienda.zero.service.ImagenService;
import com.tienda.zero.service.ProductoService;
import com.tienda.zero.service.ProveedorService;
import com.tienda.zero.service.StockService;
import com.tienda.zero.service.VigenciaPrecioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final ProveedorService proveedorService;
    private final StockService stockService;

    public ProductosDemo(ProductoService productoService, ImagenService imagenService,
                         VigenciaPrecioService vigenciaPrecioService, ProductoRepository productoRepository,
                         CategoriaRepository categoriaRepository, SubCategoriaRepository subCategoriaRepository,
                         ProveedorService proveedorService, StockService stockService) {
        this.productoService = productoService;
        this.imagenService = imagenService;
        this.vigenciaPrecioService = vigenciaPrecioService;
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.subCategoriaRepository = subCategoriaRepository;
        this.proveedorService = proveedorService;
        this.stockService = stockService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Proveedor proveedor = proveedorService.listarProveedorActivo().stream()
                .filter(item -> item.getRazonSocial().equalsIgnoreCase("Textil Andes S.R.L."))
                .findFirst()
                .orElseGet(() -> proveedorService.crearProveedor("Textil Andes S.R.L.", List.of()));
        asegurarTelefonoDemo(proveedor);
        for (ProductoEjemplo ejemplo : PRODUCTOS) {
            try {
                var existente = productoRepository.findByCodigoIgnoreCase(ejemplo.codigo());
                if (existente.isPresent()) {
                    completarConfiguracion(existente.get(), ejemplo.codigo(), proveedor);
                } else {
                    Producto producto = crearProducto(ejemplo);
                    completarConfiguracion(producto, ejemplo.codigo(), proveedor);
                }
            } catch (Exception e) {
                // Un producto de ejemplo fallido no debe impedir el arranque de la aplicación
                System.err.println("No se pudo crear el producto de ejemplo " + ejemplo.codigo() + ": " + e.getMessage());
            }
        }
        // Completa también productos ya cargados fuera del catálogo demo para que el inventario
        // inicial no deje filas sin stock ideal, saldo o proveedor.
        for (Producto producto : productoService.listarProductoActivo()) {
            try {
                completarConfiguracion(producto, producto.getCodigo(), proveedor);
            } catch (Exception e) {
                System.err.println("No se pudo completar el inventario de " + producto.getCodigo() + ": " + e.getMessage());
            }
        }
    }

    private void asegurarTelefonoDemo(Proveedor proveedor) {
        boolean tieneTelefonoPermitido = proveedor.getContactos().stream()
                .filter(contacto -> !contacto.isEliminado() && contacto instanceof ContactoTelefonico)
                .map(contacto -> ((ContactoTelefonico) contacto).getTelefono())
                .anyMatch(telefono -> "+5492615341338".equals(telefono) || "+5492613179999".equals(telefono));
        if (tieneTelefonoPermitido) return;

        List<Contacto> contactos = new java.util.ArrayList<>(proveedor.getContactos());
        contactos.add(ContactoTelefonico.builder()
                .telefono("+5492615341338")
                .tipoTelefono(TipoTelefono.CELULAR)
                .tipoContacto(TipoContacto.EMPRESA)
                .observacion("Contacto para reposición de stock")
                .eliminado(false)
                .build());
        proveedor = proveedorService.modificarProveedor(proveedor.getId(), proveedor.getRazonSocial(), contactos);
    }

    private Producto crearProducto(ProductoEjemplo ejemplo) throws Exception {
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
        return producto;
    }

    private void completarConfiguracion(Producto producto, String codigo, Proveedor proveedor) {
        if (producto.getStockIdeal() <= 0) producto.setStockIdeal(stockIdeal(codigo));
        if (producto.getProveedor() == null) producto.setProveedor(proveedor);
        productoRepository.save(producto);
        if (stockService.calcularStockActual(producto.getId()) == 0) {
            stockService.registrarMovimiento(producto.getId(), stockInicial(codigo), "Stock inicial de demostración", null);
        }
    }

    private int stockIdeal(String codigo) {
        return switch (codigo) {
            case "CAL-002" -> 120;
            case "ZAP-003", "ZAP-007" -> 60;
            case "ACC-005" -> 40;
            case "CON-006" -> 80;
            case "GOR-008" -> 50;
            default -> 100;
        };
    }

    private int stockInicial(String codigo) {
        return switch (codigo) {
            case "CAL-002" -> 90;
            case "ZAP-003" -> 45;
            case "TOP-004" -> 70;
            case "ACC-005" -> 25;
            case "CON-006" -> 55;
            case "ZAP-007" -> 40;
            case "GOR-008" -> 35;
            default -> 70;
        };
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
