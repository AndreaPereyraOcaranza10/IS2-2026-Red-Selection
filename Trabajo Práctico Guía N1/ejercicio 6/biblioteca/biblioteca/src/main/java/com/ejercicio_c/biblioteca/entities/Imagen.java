
package com.ejercicio_c.biblioteca.entities;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Entity
public class Imagen {
	
    @Id
    private String id;
    private String mime;
    private String nombre;

    //@Lob le informa a spring que el contenido de la imagen es un objeto grande y que debe ser tratado como tal.
    @Lob @Basic(fetch = FetchType.LAZY) //tipo de carga lazy
    private byte[] contenido;
    private boolean eliminado;

}
