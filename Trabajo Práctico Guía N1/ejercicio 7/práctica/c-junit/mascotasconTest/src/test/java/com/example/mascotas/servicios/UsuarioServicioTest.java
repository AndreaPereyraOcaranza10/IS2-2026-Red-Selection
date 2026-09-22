package com.example.mascotas.servicios;

import com.example.mascotas.entidades.Usuario;
import com.example.mascotas.entidades.Zona;
import com.example.mascotas.errores.ErrorServicio;
import com.example.mascotas.repositorios.UsuarioRespositorio;
import com.example.mascotas.repositorios.ZonaRepositorio;
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
class UsuarioServicioTest {

    @Mock
    private UsuarioRespositorio usuarioRepositorio;
    @Mock
    private ZonaRepositorio zonaRepositorio;
    @Mock
    private FotoServicio fotoServicio;
    @Mock
    private NotificacionServicio notificacionServicio;

    @InjectMocks
    private UsuarioServicio usuarioServicio;

    @Test
    void testRegistrar() throws ErrorServicio {
        when(zonaRepositorio.getReferenceById("z1")).thenReturn(new Zona());

        usuarioServicio.registrar(null, "Ana", "Perez", "ana@mail.com", "1234567", "1234567", "z1");

        verify(usuarioRepositorio, times(1)).save(any(Usuario.class));
    }

    @Test
    void testRegistrarClavesDistintas() {
        when(zonaRepositorio.getReferenceById("z1")).thenReturn(new Zona());

        ErrorServicio ex = assertThrows(ErrorServicio.class,
                () -> usuarioServicio.registrar(null, "Ana", "Perez", "ana@mail.com", "1234567", "7654321", "z1"));

        assertEquals("Las claves deben ser iguales", ex.getMessage());
        verify(usuarioRepositorio, never()).save(any(Usuario.class));
    }

    @Test
    void testValidarUsuarioSinNombre() {
        ErrorServicio ex = assertThrows(ErrorServicio.class,
                () -> usuarioServicio.validar("", "Perez", "ana@mail.com", "1234567", "1234567", new Zona()));

        assertEquals("El nombre del usuario no puede ser nulo", ex.getMessage());
    }

    @Test
    void testModificar() throws ErrorServicio {
        Usuario original = new Usuario();
        original.setId("u1");
        when(zonaRepositorio.getReferenceById("z1")).thenReturn(new Zona());
        when(usuarioRepositorio.findById("u1")).thenReturn(Optional.of(original));

        usuarioServicio.modificar(null, "u1", "Ana2", "Perez", "ana@mail.com", "1234567", "1234567", "z1");

        assertEquals("Ana2", original.getNombre());
        verify(usuarioRepositorio, times(1)).save(original);
    }

    @Test
    void testModificarInexistente() {
        when(zonaRepositorio.getReferenceById("z1")).thenReturn(new Zona());
        when(usuarioRepositorio.findById("u1")).thenReturn(Optional.empty());

        ErrorServicio ex = assertThrows(ErrorServicio.class,
                () -> usuarioServicio.modificar(null, "u1", "Ana", "Perez", "ana@mail.com", "1234567", "1234567", "z1"));

        assertEquals("No se encontro el usuario solicitado", ex.getMessage());
    }

    @Test
    void testLoginSinMail() {
        ErrorServicio ex = assertThrows(ErrorServicio.class, () -> usuarioServicio.login("", "1234567"));

        assertEquals("Debe ingresar un mail", ex.getMessage());
    }

    @Test
    void testLoginCorrecto() throws ErrorServicio {
        Usuario usuario = new Usuario();
        usuario.setMail("ana@mail.com");
        usuario.setClave("1234567");
        when(usuarioRepositorio.buscarPorMail("ana@mail.com")).thenReturn(usuario);

        Usuario resultado = usuarioServicio.login("ana@mail.com", "1234567");

        assertSame(usuario, resultado);
    }

    @Test
    void testDeshabilitar() throws ErrorServicio {
        Usuario usuario = new Usuario();
        usuario.setId("u1");
        when(usuarioRepositorio.findById("u1")).thenReturn(Optional.of(usuario));

        usuarioServicio.deshabilitar("u1");

        assertNotNull(usuario.getBaja());
        verify(usuarioRepositorio, times(1)).save(usuario);
    }
}