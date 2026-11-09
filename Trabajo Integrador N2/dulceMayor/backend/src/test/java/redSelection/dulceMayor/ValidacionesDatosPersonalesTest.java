package redSelection.dulceMayor;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import redSelection.dulceMayor.controller.PortalProfileController;
import redSelection.dulceMayor.dto.ResponsableCrearDTO;
import redSelection.dulceMayor.enums.TipoDocumento;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.ResponsableComercio;
import redSelection.dulceMayor.model.Usuario;
import redSelection.dulceMayor.repository.ResponsableComercioRepository;
import redSelection.dulceMayor.repository.UsuarioRepository;
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

    @Test
    void perfilRechazaNombresConNumerosYTelefonosFueraDeLongitud() {
        PortalProfileController.ProfileRequest request = new PortalProfileController.ProfileRequest(
                "Ana1", "Perez1", null, "1990-01-01", TipoDocumento.DNI,
                "12345678", "123456789", null, "1", "1", "1",
                "Calle", "1", null, null, null, null);

        var errores = validator.validate(request).stream()
                .map(error -> error.getPropertyPath().toString()).toList();

        assertTrue(errores.contains("nombre"));
        assertTrue(errores.contains("apellido"));
        assertTrue(errores.contains("telefono"));
    }

    @Test
    void dniInvalidoSeRechazaAlCrearYModificarResponsable() {
        ResponsableComercioRepository repositorio = mock(ResponsableComercioRepository.class);
        ResponsableComercioService servicio = new ResponsableComercioService(repositorio);
        ResponsableComercio existente = ResponsableComercio.builder()
                .tipoDocumento(TipoDocumento.DNI).numeroDocumento("87654321").build();
        when(repositorio.findByIdAndEliminadoFalse("responsable-1")).thenReturn(Optional.of(existente));
        ResponsableCrearDTO dto = new ResponsableCrearDTO(
                "Ana", "Perez", TipoDocumento.DNI, "1234567",
                "ana@example.com", "1123456789", null);

        assertThrows(ReglaNegocioException.class, () -> servicio.crear(dto));
        assertThrows(ReglaNegocioException.class, () -> servicio.actualizar("responsable-1", dto));
        verify(repositorio, never()).save(any());
    }

    @Test
    void dniInvalidoSeRechazaEnPerfilDelUsuario() {
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        ResponsableComercioRepository responsables = mock(ResponsableComercioRepository.class);
        PortalProfileController controller = new PortalProfileController(
                usuarios, responsables, mock(UbicacionService.class));
        Usuario usuario = Usuario.builder().nombreUsuario("ana@example.com")
                .activo(true).eliminado(false).build();
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("ana@example.com");
        when(usuarios.findByNombreUsuarioIgnoreCase("ana@example.com")).thenReturn(Optional.of(usuario));
        PortalProfileController.ProfileRequest request = new PortalProfileController.ProfileRequest(
                "Ana", "Perez", null, "1990-01-01", TipoDocumento.DNI,
                "1234567", "1123456789", null, "1", "1", "1",
                "Calle", "1", null, null, null, null);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.update(authentication, request));

        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(responsables);
    }
}
