package com.tienda.zero.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "contacto_correo_electronico")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@DiscriminatorValue("CORREO_ELECTRONICO")
public class ContactoCorreoElectronico extends Contacto{

    @Column(nullable = false)
    private String email;
}
