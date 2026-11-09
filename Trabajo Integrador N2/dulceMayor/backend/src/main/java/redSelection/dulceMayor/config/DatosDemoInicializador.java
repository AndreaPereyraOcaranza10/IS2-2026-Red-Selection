package redSelection.dulceMayor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.enums.EstadoPedido;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.enums.TipoContacto;
import redSelection.dulceMayor.enums.TipoDocumento;
import redSelection.dulceMayor.enums.TipoEmpresa;
import redSelection.dulceMayor.model.ClienteMinorista;
import redSelection.dulceMayor.model.ContactoCorreoElectronico;
import redSelection.dulceMayor.model.Direccion;
import redSelection.dulceMayor.model.DulceMayor;
import redSelection.dulceMayor.model.Empleado;
import redSelection.dulceMayor.model.Pedido;
import redSelection.dulceMayor.model.Preventista;
import redSelection.dulceMayor.model.ResponsableComercio;
import redSelection.dulceMayor.model.Transportista;
import redSelection.dulceMayor.model.Usuario;
import redSelection.dulceMayor.repository.ClienteMinoristaRepository;
import redSelection.dulceMayor.repository.DireccionRepository;
import redSelection.dulceMayor.repository.DulceMayorRepository;
import redSelection.dulceMayor.repository.EmpleadoRepository;
import redSelection.dulceMayor.repository.PedidoRepository;
import redSelection.dulceMayor.repository.PreventistaRepository;
import redSelection.dulceMayor.repository.ResponsableComercioRepository;
import redSelection.dulceMayor.repository.TransportistaRepository;
import redSelection.dulceMayor.repository.UsuarioRepository;
import redSelection.dulceMayor.service.AuthService;
import redSelection.dulceMayor.enums.Presentacion;
import redSelection.dulceMayor.enums.TipoMovimientoStock;
import redSelection.dulceMayor.model.Categoria;
import redSelection.dulceMayor.model.Producto;
import redSelection.dulceMayor.model.SubCategoria;
import redSelection.dulceMayor.repository.CategoriaRepository;
import redSelection.dulceMayor.repository.ProductoRepository;
import redSelection.dulceMayor.repository.SubCategoriaRepository;
import redSelection.dulceMayor.service.PrecioService;
import redSelection.dulceMayor.service.StockService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Datos de demostración para poder probar el sistema de punta a punta: la casa central, tres empleados con la
 * clave inicial (hay que cambiarla en el primer ingreso), seis comercios de Mendoza asignados al preventista
 * y cuatro pedidos ya facturados para que el transportista tenga qué repartir.
 * Cada bloque corre solo si su usuario ("preventista.mza", "preventista2.mza") todavía no existe, así que se puede repetir sin duplicar. No corre con el
 * perfil "prod" y se apaga con app.demo-data=false. Las direcciones y coordenadas son de ejemplo.
 */
@Component
@Profile("!prod")
@ConditionalOnProperty(name = "app.demo-data", havingValue = "true", matchIfMissing = true)
public class DatosDemoInicializador implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosDemoInicializador.class);

    private final UsuarioRepository usuarioRepository;
    private final PreventistaRepository preventistaRepository;
    private final TransportistaRepository transportistaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final DulceMayorRepository dulceMayorRepository;
    private final DireccionRepository direccionRepository;
    private final ResponsableComercioRepository responsableRepository;
    private final ClienteMinoristaRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final PasswordEncoder passwordEncoder;
    private final String correoResponsables;
    private final CategoriaRepository categoriaRepository;
    private final SubCategoriaRepository subCategoriaRepository;
    private final ProductoRepository productoRepository;
    private final PrecioService precioService;
    private final StockService stockService;

    public DatosDemoInicializador(UsuarioRepository usuarioRepository,
                                  PreventistaRepository preventistaRepository,
                                  TransportistaRepository transportistaRepository,
                                  EmpleadoRepository empleadoRepository,
                                  DulceMayorRepository dulceMayorRepository,
                                  DireccionRepository direccionRepository,
                                  ResponsableComercioRepository responsableRepository,
                                  ClienteMinoristaRepository clienteRepository,
                                  PedidoRepository pedidoRepository,
                                  PasswordEncoder passwordEncoder,
                                  @Value("${app.demo.correo-responsables:}") String correoResponsables,
                                  CategoriaRepository categoriaRepository,
                                  SubCategoriaRepository subCategoriaRepository,
                                  ProductoRepository productoRepository,
                                  PrecioService precioService,
                                  StockService stockService) {
        this.categoriaRepository = categoriaRepository;
        this.subCategoriaRepository = subCategoriaRepository;
        this.productoRepository = productoRepository;
        this.precioService = precioService;
        this.stockService = stockService;
        this.usuarioRepository = usuarioRepository;
        this.preventistaRepository = preventistaRepository;
        this.transportistaRepository = transportistaRepository;
        this.empleadoRepository = empleadoRepository;
        this.dulceMayorRepository = dulceMayorRepository;
        this.direccionRepository = direccionRepository;
        this.responsableRepository = responsableRepository;
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
        this.passwordEncoder = passwordEncoder;
        this.correoResponsables = correoResponsables == null ? "" : correoResponsables.trim();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.findByNombreUsuarioIgnoreCase("preventista.mza").isEmpty()) {
            cargarDatosBase();
        }
        if (usuarioRepository.findByNombreUsuarioIgnoreCase("preventista2.mza").isEmpty()) {
            cargarSegundoPreventista();
        }
        if (usuarioRepository.findByNombreUsuarioIgnoreCase("jefe.mza").isEmpty()) {
            empleadoRepository.save(Empleado.builder()
                    .nombre("Sofía").apellido("Navarro")
                    .tipoDocumento(TipoDocumento.DNI).numeroDocumento("24888999")
                    .usuario(usuarioEmpleado("jefe.mza", Rol.JEFE_ADMINISTRATIVO))
                    .build());
            log.info("Jefe administrativo listo: usuario jefe.mza (clave inicial '{}').", AuthService.CLAVE_INICIAL);
        }
        // Mantener disponible la cuenta demo aunque la base ya tuviera datos parciales.
        if (usuarioRepository.findByNombreUsuarioIgnoreCase("admin.mza").isEmpty()) {
            usuarioRepository.save(usuarioEmpleado("admin.mza", Rol.ADMINISTRATIVO));
            log.info("Administrativo de demostracion listo: usuario admin.mza (clave inicial '{}').", AuthService.CLAVE_INICIAL);
        }
        if (productoRepository.findByEliminadoFalse().isEmpty()) {
            cargarCatalogo();
        }
    }

    /**
     * Catálogo mínimo para poder armar pedidos: categorías, productos con precio vigente y stock inicial.
     * Hay uno sin stock (no aparece en el catálogo) y dos con stock bajo. BK-17 lo amplía con imágenes y más datos.
     */
    private void cargarCatalogo() {
        producto("DM-001", "Alfajor triple chocolate", "Alfajores", "Triples", Presentacion.BULTO, 24, "18400", 120);
        producto("DM-002", "Chicles menta (display)", "Chicles", "Menta", Presentacion.BULTO, 20, "32900", 80);
        producto("DM-003", "Caramelos masticables frutales", "Caramelos", "Masticables", Presentacion.PACK, 50, "6800", 12);
        producto("DM-004", "Chocolate con maní", "Chocolates", "Barras", Presentacion.PACK, 12, "14250", 60);
        producto("DM-005", "Gomitas ositos", "Gomitas", "Ositos", Presentacion.BULTO, 12, "21600", 9);
        producto("DM-006", "Turrón de maní", "Turrones", "Maní", Presentacion.BULTO, 50, "11900", 40);
        producto("DM-007", "Obleas rellenas", "Snacks", "Obleas", Presentacion.PACK, 36, "9700", 75);
        producto("DM-008", "Bombones surtidos", "Chocolates", "Bombones", Presentacion.BULTO, 24, "25800", 0);
        log.info("Catálogo de demostración listo: 8 productos con precio y stock.");
    }

    private void producto(String codigo, String nombre, String categoria, String subcategoria,
                          Presentacion presentacion, int unidades, String precio, int stock) {
        Categoria cat = categoriaRepository.findByEliminadoFalse().stream()
                .filter(c -> c.getNombre().equals(categoria)).findFirst()
                .orElseGet(() -> categoriaRepository.save(Categoria.builder().nombre(categoria).build()));
        SubCategoria sub = subCategoriaRepository.save(SubCategoria.builder().nombre(subcategoria).categoria(cat).build());
        Producto producto = productoRepository.save(Producto.builder()
                .codigo(codigo).nombre(nombre).marca("Dulce Mayor")
                .presentacion(presentacion).unidadesPorPresentacion(unidades)
                .stockMinimo(20).subCategoria(sub)
                .build());
        precioService.fijarPrecio(producto, new BigDecimal(precio), LocalDate.now().minusDays(30));
        if (stock > 0) {
            stockService.registrar(producto, TipoMovimientoStock.INGRESO, stock, null);
        }
    }

    private void cargarDatosBase() {
        log.info("Cargando datos de demostración (clave inicial de los empleados: '{}')...", AuthService.CLAVE_INICIAL);

        crearCasaCentralSiNoExiste();

        Preventista preventista = preventistaRepository.save(Preventista.builder()
                .nombre("Martín").apellido("Gómez")
                .tipoDocumento(TipoDocumento.DNI).numeroDocumento("30111222")
                .zonaAsignada("Gran Mendoza")
                .usuario(usuarioEmpleado("preventista.mza", Rol.PREVENTISTA))
                .build());
        transportistaRepository.save(Transportista.builder()
                .nombre("Diego").apellido("Rojas")
                .tipoDocumento(TipoDocumento.DNI).numeroDocumento("28333444")
                .patenteVehiculo("AA123BB")
                .usuario(usuarioEmpleado("transportista.mza", Rol.TRANSPORTISTA))
                .build());
        empleadoRepository.save(Empleado.builder()
                .nombre("Alex").apellido("Chen")
                .tipoDocumento(TipoDocumento.DNI).numeroDocumento("27555666")
                .usuario(usuarioEmpleado("admin.mza", Rol.ADMINISTRATIVO))
                .build());

        List<ClienteMinorista> clientes = new ArrayList<>();
        clientes.add(comercio(preventista, "Kiosco El Sol", "Lucía", "Fernández", "30111001",
                "Av. San Martín", "1250", "Ciudad", "50007", "Capital", -32.8943, -68.8441));
        clientes.add(comercio(preventista, "Almacén Don Pepe", "José", "Ruiz", "30111002",
                "Belgrano", "480", "Ciudad", "50007", "Capital", -32.8962, -68.8397));
        clientes.add(comercio(preventista, "Drugstore 24 hs", "María", "Gómez", "30111003",
                "Las Heras", "220", "Ciudad", "50007", "Capital", -32.8867, -68.8419));
        clientes.add(comercio(preventista, "Autoservicio La Esquina", "Carlos", "Díaz", "30111004",
                "Av. Godoy Cruz", "1830", "Godoy Cruz", "50035", "Godoy Cruz", -32.9189, -68.8591));
        clientes.add(comercio(preventista, "Kiosco Las Palmeras", "Ana", "Morales", "30111005",
                "Mitre", "765", "Guaymallén", "50042", "Guaymallén", -32.8878, -68.8190));
        clientes.add(comercio(preventista, "Maxikiosco Cuyo", "Pablo", "Sosa", "30111006",
                "Boulogne Sur Mer", "90", "Las Heras", "50049", "Las Heras", -32.8583, -68.8420));

        // Los primeros cuatro comercios tienen un pedido ya facturado: el transportista los recibe en su ruta.
        BigDecimal[] totales = {new BigDecimal("55700.00"), new BigDecimal("71250.00"),
                new BigDecimal("98400.00"), new BigDecimal("83300.00")};
        for (int i = 0; i < totales.length; i++) {
            pedidoRepository.save(Pedido.builder()
                    .fechaHoraCarga(LocalDateTime.now().minusDays(1).minusMinutes(i * 10L))
                    .estado(EstadoPedido.FACTURADO)
                    .cliente(clientes.get(i))
                    .preventista(preventista)
                    .subtotal(totales[i])
                    .total(totales[i])
                    .build());
        }
        log.info("Datos de demostración listos: usuarios preventista.mza, transportista.mza y admin.mza.");
    }

    /** Segundo preventista con dos comercios propios: sirve para probar que cada uno solo opera lo suyo. */
    private void cargarSegundoPreventista() {
        crearCasaCentralSiNoExiste();
        Preventista preventista = preventistaRepository.save(Preventista.builder()
                .nombre("Max").apellido("Price")
                .tipoDocumento(TipoDocumento.DNI).numeroDocumento("31222333")
                .zonaAsignada("Sur de Mendoza")
                .usuario(usuarioEmpleado("preventista2.mza", Rol.PREVENTISTA))
                .build());
        comercio(preventista, "Kiosco Los Andes", "Rosa", "Vega", "30111007",
                "Ozamis Sur", "310", "Maipú", "50056", "Maipú", -32.9731, -68.7863);
        comercio(preventista, "Almacén La Vid", "Hugo", "Paz", "30111008",
                "San Martín", "1500", "Luján de Cuyo", "50063", "Luján de Cuyo", -33.0386, -68.8786);
        log.info("Segundo preventista listo: usuario preventista2.mza (clave inicial '{}').", AuthService.CLAVE_INICIAL);
    }

    private Usuario usuarioEmpleado(String nombreUsuario, Rol rol) {
        return Usuario.builder()
                .nombreUsuario(nombreUsuario)
                .clave(passwordEncoder.encode(AuthService.CLAVE_INICIAL))
                .rol(rol)
                .primerIngreso(true)
                .build();
    }

    private void crearCasaCentralSiNoExiste() {
        boolean existe = dulceMayorRepository.findByEliminadoFalse().stream()
                .anyMatch(d -> d.getTipoEmpresa() == TipoEmpresa.SEDE_CENTRAL);
        if (existe) {
            return;
        }
        dulceMayorRepository.save(DulceMayor.builder()
                .razonSocial("Dulce Mayor S.A.").cuit("30-70000000-1")
                .nombre("Casa Central").tipoEmpresa(TipoEmpresa.SEDE_CENTRAL)
                .aliasTransferencia("dulce.mayor.mza")
                .direccion(direccion("Av. España", "1020", "Ciudad", "50007", "Capital", -32.8895, -68.8458))
                .build());
    }

    private ClienteMinorista comercio(Preventista preventista, String nombreComercio, String nombreResp,
                                      String apellidoResp, String dniResp, String calle, String numero,
                                      String localidad, String departamentoId, String departamento,
                                      double latitud, double longitud) {
        List<redSelection.dulceMayor.model.Contacto> contactos = new ArrayList<>();
        if (!correoResponsables.isEmpty()) {
            contactos.add(ContactoCorreoElectronico.builder()
                    .tipoContacto(TipoContacto.LABORAL).principal(true).email(correoResponsables).build());
        }
        ResponsableComercio responsable = responsableRepository.save(ResponsableComercio.builder()
                .nombre(nombreResp).apellido(apellidoResp)
                .tipoDocumento(TipoDocumento.DNI).numeroDocumento(dniResp)
                .contactos(contactos)
                .build());
        return clienteRepository.save(ClienteMinorista.builder()
                .razonSocial(nombreComercio).cuit("20-" + dniResp + "-3")
                .direccion(direccion(calle, numero, localidad, departamentoId, departamento, latitud, longitud))
                .nombreComercio(nombreComercio)
                .responsable(responsable)
                .preventistaAsignado(preventista)
                .build());
    }

    /** Provincia de Mendoza (id Georef 50). Los ids de departamento y localidad son de ejemplo. */
    private Direccion direccion(String calle, String numeracion, String localidad, String departamentoId,
                                String departamento, double latitud, double longitud) {
        return direccionRepository.save(Direccion.builder()
                .calle(calle).numeracion(numeracion)
                .latitud(latitud).longitud(longitud)
                .provinciaId("50").provinciaNombre("Mendoza")
                .departamentoId(departamentoId).departamentoNombre(departamento)
                .localidadId(departamentoId + "010").localidadNombre(localidad)
                .build());
    }
}