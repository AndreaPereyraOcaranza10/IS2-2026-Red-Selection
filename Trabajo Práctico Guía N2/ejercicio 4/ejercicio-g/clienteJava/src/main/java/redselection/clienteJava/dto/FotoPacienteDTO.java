package redselection.clienteJava.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Base64;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FotoPacienteDTO {
    private String id;
    private String nombre;
    private String mime;
    private byte[] contenido;   // en el JSON viaja como Base64; Jackson lo convierte solo

    // Ayuda para mostrar la imagen en HTML: <img src="data:image/png;base64,...">
    @JsonIgnore
    public String getDataUri() {
        if (contenido == null || contenido.length == 0) return null;
        return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(contenido);
    }
}
