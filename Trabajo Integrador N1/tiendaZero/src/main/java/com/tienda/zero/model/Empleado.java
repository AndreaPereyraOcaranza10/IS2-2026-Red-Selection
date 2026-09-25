package com.tienda.zero.model;

import com.tienda.zero.enums.TipoEmpleado;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "empleado")
@DiscriminatorValue("EMPLEADO")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Empleado extends Persona {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEmpleado tipoEmpleado;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
}