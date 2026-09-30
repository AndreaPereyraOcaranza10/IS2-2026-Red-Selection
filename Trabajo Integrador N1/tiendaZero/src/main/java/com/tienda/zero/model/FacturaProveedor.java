package com.tienda.zero.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "factura_proveedor")
@DiscriminatorValue("PROVEEDOR")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class FacturaProveedor extends Factura {

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    // Opcional: la orden de compra que originó esta factura. Una factura también se puede
    // cargar sin orden previa, por eso admite null.
    @ManyToOne
    @JoinColumn(name = "orden_compra_proveedor_id")
    private OrdenCompraProveedor ordenCompra;

    @Override
    public int signoStock() {
        return 1;
    }
}