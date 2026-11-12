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
import redSelection.dulceMayor.model.PedidoItem;
import redSelection.dulceMayor.model.FacturaCliente;
import redSelection.dulceMayor.model.DetalleFactura;
import redSelection.dulceMayor.model.HistorialEstadoPedido;
import redSelection.dulceMayor.model.Imagen;
import redSelection.dulceMayor.model.Pago;
import redSelection.dulceMayor.model.Preventista;
import redSelection.dulceMayor.model.ResponsableComercio;
import redSelection.dulceMayor.model.Transportista;
import redSelection.dulceMayor.model.Usuario;
import redSelection.dulceMayor.repository.ClienteMinoristaRepository;
import redSelection.dulceMayor.repository.DireccionRepository;
import redSelection.dulceMayor.repository.DulceMayorRepository;
import redSelection.dulceMayor.repository.EmpleadoRepository;
import redSelection.dulceMayor.repository.PedidoRepository;
import redSelection.dulceMayor.repository.FacturaClienteRepository;
import redSelection.dulceMayor.repository.ImagenRepository;
import redSelection.dulceMayor.repository.PagoRepository;
import redSelection.dulceMayor.repository.PreventistaRepository;
import redSelection.dulceMayor.repository.ResponsableComercioRepository;
import redSelection.dulceMayor.repository.TransportistaRepository;
import redSelection.dulceMayor.repository.UsuarioRepository;
import redSelection.dulceMayor.service.AuthService;
import redSelection.dulceMayor.enums.Presentacion;
import redSelection.dulceMayor.enums.TipoMovimientoStock;
import redSelection.dulceMayor.enums.TipoPromocion;
import redSelection.dulceMayor.enums.TipoImagen;
import redSelection.dulceMayor.enums.TipoPago;
import redSelection.dulceMayor.enums.EstadoPago;
import redSelection.dulceMayor.enums.EstadoFactura;
import redSelection.dulceMayor.enums.EstadoStockPedido;
import redSelection.dulceMayor.model.Promocion;
import redSelection.dulceMayor.repository.PromocionRepository;
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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

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
    private final PromocionRepository promocionRepository;
    private final FacturaClienteRepository facturaRepository;
    private final ImagenRepository imagenRepository;
    private final PagoRepository pagoRepository;

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
                                  StockService stockService,
                                  PromocionRepository promocionRepository,
                                  FacturaClienteRepository facturaRepository,
                                  ImagenRepository imagenRepository,
                                  PagoRepository pagoRepository) {
        this.promocionRepository = promocionRepository;
        this.facturaRepository = facturaRepository;
        this.imagenRepository = imagenRepository;
        this.pagoRepository = pagoRepository;
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
        cargarCatalogo();
        if (promocionRepository.findByEliminadoFalse().isEmpty()) {
            cargarPromociones();
        }
        cargarDatosBK17();
    }

    /** DULCE10: 10% sobre todo el pedido (volumen, desde 1 unidad). ALFAJOR15: 15% solo sobre el alfajor. */
    private void cargarPromociones() {
        LocalDate desde = LocalDate.now().minusDays(30);
        promocionRepository.save(Promocion.builder().codigo("DULCE10").tipoPromocion(TipoPromocion.VOLUMEN)
                .porcentajeDescuento(new BigDecimal("10")).cantidadMinima(1).fechaDesde(desde).build());
        productoRepository.findByEliminadoFalse().stream()
                .filter(p -> p.getCodigo().equals("DM-001")).findFirst()
                .ifPresent(alfajor -> promocionRepository.save(Promocion.builder().codigo("ALFAJOR15")
                        .tipoPromocion(TipoPromocion.PRODUCTO_DESTACADO).porcentajeDescuento(new BigDecimal("15"))
                        .producto(alfajor).fechaDesde(desde).build()));
        log.info("Promociones de demostración listas: DULCE10 y ALFAJOR15.");
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
        Producto existente = productoRepository.findByCodigoIgnoreCase(codigo).orElse(null);
        if (existente != null) {
            if (!existente.isEliminado() && existente.getImagen() == null) {
                existente.setImagen(crearImagenDemo(codigo + "-producto.png", TipoImagen.PRODUCTO,
                        new Color(197, 22, 91), nombre));
                productoRepository.save(existente);
            }
            return;
        }
        Categoria cat = categoriaRepository.findByEliminadoFalse().stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(categoria)).findFirst()
                .orElseGet(() -> categoriaRepository.save(Categoria.builder().nombre(categoria).build()));
        SubCategoria sub = subCategoriaRepository.findByNombreIgnoreCaseAndCategoriaId(subcategoria, cat.getId())
                .filter(s -> !s.isEliminado())
                .orElseGet(() -> subCategoriaRepository.save(
                        SubCategoria.builder().nombre(subcategoria).categoria(cat).build()));
        Producto producto = productoRepository.save(Producto.builder()
                .codigo(codigo).nombre(nombre).marca("Dulce Mayor")
                .descripcion("Producto de demostraciÃ³n para probar catÃ¡logo, precios, inventario y pedidos.")
                .presentacion(presentacion).unidadesPorPresentacion(unidades)
                .stockMinimo(20).enOferta(codigo.endsWith("001") || codigo.endsWith("005"))
                .subCategoria(sub)
                .imagen(crearImagenDemo(codigo + "-producto.png", TipoImagen.PRODUCTO,
                        new Color(197, 22, 91), nombre))
                .build());
        precioService.fijarPrecio(producto, new BigDecimal(precio), LocalDate.now().minusDays(30));
        if (stock > 0) {
            stockService.registrar(producto, TipoMovimientoStock.INGRESO, stock, null);
        }
    }

    /** BK-17: deja datos relacionados para recorrer los roles, el catÃ¡logo y las estadÃ­sticas en una base vacÃ­a. */
    private void cargarDatosBK17() {
        crearCasaCentralSiNoExiste();
        cargarSucursalesDemo();
        Preventista preventista = preventistaRepository.findByEliminadoFalse().stream()
                .filter(p -> p.getUsuario() != null
                        && "preventista.mza".equalsIgnoreCase(p.getUsuario().getNombreUsuario()))
                .findFirst().orElse(null);
        if (preventista == null) return;

        ClienteMinorista cliente = clienteRepository.findByEliminadoFalse().stream()
                .filter(c -> "Kiosco Demo BK17".equalsIgnoreCase(c.getNombreComercio()))
                .findFirst().orElseGet(() -> crearClienteDemo(preventista));
        asegurarUsuarioCliente(cliente);
        if (pedidoRepository.findByClienteIdAndEliminadoFalse(cliente.getId()).isEmpty()) {
            cargarPedidosDemo(cliente, preventista);
        }
        log.info("BK-17: datos demo listos (cuentas, sucursales, imÃ¡genes, estados y ventas de seis meses).");
    }

    private void cargarSucursalesDemo() {
        for (DulceMayor sucursal : dulceMayorRepository.findByEliminadoFalse()) {
            if (!"Casa Central".equalsIgnoreCase(sucursal.getNombre())
                    && !"Sucursal Godoy Cruz".equalsIgnoreCase(sucursal.getNombre())
                    && !"Sucursal Maipú".equalsIgnoreCase(sucursal.getNombre())) {
                continue;
            }
            if (sucursal.getImagenes() == null || sucursal.getImagenes().isEmpty()) {
                sucursal.setImagenes(new ArrayList<>(List.of(
                        crearImagenDemo("logo-" + slug(sucursal.getNombre()) + ".png", TipoImagen.LOGO_SUCURSAL,
                                new Color(197, 22, 91), sucursal.getNombre()),
                        crearImagenDemo("foto-" + slug(sucursal.getNombre()) + ".png", TipoImagen.FOTO_SUCURSAL,
                                new Color(27, 139, 126), "Dulce Mayor"))));
                dulceMayorRepository.save(sucursal);
            }
        }
        crearSucursalSiFalta("Sucursal Godoy Cruz", "30-70000000-1", "San MartÃ­n", "2150",
                "Godoy Cruz", "50035", "Godoy Cruz", -32.9180, -68.8420);
        crearSucursalSiFalta("Sucursal MaipÃº", "30-70000000-1", "Ozamis", "450",
                "MaipÃº", "5515", "MaipÃº", -32.9810, -68.7840);
    }

    private void crearSucursalSiFalta(String nombre, String cuit, String calle, String numero,
                                      String localidad, String cp, String departamento,
                                      double latitud, double longitud) {
        boolean existe = dulceMayorRepository.findByEliminadoFalse().stream()
                .anyMatch(d -> nombre.equalsIgnoreCase(d.getNombre()));
        if (existe) return;
        Direccion direccion = direccionRepository.save(Direccion.builder().calle(calle).numeracion(numero)
                .codigoPostal(cp).latitud(latitud).longitud(longitud)
                .provinciaId("50").provinciaNombre("Mendoza")
                .departamentoId(departamento).departamentoNombre(departamento)
                .localidadId(localidad).localidadNombre(localidad).build());
        DulceMayor sucursal = DulceMayor.builder().razonSocial("Dulce Mayor S.A.").cuit(cuit)
                .direccion(direccion).nombre(nombre).tipoEmpresa(TipoEmpresa.SUCURSAL)
                .aliasTransferencia("dulce.mayor.mza")
                .imagenes(new ArrayList<>(List.of(
                        crearImagenDemo("logo-" + slug(nombre) + ".png", TipoImagen.LOGO_SUCURSAL,
                                new Color(197, 22, 91), nombre),
                        crearImagenDemo("foto-" + slug(nombre) + ".png", TipoImagen.FOTO_SUCURSAL,
                                new Color(27, 139, 126), "Dulce Mayor"))))
                .build();
        dulceMayorRepository.save(sucursal);
    }

    private ClienteMinorista crearClienteDemo(Preventista preventista) {
        return comercio(preventista, "Kiosco Demo BK17", "TomÃ¡s", "Cliente Demo", "45999111",
                "San MartÃ­n", "1550", "Ciudad", "50007", "Capital", -32.8950, -68.8460);
    }

    private void asegurarUsuarioCliente(ClienteMinorista cliente) {
        ResponsableComercio responsable = cliente.getResponsable();
        if (responsable.getUsuario() != null) return;
        Usuario cuenta = usuarioRepository.findByNombreUsuarioIgnoreCase("cliente.demo")
                .orElseGet(() -> usuarioEmpleado("cliente.demo", Rol.CLIENTE));
        responsable.setUsuario(cuenta);
        responsableRepository.save(responsable);
    }

    private void cargarPedidosDemo(ClienteMinorista cliente, Preventista preventista) {
        Producto producto = productoRepository.findByCodigoIgnoreCase("DM-002")
                .filter(p -> !p.isEliminado()).orElse(null);
        if (producto == null) return;
        long disponible = stockService.stockActual(producto.getId());
        if (disponible < 100) {
            stockService.registrar(producto, TipoMovimientoStock.INGRESO,
                    Math.toIntExact(100 - disponible), null);
        }
        BigDecimal precio = precioService.precioVigenteObligatorio(producto, LocalDate.now());
        LocalDate hoy = LocalDate.now();
        for (int meses = 5; meses >= 0; meses--) {
            LocalDate fecha = hoy.minusMonths(meses).withDayOfMonth(10);
            crearPedidoDemo(cliente, preventista, producto, precio, EstadoPedido.ENTREGADO,
                    fecha.atTime(10, 0), TipoPago.TRANSFERENCIA);
            crearPedidoDemo(cliente, preventista, producto, precio, EstadoPedido.ENTREGADO,
                    fecha.atTime(15, 30), TipoPago.EFECTIVO);
        }
        crearPedidoDemo(cliente, preventista, producto, precio, EstadoPedido.CARGADO, hoy.atTime(9, 0), null);
        Producto sinStock = productoRepository.findByCodigoIgnoreCase("DM-008")
                .filter(p -> !p.isEliminado()).orElse(producto);
        BigDecimal precioSinStock = precioService.precioVigenteObligatorio(sinStock, hoy);
        crearPedidoDemo(cliente, preventista, sinStock, precioSinStock, EstadoPedido.PENDIENTE_STOCK,
                hoy.atTime(9, 30), null);
        crearPedidoDemo(cliente, preventista, producto, precio, EstadoPedido.PENDIENTE_PAGO,
                hoy.atTime(10, 0), TipoPago.BILLETERA_VIRTUAL);
        crearPedidoDemo(cliente, preventista, producto, precio, EstadoPedido.FACTURADO,
                hoy.atTime(10, 30), TipoPago.TRANSFERENCIA);
        crearPedidoDemo(cliente, preventista, producto, precio, EstadoPedido.EN_REPARTO,
                hoy.atTime(11, 0), TipoPago.EFECTIVO);
        crearPedidoDemo(cliente, preventista, producto, precio, EstadoPedido.ENTREGADO,
                hoy.atTime(11, 30), TipoPago.BILLETERA_VIRTUAL);
        crearPedidoDemo(cliente, preventista, producto, precio, EstadoPedido.ANULADO,
                hoy.atTime(12, 0), null);
    }

    private void crearPedidoDemo(ClienteMinorista cliente, Preventista preventista, Producto producto,
                                 BigDecimal precio, EstadoPedido estado, LocalDateTime fecha, TipoPago tipoPago) {
        int cantidad = 1;
        BigDecimal total = precio.multiply(BigDecimal.valueOf(cantidad));
        EstadoStockPedido estadoStock = estado == EstadoPedido.PENDIENTE_STOCK
                ? EstadoStockPedido.INSUFICIENTE
                : estado == EstadoPedido.CARGADO ? EstadoStockPedido.PENDIENTE : EstadoStockPedido.CONFIRMADO;
        Pedido pedido = Pedido.builder().fechaHoraCarga(fecha).estado(estado).estadoStock(estadoStock)
                .cliente(cliente).preventista(preventista).subtotal(total).descuento(BigDecimal.ZERO).total(total)
                .items(new ArrayList<>()).historial(new ArrayList<>()).build();
        pedido.getItems().add(PedidoItem.builder().pedido(pedido).producto(producto).cantidad(cantidad)
                .precioUnitario(precio).subtotal(total).build());
        agregarHistorialDemo(pedido, fecha, estado);
        Pedido guardado = pedidoRepository.save(pedido);

        boolean facturado = estado == EstadoPedido.FACTURADO || estado == EstadoPedido.EN_REPARTO
                || estado == EstadoPedido.ENTREGADO || estado == EstadoPedido.ANULADO;
        FacturaCliente factura = null;
        if (facturado) {
            EstadoFactura estadoFactura = estado == EstadoPedido.ANULADO ? EstadoFactura.ANULADA
                    : tipoPago == null ? EstadoFactura.EMITIDA : EstadoFactura.PAGADA;
            factura = FacturaCliente.builder().numeroFactura(facturaRepository.ultimoNumero() + 1)
                    .fechaFactura(fecha).estado(estadoFactura).subtotal(total).descuento(BigDecimal.ZERO)
                    .total(total).pedido(guardado).detalles(new ArrayList<>()).build();
            factura.getDetalles().add(DetalleFactura.builder().factura(factura).producto(producto)
                    .cantidad(cantidad).precioUnitario(precio).subtotal(total).build());
            factura = facturaRepository.save(factura);
            if (estadoFactura != EstadoFactura.ANULADA) {
                stockService.registrar(producto, TipoMovimientoStock.VENTA, -cantidad,
                        factura.getDetalles().get(0));
            }
        }
        if (tipoPago != null) {
            EstadoPago estadoPago = estado == EstadoPedido.PENDIENTE_PAGO ? EstadoPago.PENDIENTE
                    : estado == EstadoPedido.ANULADO ? EstadoPago.ANULADO : EstadoPago.CONFIRMADO;
            pagoRepository.save(Pago.builder().pedido(guardado).factura(factura).tipoPago(tipoPago)
                    .estado(estadoPago).monto(total)
                    .fechaPago(estadoPago == EstadoPago.CONFIRMADO ? fecha : null).build());
        }
    }

    private void agregarHistorialDemo(Pedido pedido, LocalDateTime fecha, EstadoPedido estado) {
        List<EstadoPedido> camino = switch (estado) {
            case CARGADO -> List.of(EstadoPedido.CARGADO);
            case PENDIENTE_STOCK -> List.of(EstadoPedido.CARGADO, EstadoPedido.PENDIENTE_STOCK);
            case PENDIENTE_PAGO -> List.of(EstadoPedido.CARGADO, EstadoPedido.PENDIENTE_PAGO);
            case FACTURADO -> List.of(EstadoPedido.CARGADO, EstadoPedido.PENDIENTE_PAGO, EstadoPedido.FACTURADO);
            case EN_REPARTO -> List.of(EstadoPedido.CARGADO, EstadoPedido.PENDIENTE_PAGO,
                    EstadoPedido.FACTURADO, EstadoPedido.EN_REPARTO);
            case ENTREGADO -> List.of(EstadoPedido.CARGADO, EstadoPedido.PENDIENTE_PAGO,
                    EstadoPedido.FACTURADO, EstadoPedido.EN_REPARTO, EstadoPedido.ENTREGADO);
            case ANULADO -> List.of(EstadoPedido.CARGADO, EstadoPedido.ANULADO);
        };
        for (int i = 0; i < camino.size(); i++) {
            pedido.getHistorial().add(HistorialEstadoPedido.builder().pedido(pedido)
                    .estadoAnterior(i == 0 ? null : camino.get(i - 1)).estadoNuevo(camino.get(i))
                    .fechaHora(fecha.plusMinutes(i * 5L)).usuario("sistema")
                    .observacion("BK-17 datos de demostraciÃ³n").build());
        }
    }

    private Imagen crearImagenDemo(String nombre, TipoImagen tipo, Color color, String texto) {
        BufferedImage lienzo = new BufferedImage(640, 420, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = lienzo.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(250, 238, 244));
        g.fillRect(0, 0, lienzo.getWidth(), lienzo.getHeight());
        g.setColor(color);
        g.fillRoundRect(24, 24, 592, 372, 38, 38);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        g.drawString("DULCE MAYOR", 58, 105);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        String etiqueta = texto == null ? "Producto demo" : texto;
        if (etiqueta.length() > 30) etiqueta = etiqueta.substring(0, 27) + "...";
        g.drawString(etiqueta, 58, 170);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        g.drawString("Imagen ilustrativa de demostraciÃ³n", 58, 345);
        g.dispose();
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            ImageIO.write(lienzo, "png", salida);
            return imagenRepository.save(Imagen.builder().nombre(nombre).mime("image/png")
                    .contenido(salida.toByteArray()).tipoImagen(tipo).build());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar una imagen de demostraciÃ³n.", e);
        }
    }

    private String slug(String texto) {
        return texto.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
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
