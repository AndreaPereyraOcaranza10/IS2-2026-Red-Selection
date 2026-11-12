package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.repository.ClienteMinoristaRepository;
import redSelection.dulceMayor.repository.ProductoRepository;
import redSelection.dulceMayor.repository.VigenciaPrecioRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/** Lee y materializa los datos del newsletter antes de hacer llamadas SMTP. */
@Service
public class NewsletterConsultaService {

    private final ProductoRepository productoRepository;
    private final VigenciaPrecioRepository precioRepository;
    private final StockService stockService;
    private final ClienteMinoristaRepository clienteRepository;

    public NewsletterConsultaService(ProductoRepository productoRepository,
                                     VigenciaPrecioRepository precioRepository,
                                     StockService stockService,
                                     ClienteMinoristaRepository clienteRepository) {
        this.productoRepository = productoRepository;
        this.precioRepository = precioRepository;
        this.stockService = stockService;
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public DatosNewsletter obtener(LocalDate fecha) {
        List<Producto> productosOferta = productoRepository.findByEnOfertaTrueAndEliminadoFalse();
        Set<String> idsOferta = productosOferta.stream().map(Producto::getId).collect(java.util.stream.Collectors.toSet());
        Map<String, Long> stocks = stockService.stockPorProducto();

        Map<String, BigDecimal> precios = new HashMap<>();
        for (VigenciaPrecio vigencia : precioRepository.vigentesEn(fecha)) {
            if (idsOferta.contains(vigencia.getProducto().getId())) {
                precios.putIfAbsent(vigencia.getProducto().getId(), vigencia.getPrecio());
            }
        }

        List<Oferta> ofertas = productosOferta.stream()
                .map(producto -> {
                    long stock = stocks.getOrDefault(producto.getId(), 0L);
                    BigDecimal precio = precios.get(producto.getId());
                    return stock > 0 && precio != null
                            ? new Oferta(producto.getNombre(), producto.getMarca(), precio, stock)
                            : null;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Oferta::nombre, String.CASE_INSENSITIVE_ORDER))
                .toList();

        Map<String, Destinatario> unicos = new LinkedHashMap<>();
        int sinCorreo = 0;
        for (ClienteMinorista cliente : clienteRepository.findByEliminadoFalse()) {
            ResponsableComercio responsable = cliente.getResponsable();
            Optional<ContactoCorreoElectronico> contacto = responsable == null || responsable.isEliminado()
                    ? Optional.empty()
                    : responsable.getContactos().stream()
                    .filter(c -> c instanceof ContactoCorreoElectronico && !c.isEliminado())
                    .map(c -> (ContactoCorreoElectronico) c)
                    .filter(c -> c.getEmail() != null && !c.getEmail().isBlank())
                    .sorted((a, b) -> Boolean.compare(b.isPrincipal(), a.isPrincipal()))
                    .findFirst();
            if (contacto.isEmpty()) {
                sinCorreo++;
                continue;
            }
            String correo = contacto.get().getEmail().trim();
            String nombre = String.join(" ", valor(responsable.getNombre()), valor(responsable.getApellido())).trim();
            unicos.putIfAbsent(correo.toLowerCase(Locale.ROOT), new Destinatario(correo, nombre));
        }
        return new DatosNewsletter(fecha, ofertas, List.copyOf(unicos.values()), sinCorreo);
    }

    private static String valor(String texto) { return texto == null ? "" : texto; }

    public record Oferta(String nombre, String marca, BigDecimal precio, long stock) { }
    public record Destinatario(String correo, String nombre) { }
    public record DatosNewsletter(LocalDate fecha, List<Oferta> ofertas,
                                  List<Destinatario> destinatarios, int clientesSinCorreo) { }
}
