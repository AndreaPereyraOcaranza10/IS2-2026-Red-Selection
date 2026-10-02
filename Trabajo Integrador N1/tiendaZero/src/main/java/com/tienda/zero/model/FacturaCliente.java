package com.tienda.zero.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "factura_cliente")
@DiscriminatorValue("CLIENTE")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class FacturaCliente extends Factura {
    @OneToOne
    @JoinColumn(name = "orden_compra_id", nullable = false, unique = true)
    private OrdenCompra ordenCompra;
    @Column(nullable = false, length = 100) private String nombreCliente;
    @Column(nullable = false, length = 100) private String apellidoCliente;
    @Column(length = 50) private String documentoCliente;
    @Column(nullable = false, length = 250) private String domicilioCliente;
    @Column(length = 150) private String correoCliente;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean correoPendiente = false;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean correoEnviado = false;

    @Override
    public int signoStock() { return -1; }
}
