package com.example.mascotas.servicios;

import com.example.mascotas.entidades.Mascota;
import com.example.mascotas.entidades.Usuario;
import com.example.mascotas.enumeracion.Sexo;
import com.example.mascotas.enumeracion.Tipo;
import com.example.mascotas.errores.ErrorServicio;
import com.example.mascotas.repositorios.MascotaRepositorio;
import com.example.mascotas.repositorios.UsuarioRespositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MascotaServicioTest {

    @Mock
    private UsuarioRespositorio usuarioRepositorio;
    @Mock
    private MascotaRepositorio mascotaRepositorio;
    @Mock
    private FotoServicio fotoServicio;

    @InjectMocks
    private MascotaServicio mascotaServicio;

    private Mascota mascotaDe(String idMascota, String idUsuario) {
        Usuario usuario = new Usuario();
        usuario.setId(idUsuario);
        Mascota mascota = new Mascota();
        mascota.setId(idMascota);
        mascota.setUsuario(usuario);
        return mascota;
    }

    @Test
    void testAgregarMascota() throws ErrorServicio {
        Usuario usuario = new Usuario();
        usuario.setId("u1");
        when(usuarioRepositorio.findById("u1")).thenReturn(Optional.of(usuario));

        mascotaServicio.agregarMascota(null, "u1", "Firulais", Sexo.MACHO, Tipo.PERRO);

        verify(mascotaRepositorio, times(1)).save(any(Mascota.class));
    }

    @Test
    void testAgregarMascotaSinNombre() {
        Usuario usuario = new Usuario();
        usuario.setId("u1");
        when(usuarioRepositorio.findById("u1")).thenReturn(Optional.of(usuario));

        ErrorServicio ex = assertThrows(ErrorServicio.class,
                () -> mascotaServicio.agregarMascota(null, "u1", "", Sexo.MACHO, Tipo.PERRO));

        assertEquals("El nombre de la mascota no puede ser nulo o vacio", ex.getMessage());
        verify(mascotaRepositorio, never()).save(any(Mascota.class));
    }

    @Test
    void testModificarMascotaDeOtroUsuario() {
        when(mascotaRepositorio.findById("m1")).thenReturn(Optional.of(mascotaDe("m1", "u1")));

        ErrorServicio ex = assertThrows(ErrorServicio.class,
                () -> mascotaServicio.modificar(null, "u2", "m1", "Michi", Sexo.HEMBRA, Tipo.GATO));

        assertEquals("No tiene permisos suficientes para realizar esta accion", ex.getMessage());
        verify(mascotaRepositorio, never()).save(any(Mascota.class));
    }

    @Test
    void testModificarMascotaInexistente() {
        when(mascotaRepositorio.findById("m1")).thenReturn(Optional.empty());

        ErrorServicio ex = assertThrows(ErrorServicio.class,
                () -> mascotaServicio.modificar(null, "u1", "m1", "Michi", Sexo.HEMBRA, Tipo.GATO));

        assertEquals("No existe una mascota con el identificador solicitado", ex.getMessage());
    }

    @Test
    void testEliminarMascota() throws ErrorServicio {
        Mascota mascota = mascotaDe("m1", "u1");
        when(mascotaRepositorio.findById("m1")).thenReturn(Optional.of(mascota));

        mascotaServicio.eliminar("u1", "m1");

        assertNotNull(mascota.getBaja());
        verify(mascotaRepositorio, times(1)).save(mascota);
    }

    @Test
    void testEliminarMascotaDeOtroUsuario() {
        Mascota mascota = mascotaDe("m1", "u1");
        when(mascotaRepositorio.findById("m1")).thenReturn(Optional.of(mascota));

        assertThrows(ErrorServicio.class, () -> mascotaServicio.eliminar("u2", "m1"));

        assertNull(mascota.getBaja());
        verify(mascotaRepositorio, never()).save(any(Mascota.class));
    }
}