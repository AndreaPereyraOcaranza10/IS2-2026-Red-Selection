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

    @Override
    public int signoStock() {
        return 1;
    }
}
