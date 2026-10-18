package redselection.ejercicio_c.patrones.adaptador;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

// Configuración común de todos los mappers:
// - componentModel "spring": cada mapper generado es un @Component que se inyecta con @Autowired
// - los campos que existen de un solo lado (ej. "ubicacion" en un libro digital) se ignoran sin advertencias
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MapperConfiguracion {
}