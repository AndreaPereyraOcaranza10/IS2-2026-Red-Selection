package redSelection.dulceMayor.dto;

import redSelection.dulceMayor.enums.TipoDocumento;

/** El documento viaja enmascarado ("*****222"): distingue homónimos sin exponer el número completo. */
public record ResponsableRespuestaDTO(String id, String nombre, String apellido, TipoDocumento tipoDocumento,
                                      String documentoParcial, String email, String telefono) { }