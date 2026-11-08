package redSelection.dulceMayor.dto;

/** Regex compartidas por los DTOs de entrada: nombres sin números, numeración y teléfono solo con dígitos, etc. */
public final class PatronesValidacion {

    private PatronesValidacion() { }

    public static final String NOMBRE_PERSONA = "^\\p{L}+([ '.\\-]\\p{L}+)*$";
    public static final String CALLE = "^[\\p{L}\\d][\\p{L}\\d .'°º\\-]*$";
    public static final String NUMERACION = "^\\d{1,6}$";
    public static final String DOCUMENTO = "^[A-Za-z0-9]{6,9}$";
    public static final String TELEFONO = "^\\d{8,15}$";
    public static final String CUIT = "^\\d{2}-?\\d{8}-?\\d$";
    public static final String ID_GEOREF = "^\\d{1,11}$";
}