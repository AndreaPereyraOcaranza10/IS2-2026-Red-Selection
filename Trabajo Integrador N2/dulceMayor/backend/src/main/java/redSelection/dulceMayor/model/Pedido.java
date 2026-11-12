package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import redSelection.dulceMayor.enums.*;
import redSelection.dulceMayor.patrones.state.EstadoPedidoState;
import redSelection.dulceMayor.patrones.state.EstadosPedido;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Pedido extends Base {

    @Column(nullable = false)
    private LocalDateTime fechaHoraCarga;

    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default
    private EstadoPedido estado = EstadoPedido.CARGADO;

    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default
    private EstadoStockPedido estadoStock = EstadoStockPedido.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteMinorista cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "preventista_id", nullable = false)
    private Preventista preventista;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "promocion_id")
    private Promocion promocion;

    @Column(nullable = false, precision = 12, scale = 2) @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2) @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2) @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PedidoItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaHora ASC")
    @Builder.Default
    private List<HistorialEstadoPedido> historial = new ArrayList<>();

    // =====================================================
    // CAMBIOS DE ESTADO (patrón State)
    // Usar estos métodos en lugar de setEstado: validan la transición y la registran en el historial.
    // =====================================================

    public EstadoPedidoState estadoActual() {
        return EstadosPedido.de(estado);
    }

    /** Primer registro del historial: el pedido quedó CARGADO. */
    public void registrarAlta(String usuario) {
        historial.add(HistorialEstadoPedido.builder().pedido(this).estadoAnterior(null).estadoNuevo(estado)
                .fechaHora(fechaHoraCarga != null ? fechaHoraCarga : LocalDateTime.now())
                .usuario(usuario).observacion("Pedido cargado").build());
    }

    public void esperarPago(String usuario, String observacion) {
        aplicar(estadoActual().esperarPago(), usuario, observacion);
    }

    public void facturar(String usuario, String observacion) {
        aplicar(estadoActual().facturar(), usuario, observacion);
    }

    public void quedarPendienteDeStock(String usuario, String observacion) {
        aplicar(estadoActual().quedarPendienteDeStock(), usuario, observacion);
    }

    public void despachar(String usuario, String observacion) {
        aplicar(estadoActual().despachar(), usuario, observacion);
    }

    public void entregar(String usuario, String observacion) {
        aplicar(estadoActual().entregar(), usuario, observacion);
    }

    public void anular(String usuario, String observacion) {
        aplicar(estadoActual().anular(), usuario, observacion);
    }

    /** Para el endpoint genérico de cambio de estado: traduce el destino pedido a la acción correspondiente. */
    public void avanzarA(EstadoPedido destino, String usuario, String observacion) {
        switch (destino) {
            case PENDIENTE_PAGO -> esperarPago(usuario, observacion);
            case FACTURADO -> facturar(usuario, observacion);
            case PENDIENTE_STOCK -> quedarPendienteDeStock(usuario, observacion);
            case EN_REPARTO -> despachar(usuario, observacion);
            case ENTREGADO -> entregar(usuario, observacion);
            case ANULADO -> anular(usuario, observacion);
            case CARGADO -> throw estadoActual().transicionInvalida(destino);
        }
    }

    private void aplicar(EstadoPedido nuevo, String usuario, String observacion) {
        historial.add(HistorialEstadoPedido.builder().pedido(this).estadoAnterior(estado).estadoNuevo(nuevo)
                .fechaHora(LocalDateTime.now()).usuario(usuario).observacion(observacion).build());
        estado = nuevo;
    }
}