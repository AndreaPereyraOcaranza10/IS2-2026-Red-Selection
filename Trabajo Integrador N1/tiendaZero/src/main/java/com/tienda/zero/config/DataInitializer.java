package com.tienda.zero.config;

import com.tienda.zero.dto.PerfilDTO;
import com.tienda.zero.enums.Rol;
import com.tienda.zero.enums.Sexo;
import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.enums.TipoImagen;
import com.tienda.zero.enums.TipoTelefono;
import com.tienda.zero.model.*;
import com.tienda.zero.repository.*;
import com.tienda.zero.service.ImagenService;
import com.tienda.zero.service.PerfilService;
import com.tienda.zero.service.ProductoService;
import com.tienda.zero.service.VigenciaPrecioService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PaisRepository paisRepository;
    private final ProvinciaRepository provinciaRepository;
    private final DepartamentoRepository departamentoRepository;
    private final LocalidadRepository localidadRepository;
    private final NacionalidadRepository nacionalidadRepository;
    private final PerfilService perfilService;

    @Override
    public void run(String... args) {
        System.out.println(">>> [DataInitializer] Iniciando verificación de datos iniciales...");
        inicializarGeografia();
        inicializarCategorias();
        inicializarProductos();
        inicializarUsuarios();
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

    private void inicializarGeografia() {
        if (paisRepository.count() == 0) {
            Pais argentina = paisRepository.save(Pais.builder().nombre("Argentina").eliminado(false).build());

            Provincia mendoza = provinciaRepository.save(Provincia.builder().nombre("Mendoza").pais(argentina).eliminado(false).build());
            provinciaRepository.save(Provincia.builder().nombre("Buenos Aires").pais(argentina).eliminado(false).build());
            provinciaRepository.save(Provincia.builder().nombre("Córdoba").pais(argentina).eliminado(false).build());
            provinciaRepository.save(Provincia.builder().nombre("Santa Fe").pais(argentina).eliminado(false).build());
            provinciaRepository.save(Provincia.builder().nombre("San Juan").pais(argentina).eliminado(false).build());

            Departamento capital = departamentoRepository.save(Departamento.builder().nombre("Capital").provincia(mendoza).eliminado(false).build());
            Departamento godoyCruz = departamentoRepository.save(Departamento.builder().nombre("Godoy Cruz").provincia(mendoza).eliminado(false).build());
            Departamento guaymallen = departamentoRepository.save(Departamento.builder().nombre("Guaymallén").provincia(mendoza).eliminado(false).build());
            departamentoRepository.save(Departamento.builder().nombre("Las Heras").provincia(mendoza).eliminado(false).build());
            departamentoRepository.save(Departamento.builder().nombre("Luján de Cuyo").provincia(mendoza).eliminado(false).build());
            departamentoRepository.save(Departamento.builder().nombre("Maipú").provincia(mendoza).eliminado(false).build());
            departamentoRepository.save(Departamento.builder().nombre("San Rafael").provincia(mendoza).eliminado(false).build());

            localidadRepository.save(Localidad.builder().nombre("Ciudad de Mendoza").codigoPostal("5500").departamento(capital).eliminado(false).build());
            localidadRepository.save(Localidad.builder().nombre("Godoy Cruz").codigoPostal("5501").departamento(godoyCruz).eliminado(false).build());
            localidadRepository.save(Localidad.builder().nombre("Villa Nueva").codigoPostal("5521").departamento(guaymallen).eliminado(false).build());

            nacionalidadRepository.save(Nacionalidad.builder().nombre("Argentina").eliminado(false).build());
            nacionalidadRepository.save(Nacionalidad.builder().nombre("Chilena").eliminado(false).build());
            nacionalidadRepository.save(Nacionalidad.builder().nombre("Uruguaya").eliminado(false).build());
            nacionalidadRepository.save(Nacionalidad.builder().nombre("Brasileña").eliminado(false).build());

            System.out.println(">>> [DataInitializer] Datos geográficos base asegurados en BD.");
        }
    }

    private void inicializarUsuarios() {
        // 1. Usuario Administrador por defecto
        if (!usuarioRepository.existsByEmailIgnoreCase("admin@tiendazero.com")) {
            Usuario admin = Usuario.builder()
                    .email("admin@tiendazero.com")
                    .password(passwordEncoder.encode("admin123"))
                    .rol(Rol.ADMIN)
                    .activo(true)
                    .eliminado(false)
                    .build();
            usuarioRepository.save(admin);
            System.out.println(">>> [DataInitializer] Usuario Administrador creado: admin@tiendazero.com / admin123");
        }

        // 2. Usuario Cliente demo con perfil completo
        if (!usuarioRepository.existsByEmailIgnoreCase("cliente@tiendazero.com")) {
            Usuario clienteUser = Usuario.builder()
                    .email("cliente@tiendazero.com")
                    .password(passwordEncoder.encode("cliente123"))
                    .rol(Rol.CLIENTE)
                    .activo(true)
                    .eliminado(false)
                    .build();
            clienteUser = usuarioRepository.save(clienteUser);

            // Completar su perfil personal según requerimiento
            PerfilDTO perfilDemo = PerfilDTO.builder()
                    .nombre("Juan")
                    .apellido("Pérez")
                    .sexo(Sexo.MASCULINO)
                    .fechaNacimiento("1995-05-15")
                    .tipoDocumento(TipoDocumento.DNI)
                    .numeroDocumento("38945123")
                    .telefono("+54 9 261 456-7890")
                    .tipoTelefono(TipoTelefono.CELULAR)
                    .provincia("Mendoza")
                    .departamento("Capital")
                    .localidad("Ciudad de Mendoza")
                    .codigoPostal("5500")
                    .calle("Av. San Martín")
                    .numeroCalle("1450")
                    .manzanaPiso("Piso 3")
                    .casaDepartamento("Dpto B")
                    .referencia("Frente a la plaza")
                    .build();

            perfilService.guardarPerfilUsuario(clienteUser.getEmail(), perfilDemo);
            System.out.println(">>> [DataInitializer] Usuario Cliente demo creado: cliente@tiendazero.com / cliente123");
        }
    }
}


