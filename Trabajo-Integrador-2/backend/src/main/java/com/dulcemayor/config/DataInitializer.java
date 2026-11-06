package com.dulcemayor.config;

import com.dulcemayor.enums.Rol;
import com.dulcemayor.model.Categoria;
import com.dulcemayor.model.Producto;
import com.dulcemayor.model.SubCategoria;
import com.dulcemayor.model.Usuario;
import com.dulcemayor.repository.CategoriaRepository;
import com.dulcemayor.repository.DepartamentoRepository;
import com.dulcemayor.repository.LocalidadRepository;
import com.dulcemayor.repository.PaisRepository;
import com.dulcemayor.repository.ProductoRepository;
import com.dulcemayor.repository.ProvinciaRepository;
import com.dulcemayor.repository.SubCategoriaRepository;
import com.dulcemayor.repository.UsuarioRepository;
import com.dulcemayor.service.CategoriaService;
import com.dulcemayor.service.ProductoService;
import com.dulcemayor.service.SubCategoriaService;
import com.dulcemayor.service.VigenciaPrecioService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final CategoriaRepository categories;
    private final PaisRepository countries;
    private final ProvinciaRepository provinces;
    private final DepartamentoRepository departments;
    private final LocalidadRepository localities;
    private final SubCategoriaRepository subcategories;
    private final ProductoRepository products;
    private final UsuarioRepository users;
    private final CategoriaService categoryService;
    private final SubCategoriaService subcategoryService;
    private final ProductoService productService;
    private final VigenciaPrecioService prices;
    private final PasswordEncoder encoder;

    private record Seed(String sku, String name, String brand, String category, String pack, int units,
                        int stock, int minimum, double price, String color, String image, String description, boolean offer) {}

    @Override
    public void run(String... args) {
        seedLocations();
        seedCatalog();
        seedAdmin();
    }

    private void seedLocations() {
        var argentina = countries.findByNombre("Argentina").orElseGet(() -> countries.save(
                com.dulcemayor.model.Pais.builder().nombre("Argentina").eliminado(false).build()));
        Map<String, Map<String, List<String>>> locations = new LinkedHashMap<>();
        locations.put("Mendoza", Map.ofEntries(
                Map.entry("Capital", List.of("Ciudad de Mendoza")),
                Map.entry("Godoy Cruz", List.of("Godoy Cruz", "Villa Hipódromo")),
                Map.entry("Guaymallén", List.of("Villa Nueva", "San José", "Rodeo de la Cruz", "Dorrego")),
                Map.entry("Las Heras", List.of("Las Heras", "El Challao", "Uspallata")),
                Map.entry("Luján de Cuyo", List.of("Luján de Cuyo", "Chacras de Coria", "Vistalba")),
                Map.entry("Maipú", List.of("Maipú", "Gutiérrez", "Rodeo del Medio")),
                Map.entry("Lavalle", List.of("Villa Tulumaya")),
                Map.entry("Junín", List.of("Junín")),
                Map.entry("La Paz", List.of("La Paz")),
                Map.entry("Malargüe", List.of("Malargüe")),
                Map.entry("Rivadavia", List.of("Rivadavia")),
                Map.entry("San Carlos", List.of("La Consulta", "Eugenio Bustos")),
                Map.entry("San Martín", List.of("San Martín")),
                Map.entry("San Rafael", List.of("San Rafael", "Cuadro Nacional")),
                Map.entry("Santa Rosa", List.of("Santa Rosa")),
                Map.entry("Tunuyán", List.of("Tunuyán")),
                Map.entry("Tupungato", List.of("Tupungato")),
                Map.entry("General Alvear", List.of("General Alvear"))));
        locations.put("San Juan", Map.ofEntries(
                Map.entry("Capital", List.of("San Juan")), Map.entry("Albardón", List.of("Albardón")),
                Map.entry("Angaco", List.of("Villa del Salvador")), Map.entry("Calingasta", List.of("Calingasta")),
                Map.entry("Caucete", List.of("Caucete")), Map.entry("Chimbas", List.of("Chimbas")),
                Map.entry("Iglesia", List.of("Rodeo")), Map.entry("Jáchal", List.of("San José de Jáchal")),
                Map.entry("9 de Julio", List.of("9 de Julio")), Map.entry("Pocito", List.of("Villa Aberastain")),
                Map.entry("Rawson", List.of("Villa Krause")), Map.entry("Rivadavia", List.of("Rivadavia")),
                Map.entry("San Martín", List.of("San Martín")), Map.entry("Santa Lucía", List.of("Santa Lucía")),
                Map.entry("Sarmiento", List.of("Media Agua")), Map.entry("Ullum", List.of("Villa Ibáñez")),
                Map.entry("Valle Fértil", List.of("San Agustín del Valle Fértil")),
                Map.entry("25 de Mayo", List.of("Santa Rosa")), Map.entry("Zonda", List.of("Villa Basilio Nievas"))));

        locations.forEach((provinceName, departmentMap) -> {
            var province = provinces.findByNombre(provinceName).orElseGet(() -> provinces.save(
                    com.dulcemayor.model.Provincia.builder().nombre(provinceName).pais(argentina).eliminado(false).build()));
            departmentMap.forEach((departmentName, localityNames) -> {
                var department = departments.findByProvinciaIdAndEliminadoFalse(province.getId()).stream()
                        .filter(d -> d.getNombre().equalsIgnoreCase(departmentName)).findFirst()
                        .orElseGet(() -> departments.save(com.dulcemayor.model.Departamento.builder()
                                .nombre(departmentName).provincia(province).eliminado(false).build()));
                localityNames.forEach(localityName -> {
                    boolean exists = localities.findByDepartamentoIdAndEliminadoFalse(department.getId()).stream()
                            .anyMatch(l -> l.getNombre().equalsIgnoreCase(localityName));
                    if (!exists) localities.save(com.dulcemayor.model.Localidad.builder().nombre(localityName)
                            .codigoPostal("").departamento(department).eliminado(false).build());
                });
            });
        });
    }

    private void seedCatalog() {
        List<Seed> seeds = List.of(
                new Seed("ARC-BON-048", "Bon o Bon Clásico", "Arcor", "Chocolates", "Caja x 60 unidades", 60, 42, 8, 28400, "#f7e9d8", "https://lh3.googleusercontent.com/aida-public/AB6AXuCmfL6TWsrsTd6seBgB3r-bu8AMC_0-QGV37mvBBWl-yHaPliqMLa1V-hkqWiMRGF4wpVUBlELMGy3Pjuv2kVNO3ynpQ6YO6vNgfN4krb33blEeftUPOS-ybtPFTarUBNRXrgHecc7Zr4VakbfNKQ3frlzwots9bW6iPS_W41Q6Y1ValBYEG9HMHChvp7gXtzTrwrsBu3a3u-cc-M6okcsWozxyVfYo6u2jDnMdwGU", "Bombón de chocolate con relleno de maní. Caja mayorista x 60 unidades.", true),
                new Seed("GUY-TRIP-CH", "Alfajor Triple Chocolate", "Guaymallén", "Alfajores", "Bulto x 6 cajas · 144 unidades", 144, 12, 50, 54600, "#f3e7df", "https://lh3.googleusercontent.com/aida-public/AB6AXuDbbXR6WVgZgkKDFjO_vBTXNUMJtTbzNUAFBR3Ro4thv5w9V_zkfq0oWI0p2oNqeJ0K-e3gUGEJJfjJRF_UblKIRMsieTVEPoGVA1IeypDLe8LizwdRCPGmWFhKOAZnpNWf0qbPFHwyRJMw6TjZ4LrfdkXN3ejibLQQrXfsrMWOtnnto7TyQTlzQwWrFLnI7dY4eCHsk6BUdi-MXvkZdodAGWzXM3CU8EtRLWVVvzg", "Alfajor triple con cobertura de chocolate. Bulto cerrado.", false),
                new Seed("MOG-DIEN-012", "Mogul Dientes Gomitas", "Mogul", "Gomitas", "Caja x 12 unidades", 12, 68, 10, 16800, "#ffede5", "https://lh3.googleusercontent.com/aida-public/AB6AXuBwEADATTWmLGaMJuPCTMZs7RYphNzVb1cC_kcGuSKn-okE7IRyUI1zYzZB6j0OJvevj7mzq7q3Eqs-9sB4sj82CfIA2-GnE1ZgN8XnPWF7FvnZAIO9d1QQiMsyfiTeVu1fst6URhi53vxDosSDk570tyeO-G5H13fbRMsHLFsy3RQCQB30DCe-MUjcgU89-7s_OTdWvcb7Lb78bk0rd5qupOWLsluoUXpKgNtrknE", "Gomitas sabor frutal. Caja con doce bolsas.", false),
                new Seed("COF-BLK-038", "Chocolate Block 38g", "Cofler", "Chocolates", "Caja x 24 unidades", 24, 9, 15, 21600, "#eee5dc", "https://lh3.googleusercontent.com/aida-public/AB6AXuAFDjLm3zB2nO4cpuzOp2drKs7qGPeu3YaRZes8ib6NqjAzK_R6CTQK-RCJQ0orWaa9TotkiC-DNZewFT7a-oUT8djwwTSPWJRGXvpOsXC_Dj7GdJy1LpBJbMr_C4XRGk5Z2Ym_IkcdysFV5kmMZjvB-XExFNpxnoyTy2UsLCGJFWvVopSUdggvWfN2gFSstI7RhGao6qt5X-tkFZOOMpEkhqKCyez23wZxxYdf9pE", "Chocolate con leche en formato individual de 38 g.", false),
                new Seed("MON-BEL-MEN", "Beldent Menta Infinito", "Beldent", "Chicles", "Display x 20 unidades", 20, 4, 20, 19400, "#e5f2ee", "https://lh3.googleusercontent.com/aida-public/AB6AXuDWCbC9D0quQlQiSFmwMIP6HkffsH-QkduzQS2zQwb7rV_v1ckYbVGSz4P7VVkKdP3w3z79IXMW1hfolbT5XLJfG4vp6DLr9QK-cM4JhfLnoG5IxEBvQpvpLLaSP-z4TCEoM2e58C-IzYTGh_TZ072aJ-v3m4903ScBkTu7V7omo0I9T5l2dh7hhFuHs1vx6v5_DV_i-wl6_xcf3nZTAGBs_RVkVQEmiqJsVqEVV58", "Chicles sin azúcar con sabor a menta. Display x 20 unidades.", false),
                new Seed("GEO-PAL-024", "Palitos de la Selva", "Georgalos", "Caramelos", "Caja x 24 paquetes", 24, 31, 8, 13200, "#f4e9d8", "https://lh3.googleusercontent.com/aida-public/AB6AXuA2-HWdvR0tWavMD3RZWhLIsLr0dsUJJBVBIN-ymWAD6Gdp3u6A8Wq9eet-0Ce8Hxb7UThLGdhpqY_S8q5pC-aK03leK8F2WNgTSyNWVoSiPq_XDb-wa1oRfjsKaFgC4OxRWsZjFIQtjcfz9NoCeres1Mxs34VXqtFHgXVQBDDM4uj31HC-Ttoc__7-iyKkx4J6VtCDbqID6SsEAnIOgqmtu1_vqBl7yXlIQqMJevQ", "Caramelos masticables frutales, presentación por caja.", false)
        );
        for (Seed seed : seeds) {
            if (products.findByCodigoIgnoreCase(seed.sku()).isPresent()) continue;
            Categoria category = categories.findByNombreIgnoreCase(seed.category())
                    .orElseGet(() -> categoryService.crearCategoria(seed.category()));
            SubCategoria sub = subcategories.findByCategoriaIdAndEliminadoFalse(category.getId()).stream().findFirst()
                    .orElseGet(() -> subcategoryService.crearSubCategoria("General", category.getId()));
            Producto p = productService.crearProducto(seed.sku(), seed.name(), seed.description(), seed.pack(), seed.offer(), null, sub.getId());
            p.setMarca(seed.brand()); p.setPresentacion(seed.pack()); p.setUnidadesPorBulto(seed.units());
            p.setStock(seed.stock()); p.setStockMinimo(seed.minimum()); p.setImagenUrl(seed.image()); p.setColorImagen(seed.color());
            products.save(p);
            prices.crearVigenciaPrecio(Date.valueOf(LocalDate.now()), null, seed.price(), p.getId());
        }
    }

    private void seedAdmin() {
        String email = "admin@dulcemayor.com";
        if (users.existsByEmailIgnoreCase(email)) return;
        users.save(Usuario.builder().email(email).password(encoder.encode("dulcemayor"))
                .rol(Rol.ADMIN).activo(true).eliminado(false).build());
    }
}
