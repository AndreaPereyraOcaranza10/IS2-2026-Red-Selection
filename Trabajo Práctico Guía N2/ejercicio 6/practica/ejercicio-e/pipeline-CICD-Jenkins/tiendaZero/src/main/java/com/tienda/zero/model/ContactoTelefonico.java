package com.tienda.zero.model;

import com.tienda.zero.enums.TipoTelefono;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "contacto_telefonico")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@DiscriminatorValue("TELEFONO")
public class ContactoTelefonico extends Contacto{

    @Column(nullable = false)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTelefono tipoTelefono;

}
