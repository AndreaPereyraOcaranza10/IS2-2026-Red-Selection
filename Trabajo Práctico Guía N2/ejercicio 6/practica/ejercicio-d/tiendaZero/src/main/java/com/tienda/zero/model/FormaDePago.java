package com.tienda.zero.model;

import com.tienda.zero.enums.TipoPago;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table (name = "forma_de_pago")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FormaDePago {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID )
    private String id;

    @Column
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column (nullable = false)
    private TipoPago tipoPago;

    @Column
    private String observacion;

    @Builder.Default
    @Column (nullable = false)
    private boolean eliminado = false;

}
