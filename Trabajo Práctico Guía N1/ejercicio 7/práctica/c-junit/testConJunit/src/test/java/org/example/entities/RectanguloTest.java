package org.example.entities;

import org.example.entities.Rectangulo;
import org.example.services.RectanguloService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

public class RectanguloTest {

    private RectanguloService rs;

    @BeforeEach
    public void setUp() {
        rs = new RectanguloService();
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void deberiaInicializarConColor(){
        assertNotNull(new Rectangulo(10, 10).getColor());
    }

    @Test
    public void deberiaCalcularArea() {

        assertEquals(100.0, rs.calcularArea(new Rectangulo(10, 10)), 0.0);

        assertEquals(20.0, rs.calcularArea(new Rectangulo(4, 5)), 0.0);

        assertEquals(1.0, rs.calcularArea(new Rectangulo(1, 1)), 0.0);
    }

    @Test
    public void deberiaCalcularPerimetro() {

        assertEquals(40.0, rs.calcularPerimetro(new Rectangulo(10, 10)), 0.0);

        assertEquals(100.0, rs.calcularPerimetro(new Rectangulo(20, 30)), 0.0);

        assertEquals(30.0, rs.calcularPerimetro(new Rectangulo(5, 10)), 0.0);
    }

    @Test
    public void deberiaActivarODesactivar() {
        Rectangulo r = new Rectangulo(5, 5);

        assertTrue(r.isActive());

        r.setActive(false);
        assertFalse(r.isActive());
    }

}