package redSelection.dulceMayor.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/*
 Dulce Mayor (central y sucursales) y los comercios clientes son empresas.
 El CUIT no es único en la base porque las sucursales comparten el de Dulce Mayor;
 la unicidad entre clientes se valida en el service.
 */
@Entity @Table(name = "empresa")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public abstract class Empresa extends Base {

    @Column(nullable = false)
    private String razonSocial;

    @Column(nullable = false)
    private String cuit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "direccion_id", nullable = false)
    private Direccion direccion;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "empresa_id")
    @Builder.Default
    private List<Contacto> contactos = new ArrayList<>();
}

