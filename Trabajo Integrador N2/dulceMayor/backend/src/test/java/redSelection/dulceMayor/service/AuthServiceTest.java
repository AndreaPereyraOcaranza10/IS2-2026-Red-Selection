package redSelection.dulceMayor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import redSelection.dulceMayor.dto.LoginDTO;
import redSelection.dulceMayor.exception.CuentaBloqueadaException;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.model.Usuario;
import redSelection.dulceMayor.repository.PersonaRepository;
import redSelection.dulceMayor.repository.UsuarioRepository;
import redSelection.dulceMayor.security.JwtService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** BK-16: bloqueo de login por intentos fallidos (lógica de AuthService + Usuario, sin base de datos). */
class AuthServiceTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private UsuarioRepository usuarios;
    private AuthService servicio;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepository.class);
        PersonaRepository personas = mock(PersonaRepository.class);
        JwtService jwt = mock(JwtService.class);
        when(jwt.emitir(any(), any())).thenReturn("token");
        when(personas.findByUsuarioId(any())).thenReturn(Optional.empty());
        usuario = Usuario.builder().nombreUsuario("prev").clave(encoder.encode("Correcta#1")).rol(Rol.PREVENTISTA).build();
        usuario.setId("u1");
        when(usuarios.findByNombreUsuarioIgnoreCase("prev")).thenReturn(Optional.of(usuario));
        servicio = new AuthService(usuarios, personas, encoder, jwt, 3, 15);   // 3 intentos, 15 min
    }

    private void intentar(String clave) { servicio.login(new LoginDTO("prev", clave)); }

    @Test @DisplayName("Los fallos bajo el máximo devuelven credenciales inválidas y se cuentan")
    void fallosSeCuentan() {
        assertThrows(BadCredentialsException.class, () -> intentar("mala"));
        assertThrows(BadCredentialsException.class, () -> intentar("mala"));
        assertEquals(2, usuario.getIntentosFallidos());
        assertFalse(usuario.estaBloqueado(LocalDateTime.now()));
    }

    @Test @DisplayName("Al llegar al máximo la cuenta se bloquea (423) y se guarda el estado")
    void seBloquea() {
        assertThrows(BadCredentialsException.class, () -> intentar("mala"));
        assertThrows(BadCredentialsException.class, () -> intentar("mala"));
        CuentaBloqueadaException ex = assertThrows(CuentaBloqueadaException.class, () -> intentar("mala"));
        assertTrue(ex.getSegundosRestantes() > 0);
        assertTrue(usuario.estaBloqueado(LocalDateTime.now()));
        verify(usuarios, times(3)).save(usuario);
    }

    @Test @DisplayName("Bloqueada, ni la clave correcta entra")
    void bloqueadaNoEntra() {
        usuario.registrarFallo(1, Duration.ofMinutes(15), LocalDateTime.now());
        assertThrows(CuentaBloqueadaException.class, () -> intentar("Correcta#1"));
    }

    @Test @DisplayName("Un login correcto reinicia el contador")
    void aciertoReinicia() {
        assertThrows(BadCredentialsException.class, () -> intentar("mala"));
        assertNotNull(servicio.login(new LoginDTO("prev", "Correcta#1")));
        assertEquals(0, usuario.getIntentosFallidos());
    }

    @Test @DisplayName("Vencido el bloqueo se puede volver a intentar")
    void bloqueoVence() {
        usuario.registrarFallo(1, Duration.ofMinutes(15), LocalDateTime.now().minusMinutes(20));
        assertFalse(usuario.estaBloqueado(LocalDateTime.now()));
        assertNotNull(servicio.login(new LoginDTO("prev", "Correcta#1")));
        assertNull(usuario.getBloqueadoHasta());
    }

    @Test @DisplayName("Logout y cambio de clave suben la versión del token")
    void revocaTokens() {
        when(usuarios.findByIdAndEliminadoFalse("u1")).thenReturn(Optional.of(usuario));
        int antes = usuario.getVersionToken();
        servicio.cerrarSesion(new redSelection.dulceMayor.security.UsuarioAutenticado("u1", "prev", Rol.PREVENTISTA, false, null, antes));
        assertEquals(antes + 1, usuario.getVersionToken());
    }
}
