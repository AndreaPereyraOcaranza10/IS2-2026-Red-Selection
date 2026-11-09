package redSelection.dulceMayor;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import redSelection.dulceMayor.dto.PerfilClienteDTO;
import redSelection.dulceMayor.dto.ResponsableCrearDTO;
import redSelection.dulceMayor.enums.TipoDocumento;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.ResponsableComercio;
import redSelection.dulceMayor.model.Usuario;
import redSelection.dulceMayor.repository.ResponsableComercioRepository;
import redSelection.dulceMayor.service.CuentaClienteService;
import redSelection.dulceMayor.service.PerfilClienteService;
import redSelection.dulceMayor.service.ResponsableComercioService;
import redSelection.dulceMayor.service.UbicacionService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidacionesDatosPersonalesTest {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private final Validator validator = FACTORY.getValidator();

    @AfterAll
    static void cerrarValidador() {
        FACTORY.close();
    }

    @Test
    void responsableRechazaNombresConNumerosYTelefonosFueraDeLongitud() {
        ResponsableCrearDTO dto = new ResponsableCrearDTO(
                "Ana1", "Perez1", TipoDocumento.DNI, "12345678",
                "ana@example.com", "123456789", null);

        var errores = validator.validate(dto).stream()
                .map(error -> error.getPropertyPath().toString()).toList();

        assertTrue(errores.contains("nombre"));
        assertTrue(errores.contains("apellido"));
        assertTrue(errores.contains("telefono"));
    }

    @Test
    void aceptaTelefonosDeDiezOUndigitos() {
        for (String telefono : new String[]{"1123456789", "01123456789"}) {
            ResponsableCrearDTO dto = new ResponsableCrearDTO(
                    "Ana", "Perez", TipoDocumento.DNI, "12345678",
                    "ana@example.com", telefono, null);
            assertTrue(validator.validate(dto).isEmpty(), "Teléfono rechazado: " + telefono);
        }
    }

    // Las reglas del perfil se movieron del controller a PerfilClienteService (ValidadorPersona),
    // así que estos casos se prueban contra el service y esperan ReglaNegocioException.
    @Test
    void perfilRechazaNombresConNumerosYTelefonosFueraDeLongitud() {
        PerfilClienteService servicio = perfilServiceDe(mock(ResponsableComercioService.class));

        assertThrows(ReglaNegocioException.class, () -> servicio.actualizar(CORREO,
                perfil("Ana1", "Perez", "12345678", "1123456789")));
        assertThrows(ReglaNegocioException.class, () -> servicio.actualizar(CORREO,
                perfil("Ana", "Perez1", "12345678", "1123456789")));
        assertThrows(ReglaNegocioException.class, () -> servicio.actualizar(CORREO,
                perfil("Ana", "Perez", "12345678", "123456789")));
    }

    @Test
    void dniInvalidoSeRechazaAlCrearYModificarResponsable() {
        ResponsableComercioRepository repositorio = mock(ResponsableComercioRepository.class);
        ResponsableComercioService servicio = new ResponsableComercioService(repositorio,
                mock(CuentaClienteService.class));
        ResponsableComercio existente = ResponsableComercio.builder()
                .tipoDocumento(TipoDocumento.DNI).numeroDocumento("87654321").build();
        when(repositorio.findByIdAndEliminadoFalse("responsable-1")).thenReturn(Optional.of(existente));
        // El DNI acepta 7 u 8 números (los DNI viejos tienen 7): 6 es inválido.
        ResponsableCrearDTO dto = new ResponsableCrearDTO(
                "Ana", "Perez", TipoDocumento.DNI, "123456",
                "ana@example.com", "1123456789", null);

        assertThrows(ReglaNegocioException.class, () -> servicio.crear(dto));
        assertThrows(ReglaNegocioException.class, () -> servicio.actualizar("responsable-1", dto));
        verify(repositorio, never()).save(any());
    }

    @Test
    void dniInvalidoSeRechazaEnPerfilDelUsuario() {
        ResponsableComercioService responsables = mock(ResponsableComercioService.class);
        PerfilClienteService servicio = perfilServiceDe(responsables);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.actualizar(CORREO, perfil("Ana", "Perez", "123456", "1123456789")));

        assertTrue(error.getMessage().contains("DNI"));
        verify(responsables, never()).guardar(any());
    }

    private static final String CORREO = "ana@example.com";

    /** Service del perfil con un usuario activo y sin perfil cargado todavía (primera vez que lo completa). */
    private static PerfilClienteService perfilServiceDe(ResponsableComercioService responsables) {
        CuentaClienteService cuentas = mock(CuentaClienteService.class);
        Usuario usuario = Usuario.builder().nombreUsuario(CORREO).activo(true).eliminado(false).build();
        when(cuentas.obtenerUsuarioActivo(CORREO)).thenReturn(usuario);
        when(responsables.buscarPorNombreUsuario(CORREO)).thenReturn(Optional.empty());
        return new PerfilClienteService(cuentas, responsables, mock(UbicacionService.class));
    }

    private static PerfilClienteDTO perfil(String nombre, String apellido, String dni, String telefono) {
        return new PerfilClienteDTO(nombre, apellido, null, "1990-01-01", TipoDocumento.DNI,
                dni, telefono, null, "1", "1", "1", "Calle", "1", null, null, null, null);
    }
}
