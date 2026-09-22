package com.example.junit;

import org.junit.jupiter.api.*;
import org.springframework.test.annotation.Repeat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CalculadoraTest {

    private Calculadora cal = new Calculadora();

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        System.out.println("Inicia" + testInfo.getDisplayName());
    }

    @AfterEach
    public void afterEach(TestInfo testInfo){
        System.out.println("Finaliza" + testInfo.getDisplayName());
    }

    @Test
    @Order(1)
    public void testSumar() {
        int a = 2;
        int b = 3;
        int c = 5; // Resultado esperado

        int resultado = cal.sumar(a, b);
        System.out.println("Resultado: " + resultado);
    }

    @Test
    @Order(2)
    public void testDividir() {
        Double numerador = 6.0;
        Double denominador = 3.0;
        Double esperado = 2.0;

        try {
            Double resultado = cal.dividir(numerador, denominador);
            System.out.println("resultado " + resultado);

            assertEquals(esperado, resultado);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(3)
    @RepeatedTest(5)
    public void testDividirCheckExcepcion() {
        Double numerador = 10.0;
        Double denominador = 0.0;
        Double esperado = 0.6666666666;
        Double resultado = 0.0;

        try {
            Exception excepcion = assertThrows(Exception.class, () -> {
                cal.dividir(numerador, denominador);
            });

            assertEquals("Denominador inválido", excepcion.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("resultado " + resultado);
    }
}