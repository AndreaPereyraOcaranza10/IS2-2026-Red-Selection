package com.RedSelection.videojuegos.services;

import com.RedSelection.videojuegos.entities.Videojuego;
import com.RedSelection.videojuegos.repositories.RepositorioVideojuego;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioVideojuegoTest {

    @Mock
    private RepositorioVideojuego repositorio;

    @InjectMocks
    private ServicioVideojuego servicio;

    private Videojuego videojuego(long id, String titulo, boolean activo) {
        Videojuego v = new Videojuego();
        v.setId(id);
        v.setTitulo(titulo);
        v.setActivo(activo);
        return v;
    }

    @Test
    void testFindAll() throws Exception {
        when(repositorio.findAll()).thenReturn(List.of(videojuego(1, "Life is Strange", true), videojuego(2, "Mario", true)));

        List<Videojuego> resultado = servicio.findAll();

        assertEquals(2, resultado.size());
    }

    @Test
    void testFindById() throws Exception {
        Videojuego lifeIsStrange = videojuego(1, "Life is Strange", true);
        when(repositorio.findById(1L)).thenReturn(Optional.of(lifeIsStrange));

        assertSame(lifeIsStrange, servicio.findById(1L));
    }

    @Test
    void testFindByIdInexistente() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> servicio.findById(99L));
    }

    @Test
    void testSaveOne() throws Exception {
        Videojuego nuevo = videojuego(0, "Life is Strange", true);
        when(repositorio.save(nuevo)).thenReturn(nuevo);

        Videojuego resultado = servicio.saveOne(nuevo);

        assertSame(nuevo, resultado);
        verify(repositorio, times(1)).save(nuevo);
    }

    @Test
    void testDeleteByIdEsBajaLogica() throws Exception {
        Videojuego activo = videojuego(1, "Life is Strange", true);
        when(repositorio.findById(1L)).thenReturn(Optional.of(activo));

        boolean resultado = servicio.deleteById(1L);

        assertTrue(resultado);
        assertFalse(activo.isActivo());
        verify(repositorio, times(1)).save(activo);
    }

    @Test
    void testDeleteByIdInexistente() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> servicio.deleteById(99L));

        verify(repositorio, never()).save(any(Videojuego.class));
    }

    @Test
    void testFindByTitle() throws Exception {
        List<Videojuego> coincidencias = List.of(videojuego(1, "Life is Strange", true));
        when(repositorio.findByTitle("life")).thenReturn(coincidencias);

        assertSame(coincidencias, servicio.findByTitle("life"));
    }
}
