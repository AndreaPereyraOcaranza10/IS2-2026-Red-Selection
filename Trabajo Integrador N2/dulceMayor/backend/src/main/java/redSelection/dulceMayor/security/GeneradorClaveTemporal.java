package redSelection.dulceMayor.security;

import java.security.SecureRandom;

/**
 * Clave temporal para el primer ingreso de un cliente: aleatoria (SecureRandom, no predecible como una clave
 * fija), sin caracteres que se confunden al leerlos (0/O, 1/l/I) y con mayúscula, minúscula y número.
 * Se guarda hasheada (BCrypt) y se manda por correo UNA vez; el usuario nace con primerIngreso = true, así que
 * el backend lo obliga a cambiarla antes de dejarlo hacer cualquier otra cosa.
 */
public final class GeneradorClaveTemporal {

    private static final String MAYUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String MINUSCULAS = "abcdefghijkmnpqrstuvwxyz";
    private static final String NUMEROS = "23456789";
    private static final String TODOS = MAYUSCULAS + MINUSCULAS + NUMEROS;
    private static final int LARGO = 10;
    private static final SecureRandom AZAR = new SecureRandom();

    private GeneradorClaveTemporal() { }

    public static String generar() {
        char[] clave = new char[LARGO];
        clave[0] = elegir(MAYUSCULAS);
        clave[1] = elegir(MINUSCULAS);
        clave[2] = elegir(NUMEROS);
        for (int i = 3; i < LARGO; i++) {
            clave[i] = elegir(TODOS);
        }
        for (int i = LARGO - 1; i > 0; i--) {          // mezcla Fisher-Yates para que las posiciones no sean fijas
            int j = AZAR.nextInt(i + 1);
            char tmp = clave[i];
            clave[i] = clave[j];
            clave[j] = tmp;
        }
        return new String(clave);
    }

    private static char elegir(String alfabeto) {
        return alfabeto.charAt(AZAR.nextInt(alfabeto.length()));
    }
}