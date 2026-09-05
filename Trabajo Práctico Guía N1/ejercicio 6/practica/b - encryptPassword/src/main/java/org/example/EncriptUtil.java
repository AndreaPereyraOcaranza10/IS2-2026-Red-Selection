package org.example;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;



public class EncriptUtil {

    public static void main(String[] args) {
        String password = "123";
        System.out.println("Contraseña original: " + password);
        String encryptedPassword = encryptPassword(password);
        System.out.println("Contraseña encriptada: " + encryptedPassword);
    }

    public static String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }
}

