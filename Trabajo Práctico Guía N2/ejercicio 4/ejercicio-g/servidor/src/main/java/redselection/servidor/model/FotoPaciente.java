package redselection.servidor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "foto_paciente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FotoPaciente extends Base {

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "mime")
    private String mime;

    @Lob
    @Column(name = "contenido")
    private byte[] contenido;
}
