package com.example.junit;

public class Calculadora {
    public Integer sumar (Integer a, Integer b){
        if (a == null) {
            a = 0;
        }
        if (b == null) {
            b = 0;
        }
        return a + b;
    }

    public Double dividir(Double numerador, Double denominador) throws Exception {
        if (denominador == null || denominador == 0.0) {
            throw new Exception("Denominador inválido");
        }
        return numerador / denominador;
    }
}
