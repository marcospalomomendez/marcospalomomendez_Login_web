package com.LoginWeb.marcos_Login_web.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Utilidad para manejo seguro de contraseñas con BCrypt.
 */
@Component
public class PasswordUtil {

    // Strength = 12 (factor de coste). Valores recomendados: 10-14
    // Mayor valor = más seguro pero más lento
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    /**
     * Hashea una contraseña en texto plano.
     *
     * @param rawPassword Contraseña en texto plano
     * @return Hash BCrypt de la contraseña
     *
     * Ejemplo:
     *   Input:  "password123"
     *   Output: "$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     */
    public String hashPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * Verifica si una contraseña coincide con su hash.
     *
     * @param rawPassword Contraseña en texto plano introducida por el usuario
     * @param hashedPassword Hash almacenado en la base de datos
     * @return true si coinciden, false si no
     *
     * Ejemplo:
     *   verifyPassword("password123", "$2a$12$N9qo8u...") → true
     *   verifyPassword("wrongpass", "$2a$12$N9qo8u...") → false
     */
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }

    /**
     * Genera un hash BCrypt usando el encoder estático.
     * Útil para generar hashes desde la consola o scripts.
     */
    public static String generateHash(String password) {
        return encoder.encode(password);
    }

    // Método main para generar hashes de prueba
    public static void main(String[] args) {
        String[] passwords = {"password123", "admin", "usuario1"};

        System.out.println("=== Generador de Hashes BCrypt ===\n");
        for (String pwd : passwords) {
            String hash = generateHash(pwd);
            System.out.println("Password: " + pwd);
            System.out.println("Hash:     " + hash);
            System.out.println("Longitud: " + hash.length() + " caracteres\n");
        }
    }
}