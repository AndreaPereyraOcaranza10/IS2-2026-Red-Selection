package redSelection.dulceMayor.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.dto.*;
import redSelection.dulceMayor.enums.*;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.patrones.facade.NotificacionFacade;
import redSelection.dulceMayor.patrones.strategy.OptimizadorRuta;
import redSelection.dulceMayor.patrones.strategy.PlanOptimizado;
import redSelection.dulceMayor.patrones.strategy.PuntoRuta;
import redSelection.dulceMayor.repository.*;
import redSelection.dulceMayor.security.UsuarioActual;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Rutas diarias de preventa y de reparto.
 * No extiende BaseService a propósito: la ruta no es un recurso con CRUD genérico, sino un caso de uso
 * (armar la ruta del día, registrar visitas/entregas, reordenar). Sí reutiliza las excepciones y el
 * manejador global de errores.
 * Usa Strategy (OptimizadorRuta) para ordenar y la Facade de notificaciones para avisar al próximo comercio.
 */
@Service
public class RutaService {

    private final RutaDiariaRepository rutaRepository;
    private final ParadaRutaRepository paradaRepository;
    private final PreventistaRepository preventistaRepository;
    private final TransportistaRepository transportistaRepository;
    private final ClienteMinoristaRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final EntregaRepository entregaRepository;
    private final DulceMayorRepository dulceMayorRepository;
    private final OptimizadorRuta optimizador;          // por calles (OSRM) con respaldo
    private final OptimizadorRuta ordenadorLocal;       // línea recta, sin red: orden inicial de una ruta nueva
    private final NotificacionFacade notificaciones;
    private final UsuarioActual usuarioActual;

    public RutaService(RutaDiariaRepository rutaRepository,
                       ParadaRutaRepository paradaRepository,
                       PreventistaRepository preventistaRepository,
                       TransportistaRepository transportistaRepository,
                       ClienteMinoristaRepository clienteRepository,
                       PedidoRepository pedidoRepository,
                       EntregaRepository entregaRepository,
                       DulceMayorRepository dulceMayorRepository,
                       OptimizadorRuta optimizador,
                       @Qualifier("vecinoMasCercanoOptimizador") OptimizadorRuta ordenadorLocal,
                       NotificacionFacade notificaciones,
                       UsuarioActual usuarioActual) {
        this.rutaRepository = rutaRepository;
        this.paradaRepository = paradaRepository;
        this.preventistaRepository = preventistaRepository;
        this.transportistaRepository = transportistaRepository;
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
        this.entregaRepository = entregaRepository;
        this.dulceMayorRepository = dulceMayorRepository;
        this.optimizador = optimizador;
        this.ordenadorLocal = ordenadorLocal;
        this.notificaciones = notificaciones;
        this.usuarioActual = usuarioActual;
    }

    // =====================================================
    // RUTA DE PREVENTA
    // =====================================================

    /** Ruta de hoy del preventista: si no existe, se arma con los comercios que tiene asignados. */
    @Transactional
    public RutaDTO rutaDePreventista(String preventistaId) {
        Preventista preventista = preventistaRepository.findByIdAndEliminadoFalse(preventistaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Preventista", preventistaId));
        LocalDate hoy = LocalDate.now();
        RutaDiaria ruta = rutaRepository
                .findByResponsableIdAndFechaAndTipoRuta(preventistaId, hoy, TipoRuta.PREVENTA)
                .orElseGet(() -> armarRutaDePreventa(preventista, hoy));
        return aDTO(ruta);
    }

    private RutaDiaria armarRutaDePreventa(Preventista preventista, LocalDate fecha) {
        RutaDiaria ruta = RutaDiaria.builder()
                .fecha(fecha)
                .tipoRuta(TipoRuta.PREVENTA)
                .responsable(preventista)
                .build();
        List<ClienteMinorista> clientes =
                clienteRepository.findByPreventistaAsignadoIdAndEliminadoFalse(preventista.getId());
        for (ClienteMinorista cliente : ordenInicial(clientes)) {
            agregarParada(ruta, cliente, null);
        }
        return rutaRepository.save(ruta);
    }

    /**
     * Tomar un pedido cuenta como visita: si el comercio es una parada pendiente de la ruta de hoy del
     * preventista, se cierra. Si no está en la ruta (pedido fuera de recorrido), no hace nada.
     */
    @Transactional
    public void registrarVisitaPorPedido(String preventistaId, String clienteId) {
        rutaRepository.findByResponsableIdAndFechaAndTipoRuta(preventistaId, LocalDate.now(), TipoRuta.PREVENTA)
                .ifPresent(ruta -> pendientes(ruta).stream()
                        .filter(p -> p.getCliente().getId().equals(clienteId))
                        .findFirst()
                        .ifPresent(parada -> {
                            cerrarParada(parada);
                            ruta.setEstado(primeraPendiente(ruta).isPresent() ? EstadoRuta.EN_CURSO : EstadoRuta.FINALIZADA);
                        }));
    }

    /** Registra que el preventista visitó el comercio. Cierra la ruta cuando no quedan paradas pendientes. */
    @Transactional
    public ParadaRutaDTO registrarVisita(String paradaId) {
        ParadaRuta parada = buscarParada(paradaId);
        RutaDiaria ruta = parada.getRuta();
        if (ruta.getTipoRuta() != TipoRuta.PREVENTA) {
            throw new ReglaNegocioException("Esta parada es de reparto: se registra como entrega.");
        }
        cerrarParada(parada);
        ruta.setEstado(primeraPendiente(ruta).isPresent() ? EstadoRuta.EN_CURSO : EstadoRuta.FINALIZADA);
        return aDTO(parada);
    }

    // =====================================================
    // RUTA DE REPARTO
    // =====================================================

    /**
     * Ruta de hoy del transportista. Cada consulta toma los pedidos FACTURADOS que nadie despachó,
     * los pasa a EN_REPARTO, crea su Entrega y los suma como paradas al final de la ruta.
     */
    @Transactional
    public RutaDTO rutaDeTransportista(String transportistaId) {
        Transportista transportista = transportistaRepository.findByIdAndEliminadoFalse(transportistaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Transportista", transportistaId));
        LocalDate hoy = LocalDate.now();
        RutaDiaria ruta = rutaRepository
                .findByResponsableIdAndFechaAndTipoRuta(transportistaId, hoy, TipoRuta.REPARTO)
                .orElseGet(() -> rutaRepository.save(RutaDiaria.builder()
                        .fecha(hoy)
                        .tipoRuta(TipoRuta.REPARTO)
                        .responsable(transportista)
                        .build()));
        despacharFacturados(ruta, transportista);
        return aDTO(ruta);
    }

    /**
     * Un comercio recién dado de alta se suma como parada pendiente a la ruta de hoy del preventista.
     * Si hoy todavía no consultó su ruta no hace falta nada: se arma sola con los comercios asignados.
     */
    @Transactional
    public void incorporarARutaDeHoy(String preventistaId, ClienteMinorista cliente) {
        rutaRepository.findByResponsableIdAndFechaAndTipoRuta(preventistaId, LocalDate.now(), TipoRuta.PREVENTA)
                .ifPresent(ruta -> {
                    agregarParada(ruta, cliente, null);
                    if (ruta.getEstado() == EstadoRuta.FINALIZADA) {
                        ruta.setEstado(EstadoRuta.EN_CURSO);   // volvió a tener una parada pendiente
                    }
                    rutaRepository.save(ruta);
                });
    }

    private void despacharFacturados(RutaDiaria ruta, Transportista transportista) {
        List<Pedido> facturados = pedidoRepository.findByEstadoOrderByFechaHoraCargaAsc(EstadoPedido.FACTURADO);
        if (facturados.isEmpty()) {
            return;
        }
        LocalDateTime ahora = LocalDateTime.now();
        Map<String, Pedido> porCliente = new LinkedHashMap<>();
        for (Pedido pedido : facturados) {
            porCliente.putIfAbsent(pedido.getCliente().getId(), pedido);
        }
        // Un comercio con más de un pedido facturado se atiende en una sola parada: el resto queda para la próxima consulta.
        List<ClienteMinorista> clientes = porCliente.values().stream().map(Pedido::getCliente).toList();
        String usuario = usuarioActual.nombreOSistema();
        for (ClienteMinorista cliente : ordenInicial(clientes)) {
            Pedido pedido = porCliente.get(cliente.getId());
            agregarParada(ruta, cliente, pedido);
            pedido.despachar(usuario, "Asignado a la ruta de reparto del " + ruta.getFecha());
            Entrega entrega = entregaRepository.findByPedidoId(pedido.getId())
                    .orElseGet(() -> Entrega.builder().pedido(pedido).build());
            entrega.setTransportista(transportista);
            entrega.setFechaDespacho(ahora);
            entregaRepository.save(entrega);
        }
        if (ruta.getEstado() == EstadoRuta.FINALIZADA) {
            ruta.setEstado(EstadoRuta.PLANIFICADA);   // llegaron pedidos nuevos: la ruta se reabre
        }
        rutaRepository.save(ruta);
    }

    /**
     * Registra la entrega: cierra la parada, pasa el pedido a ENTREGADO y avisa por correo al comercio
     * que sigue. Si el correo falla, la entrega igual queda registrada (correoEnviado = false).
     */
    @Transactional
    public EntregaRealizadaDTO registrarEntrega(String paradaId) {
        ParadaRuta parada = buscarParada(paradaId);
        RutaDiaria ruta = parada.getRuta();
        if (ruta.getTipoRuta() != TipoRuta.REPARTO) {
            throw new ReglaNegocioException("Esta parada es de preventa: se registra como visita.");
        }
        Pedido pedido = parada.getPedido();
        if (pedido == null) {
            throw new ReglaNegocioException("Esta parada no tiene un pedido para entregar.");
        }
        pedido.entregar(usuarioActual.nombreOSistema(), "Entregado al comercio");   // el State rechaza si no está EN_REPARTO
        LocalDateTime ahora = cerrarParada(parada);
        entregaRepository.findByPedidoId(pedido.getId()).ifPresent(e -> e.setFechaEntrega(ahora));

        Optional<ParadaRuta> siguiente = primeraPendiente(ruta);
        boolean correoEnviado = false;
        if (siguiente.isPresent()) {
            ruta.setEstado(EstadoRuta.EN_CURSO);
            correoEnviado = notificaciones.avisarProximaEntrega(siguiente.get());
        } else {
            ruta.setEstado(EstadoRuta.FINALIZADA);
        }
        return new EntregaRealizadaDTO(aDTO(parada), siguiente.map(p -> aDTO(p)).orElse(null), correoEnviado);
    }

    // =====================================================
    // ORDEN DE LA RUTA (común a ambas)
    // =====================================================

    /** Orden manual: debe venir exactamente el conjunto de paradas pendientes. */
    @Transactional
    public RutaDTO reordenar(String rutaId, OrdenarRutaDTO dto) {
        RutaDiaria ruta = buscarRuta(rutaId);
        Map<String, ParadaRuta> pendientes = pendientes(ruta).stream()
                .collect(Collectors.toMap(ParadaRuta::getId, p -> p, (a, b) -> a, LinkedHashMap::new));
        Set<String> recibidas = new HashSet<>(dto.paradaIds());
        if (recibidas.size() != dto.paradaIds().size() || !recibidas.equals(pendientes.keySet())) {
            throw new ReglaNegocioException("Hay que enviar todas las paradas pendientes, sin repetir ni agregar otras.");
        }
        renumerar(ruta, dto.paradaIds().stream().map(pendientes::get).toList());
        return aDTO(ruta);
    }

    /**
     * Ordena las paradas pendientes con la estrategia de optimización (por calles, con respaldo en línea recta)
     * y devuelve la ruta ya reordenada junto con el recorrido para dibujarlo. El origen es, en este orden:
     * la ubicación enviada, el último comercio atendido o el depósito central. Las paradas sin coordenadas
     * quedan al final, en su orden actual.
     */
    @Transactional
    public PlanRutaDTO optimizar(String rutaId, OptimizarRutaDTO dto) {
        RutaDiaria ruta = buscarRuta(rutaId);
        List<ParadaRuta> pendientes = pendientes(ruta);
        List<ParadaRuta> conCoordenadas = pendientes.stream().filter(p -> puntoDeParada(p).isPresent()).toList();
        List<ParadaRuta> sinCoordenadas = pendientes.stream().filter(p -> puntoDeParada(p).isEmpty()).toList();

        PuntoRuta origen = origenDe(ruta, dto);
        List<PuntoRuta> puntos = conCoordenadas.stream().map(p -> puntoDeParada(p).orElseThrow()).toList();
        Map<String, ParadaRuta> porId = conCoordenadas.stream()
                .collect(Collectors.toMap(ParadaRuta::getId, p -> p));

        PlanOptimizado plan = optimizador.ordenar(origen, puntos);

        List<ParadaRuta> nuevoOrden = new ArrayList<>();
        for (String id : plan.orden()) {
            nuevoOrden.add(porId.get(id));
        }
        nuevoOrden.addAll(sinCoordenadas);
        renumerar(ruta, nuevoOrden);
        return new PlanRutaDTO(aDTO(ruta), Math.round(plan.distanciaKm() * 10.0) / 10.0, plan.duracionMin(),
                plan.geometria(), plan.fuente());
    }

    // =====================================================
    // AUXILIARES
    // =====================================================

    private RutaDiaria buscarRuta(String id) {
        return rutaRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta", id));
    }

    private ParadaRuta buscarParada(String id) {
        return paradaRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Parada", id));
    }

    private LocalDateTime cerrarParada(ParadaRuta parada) {
        if (parada.getEstado() != EstadoParada.PENDIENTE) {
            throw new ReglaNegocioException("La parada ya fue registrada.");
        }
        LocalDateTime ahora = LocalDateTime.now();
        parada.setEstado(EstadoParada.COMPLETADA);
        parada.setFechaHora(ahora);
        return ahora;
    }

    private void agregarParada(RutaDiaria ruta, ClienteMinorista cliente, Pedido pedido) {
        ruta.getParadas().add(ParadaRuta.builder()
                .ruta(ruta)
                .orden(ruta.getParadas().size() + 1)
                .cliente(cliente)
                .pedido(pedido)
                .build());
    }

    private List<ParadaRuta> pendientes(RutaDiaria ruta) {
        return ruta.getParadas().stream()
                .filter(p -> !p.isEliminado() && p.getEstado() == EstadoParada.PENDIENTE)
                .sorted(Comparator.comparingInt(ParadaRuta::getOrden))
                .toList();
    }

    private Optional<ParadaRuta> primeraPendiente(RutaDiaria ruta) {
        return pendientes(ruta).stream().findFirst();
    }

    /** Las atendidas conservan su lugar al principio; las pendientes siguen en el orden indicado. */
    private void renumerar(RutaDiaria ruta, List<ParadaRuta> pendientesEnOrden) {
        List<ParadaRuta> resto = ruta.getParadas().stream()
                .filter(p -> !p.isEliminado() && p.getEstado() != EstadoParada.PENDIENTE)
                .sorted(Comparator.comparingInt(ParadaRuta::getOrden))
                .collect(Collectors.toCollection(ArrayList::new));
        resto.addAll(pendientesEnOrden);
        for (int i = 0; i < resto.size(); i++) {
            resto.get(i).setOrden(i + 1);
        }
    }

    /** Orden razonable al armar una ruta nueva: se optimiza desde el depósito; sin coordenadas, al final. */
    private List<ClienteMinorista> ordenInicial(List<ClienteMinorista> clientes) {
        Map<String, ClienteMinorista> conCoordenadas = new LinkedHashMap<>();
        List<ClienteMinorista> sinCoordenadas = new ArrayList<>();
        for (ClienteMinorista cliente : clientes) {
            if (puntoDeCliente(cliente).isPresent()) {
                conCoordenadas.put(cliente.getId(), cliente);
            } else {
                sinCoordenadas.add(cliente);
            }
        }
        List<PuntoRuta> puntos = conCoordenadas.values().stream().map(c -> puntoDeCliente(c).orElseThrow()).toList();
        List<ClienteMinorista> ordenados = new ArrayList<>();
        for (String id : ordenadorLocal.ordenar(depositoCentral().orElse(null), puntos).orden()) {
            ordenados.add(conCoordenadas.get(id));
        }
        ordenados.addAll(sinCoordenadas);
        return ordenados;
    }

    private PuntoRuta origenDe(RutaDiaria ruta, OptimizarRutaDTO dto) {
        if (dto != null && dto.latitud() != null && dto.longitud() != null) {
            return new PuntoRuta("origen", dto.latitud(), dto.longitud());
        }
        Optional<PuntoRuta> ultimaAtendida = ruta.getParadas().stream()
                .filter(p -> !p.isEliminado() && p.getEstado() == EstadoParada.COMPLETADA && p.getFechaHora() != null)
                .max(Comparator.comparing(ParadaRuta::getFechaHora))
                .flatMap(this::puntoDeParada);
        return ultimaAtendida.or(this::depositoCentral).orElse(null);
    }

    private Optional<PuntoRuta> depositoCentral() {
        return dulceMayorRepository.findByEliminadoFalse().stream()
                .filter(d -> d.getTipoEmpresa() == TipoEmpresa.SEDE_CENTRAL)
                .findFirst()
                .flatMap(d -> puntoDe("deposito", d.getDireccion()));
    }

    private Optional<PuntoRuta> puntoDeParada(ParadaRuta parada) {
        return puntoDe(parada.getId(), parada.getCliente().getDireccion());
    }

    private Optional<PuntoRuta> puntoDeCliente(ClienteMinorista cliente) {
        return puntoDe(cliente.getId(), cliente.getDireccion());
    }

    private Optional<PuntoRuta> puntoDe(String id, Direccion direccion) {
        if (direccion == null || direccion.getLatitud() == null || direccion.getLongitud() == null) {
            return Optional.empty();
        }
        return Optional.of(new PuntoRuta(id, direccion.getLatitud(), direccion.getLongitud()));
    }

    // =====================================================
    // CONVERSIÓN A DTO
    // =====================================================

    private RutaDTO aDTO(RutaDiaria ruta) {
        List<ParadaRuta> ordenadas = ruta.getParadas().stream()
                .filter(p -> !p.isEliminado())
                .sorted(Comparator.comparingInt(ParadaRuta::getOrden))
                .toList();
        List<ParadaRutaDTO> paradas = new ArrayList<>();
        PuntoRuta anterior = depositoCentral().orElse(null);
        for (ParadaRuta parada : ordenadas) {
            Optional<PuntoRuta> punto = puntoDeParada(parada);
            Double km = null;
            if (punto.isPresent()) {
                if (anterior != null) {
                    km = Math.round(anterior.distanciaKm(punto.get()) * 10.0) / 10.0;
                }
                anterior = punto.get();
            }
            paradas.add(aDTO(parada, km));
        }
        return new RutaDTO(ruta.getId(), ruta.getFecha(), ruta.getTipoRuta(), ruta.getEstado(),
                kmRestantes(ruta), paradas);
    }

    private double kmRestantes(RutaDiaria ruta) {
        PuntoRuta anterior = origenDe(ruta, null);
        double total = 0;
        for (ParadaRuta parada : pendientes(ruta)) {
            Optional<PuntoRuta> punto = puntoDeParada(parada);
            if (punto.isEmpty()) {
                continue;
            }
            if (anterior != null) {
                total += anterior.distanciaKm(punto.get());
            }
            anterior = punto.get();
        }
        return Math.round(total * 10.0) / 10.0;
    }

    private ParadaRutaDTO aDTO(ParadaRuta parada) {
        return aDTO(parada, null);
    }

    private ParadaRutaDTO aDTO(ParadaRuta parada, Double kmDesdeAnterior) {
        ClienteMinorista cliente = parada.getCliente();
        ResponsableComercio responsable = cliente.getResponsable();
        Direccion direccion = cliente.getDireccion();
        return new ParadaRutaDTO(
                parada.getId(),
                parada.getOrden(),
                cliente.getId(),
                cliente.getNombreComercio(),
                responsable.getNombre() + " " + responsable.getApellido(),
                telefonoPrincipal(responsable),
                direccion.getCalle() + " " + direccion.getNumeracion() + ", " + direccion.getLocalidadNombre(),
                direccion.getLatitud(),
                direccion.getLongitud(),
                parada.getEstado(),
                parada.getPedido() != null ? parada.getPedido().getId() : null,
                parada.getFechaHora(),
                kmDesdeAnterior,
                parada.getPedido() != null ? parada.getPedido().getTotal() : null);
    }

    private String telefonoPrincipal(Persona persona) {
        return persona.getContactos().stream()
                .filter(c -> c instanceof ContactoTelefonico && !c.isEliminado())
                .map(c -> (ContactoTelefonico) c)
                .sorted((a, b) -> Boolean.compare(b.isPrincipal(), a.isPrincipal()))
                .map(ContactoTelefonico::getTelefono)
                .findFirst()
                .orElse(null);
    }
}
