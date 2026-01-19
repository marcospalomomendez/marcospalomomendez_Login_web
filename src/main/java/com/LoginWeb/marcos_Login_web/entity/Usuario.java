package com.LoginWeb.marcos_Login_web.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un usuario del sistema.
 *
 * Tecnologías utilizadas:
 * - JPA/Hibernate: Mapeo objeto-relacional
 * - Lombok: Reducción de código boilerplate
 * - BCrypt: Verificación segura de contraseñas
 */
@Entity
@Table(name = "usuarios", indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_email", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    // ========================================
    // CAMPOS DE LA ENTIDAD
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "activo", nullable = false)
    @Builder.Default  // Lombok: valor por defecto en el builder
    private Boolean activo = true;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;
    @Column(name = "intentos_fallidos",nullable = false)
    private int intentosFallidos = 0;
    @Column(name = "bloqueado",nullable = false)
    private boolean bloqueado = false;
    // ========================================
    // CALLBACKS DEL CICLO DE VIDA
    // ========================================

    /**
     * Se ejecuta automáticamente ANTES de insertar en BBDD.
     * Establece la fecha de creación y actualización.
     */
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Se ejecuta automáticamente ANTES de actualizar en BBDD.
     * Actualiza la fecha de última modificación.
     */
    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // ========================================
    // MÉTODOS DE NEGOCIO
    // ========================================

    // Encoder estático para verificación de contraseñas
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    /**
     * Verifica si la contraseña proporcionada coincide con el hash almacenado.
     *
     * @param rawPassword Contraseña en texto plano a verificar
     * @return true si la contraseña es correcta, false en caso contrario
     */
    public boolean verificarPassword(String rawPassword) {
        return encoder.matches(rawPassword, this.passwordHash);
    }

    /**
     * Hashea una contraseña y la asigna a este usuario.
     * Útil al crear o actualizar la contraseña.
     *
     * @param rawPassword Contraseña en texto plano
     */
    public void setPassword(String rawPassword) {
        this.passwordHash = encoder.encode(rawPassword);
    }

    /**
     * Desactiva el usuario (borrado lógico).
     */
    public void desactivar() {
        this.activo = false;
    }

    /**
     * Reactiva un usuario desactivado.
     */
    public void activar() {
        this.activo = true;
    }
    public void reseteoIntentosFallidos() {
        this.intentosFallidos = 0;
        this.bloqueado = false;
    }
    public void incrementarIntentosFallidos() {
        this.intentosFallidos++;
        if (this.intentosFallidos >= 3) {
            this.bloqueado = true;
        }
    }
    public void registrarLoginExitoso() {
        this.ultimoLogin = LocalDateTime.now();
        reseteoIntentosFallidos();
    }
}