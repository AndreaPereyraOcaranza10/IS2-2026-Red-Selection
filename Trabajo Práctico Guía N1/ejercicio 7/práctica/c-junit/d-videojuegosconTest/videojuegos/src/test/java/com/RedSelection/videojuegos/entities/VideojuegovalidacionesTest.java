package com.RedSelection.videojuegos.entities;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Videojuego - restricciones de validacion")
class VideojuegoValidacionTest {

    private static final long UN_DIA_EN_MS = 24L * 60 * 60 * 1000;

    private static Validator validator;

    @BeforeAll
    static void crearValidador() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Videojuego videojuegoValido() {
        Videojuego v = new Videojuego();
        v.setTitulo("Tomb Raider");
        v.setDescripcion("Aventura epica");
        v.setPrecio(50f);
        v.setStock((short) 10);
        v.setFechaLanzamiento(new Date(System.currentTimeMillis() - UN_DIA_EN_MS));
        v.setEstudio(new Estudio());
        v.setCategoria(new Categoria());
        return v;
    }

    private boolean hayViolacionEn(Videojuego v, String propiedad) {
        return validator.validate(v).stream()
                .anyMatch(violacion -> violacion.getPropertyPath().toString().equals(propiedad));
    }

    @Test
    @DisplayName("un videojuego con todos los datos correctos no tiene violaciones")
    void videojuegoValido_sinViolaciones() {
        assertTrue(validator.validate(videojuegoValido()).isEmpty());
    }

    @Test
    @DisplayName("un videojuego nuevo nace activo por defecto")
    void videojuegoNuevo_estaActivo() {
        assertTrue(new Videojuego().isActivo());
    }

    @ParameterizedTest(name = "titulo = [{0}]")
    @NullAndEmptySource
    @DisplayName("titulo nulo o vacio es invalido")
    void titulo_nuloOVacio_esInvalido(String titulo) {
        Videojuego v = videojuegoValido();
        v.setTitulo(titulo);

        assertTrue(hayViolacionEn(v, "titulo"));
    }

    @ParameterizedTest(name = "descripcion de {0} caracteres")
    @ValueSource(ints = {4, 101})
    @DisplayName("descripcion fuera del rango 5-100 es invalida")
    void descripcion_fueraDeRango_esInvalida(int largo) {
        Videojuego v = videojuegoValido();
        v.setDescripcion("a".repeat(largo));

        assertTrue(hayViolacionEn(v, "descripcion"));
    }

    @ParameterizedTest(name = "descripcion de {0} caracteres")
    @ValueSource(ints = {5, 100})
    @DisplayName("descripcion en los limites 5 y 100 es valida")
    void descripcion_enLosLimites_esValida(int largo) {
        Videojuego v = videojuegoValido();
        v.setDescripcion("a".repeat(largo));

        assertFalse(hayViolacionEn(v, "descripcion"));
    }

    @ParameterizedTest(name = "precio = {0}")
    @ValueSource(floats = {4.99f, 0f, -1f, 10000.5f})
    @DisplayName("precio fuera del rango 5-10000 es invalido")
    void precio_fueraDeRango_esInvalido(float precio) {
        Videojuego v = videojuegoValido();
        v.setPrecio(precio);

        assertTrue(hayViolacionEn(v, "precio"));
    }

    @ParameterizedTest(name = "precio = {0}")
    @ValueSource(floats = {5f, 10000f})
    @DisplayName("precio en los limites 5 y 10000 es valido")
    void precio_enLosLimites_esValido(float precio) {
        Videojuego v = videojuegoValido();
        v.setPrecio(precio);

        assertFalse(hayViolacionEn(v, "precio"));
    }

    @ParameterizedTest(name = "stock = {0}")
    @ValueSource(shorts = {-1, 10001})
    @DisplayName("stock fuera del rango 0-10000 es invalido")
    void stock_fueraDeRango_esInvalido(short stock) {
        Videojuego v = videojuegoValido();
        v.setStock(stock);

        assertTrue(hayViolacionEn(v, "stock"));
    }

    @ParameterizedTest(name = "stock = {0}")
    @ValueSource(shorts = {0, 10000})
    @DisplayName("stock en los limites 0 y 10000 es valido")
    void stock_enLosLimites_esValido(short stock) {
        Videojuego v = videojuegoValido();
        v.setStock(stock);

        assertFalse(hayViolacionEn(v, "stock"));
    }

    @Test
    @DisplayName("fecha de lanzamiento nula es invalida")
    void fechaLanzamiento_nula_esInvalida() {
        Videojuego v = videojuegoValido();
        v.setFechaLanzamiento(null);

        assertTrue(hayViolacionEn(v, "fechaLanzamiento"));
    }

    @Test
    @DisplayName("fecha de lanzamiento futura es invalida")
    void fechaLanzamiento_futura_esInvalida() {
        Videojuego v = videojuegoValido();
        v.setFechaLanzamiento(new Date(System.currentTimeMillis() + UN_DIA_EN_MS));

        assertTrue(hayViolacionEn(v, "fechaLanzamiento"));
    }

    @Test
    @DisplayName("fecha de lanzamiento de hoy es valida")
    void fechaLanzamiento_hoy_esValida() {
        Videojuego v = videojuegoValido();
        v.setFechaLanzamiento(new Date());

        assertFalse(hayViolacionEn(v, "fechaLanzamiento"));
    }

    @Test
    @DisplayName("estudio nulo es invalido")
    void estudio_nulo_esInvalido() {
        Videojuego v = videojuegoValido();
        v.setEstudio(null);

        assertTrue(hayViolacionEn(v, "estudio"));
    }

    @Test
    @DisplayName("categoria nula es invalida")
    void categoria_nula_esInvalida() {
        Videojuego v = videojuegoValido();
        v.setCategoria(null);

        assertTrue(hayViolacionEn(v, "categoria"));
    }
}