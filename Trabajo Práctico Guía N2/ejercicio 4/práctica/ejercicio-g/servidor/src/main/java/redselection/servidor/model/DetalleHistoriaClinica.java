package redselection.servidor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "detalle_historia_clinica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleHistoriaClinica extends Base {

    @Column(name = "fecha_historia")
    private LocalDate fechaHistoria;

    @Column(name = "detalle_historia")
    private String detalleHistoria;

    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;

    @JsonIgnore // evita el bucle infinito historia -> detalle -> historia al serializar
    @ManyToOne
    @JoinColumn(name = "historia_clinica_id")
    private HistoriaClinica historiaClinica;
}