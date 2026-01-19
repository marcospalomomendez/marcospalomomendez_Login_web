package com.LoginWeb.marcos_Login_web.service;

import com.LoginWeb.marcos_Login_web.entity.Usuario;
import com.LoginWeb.marcos_Login_web.repository.UsuarioRepository;
import com.LoginWeb.marcos_Login_web.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio que implementa la lógica de negocio para gestión de usuarios.
 *
 * Anotaciones clave:
 * - @Service: Marca la clase como componente de servicio Spring
 * - @Transactional: Gestión automática de transacciones
 * - @Slf4j: Logger automático de Lombok
 * - @RequiredArgsConstructor: Inyección de dependencias por constructor
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordUtil passwordUtil = new PasswordUtil();
    // ========================================
    // OPERACIONES CREATE
    // ========================================

    /**
     * Crea un nuevo usuario con contraseña hasheada.
     *
     * @param username Nombre de usuario único
     * @param email Email único
     * @param rawPassword Contraseña en texto plano (se hasheará)
     * @return Usuario creado
     * @throws IllegalArgumentException si username o email ya existen
     */
    public Usuario crearUsuario(String username, String email, String rawPassword) {
        log.info("Creando nuevo usuario: {}", username);

        // Validaciones de negocio
        if (usuarioRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("El username '" + username + "' ya existe");
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El email '" + email + "' ya está registrado");
        }

        // Crear usuario con contraseña hasheada
        Usuario usuario = Usuario.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordUtil.hashPassword(rawPassword))
                .activo(true)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario creado con ID: {}", guardado.getId());

        return guardado;
    }

    // ========================================
    // OPERACIONES READ
    // ========================================

    /**
     * Obtiene todos los usuarios.
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodos() {
        log.debug("Obteniendo todos los usuarios");
        return usuarioRepository.findAll();
    }

    /**
     * Obtiene un usuario por su ID.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorId(Long id) {
        log.debug("Buscando usuario con ID: {}", id);
        return usuarioRepository.findById(id);
    }

    /**
     * Obtiene un usuario por su username.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorUsername(String username) {
        log.debug("Buscando usuario con username: {}", username);
        return usuarioRepository.findByUsername(username);
    }

    /**
     * Obtiene todos los usuarios activos.
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerUsuariosActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    // ========================================
    // OPERACIONES UPDATE
    // ========================================

    /**
     * Actualiza email y/o contraseña de un usuario.
     *
     * @param id ID del usuario a actualizar
     * @param nuevoEmail Nuevo email (null para no cambiar)
     * @param nuevaPassword Nueva contraseña en texto plano (null para no cambiar)
     * @return Usuario actualizado
     * @throws IllegalArgumentException si el usuario no existe o email duplicado
     */
    public Usuario actualizarUsuario(Long id, String nuevoEmail, String nuevaPassword) {
        log.info("Actualizando usuario ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        // Actualizar email si se proporciona
        if (nuevoEmail != null && !nuevoEmail.equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(nuevoEmail)) {
                throw new IllegalArgumentException("El email '" + nuevoEmail + "' ya está en uso");
            }
            usuario.setEmail(nuevoEmail);
            log.debug("Email actualizado a: {}", nuevoEmail);
        }

        // Actualizar contraseña si se proporciona
        if (nuevaPassword != null && !nuevaPassword.isEmpty()) {
            usuario.setPasswordHash(passwordUtil.hashPassword(nuevaPassword));
            log.debug("Contraseña actualizada para usuario: {}", usuario.getUsername());
        }

        return usuarioRepository.save(usuario);
    }

    // ========================================
    // OPERACIONES DELETE
    // ========================================

    /**
     * Elimina un usuario de forma permanente (borrado físico).
     *
     * @param id ID del usuario a eliminar
     * @throws IllegalArgumentException si el usuario no existe
     */
    public void eliminarUsuario(Long id) {
        log.warn("Eliminando usuario ID: {} (borrado físico)", id);

        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + id);
        }

        usuarioRepository.deleteById(id);
        log.info("Usuario eliminado permanentemente");
    }

    /**
     * Desactiva un usuario (borrado lógico).
     * El usuario permanece en la BBDD pero no puede hacer login.
     *
     * @param id ID del usuario a desactivar
     * @return Usuario desactivado
     */
    public Usuario desactivarUsuario(Long id) {
        log.info("Desactivando usuario ID: {} (borrado lógico)", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        usuario.setActivo(false);
        return usuarioRepository.save(usuario);
    }

    /**
     * Reactiva un usuario previamente desactivado.
     */
    public Usuario activarUsuario(Long id) {
        log.info("Reactivando usuario ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    // ========================================
    // OPERACIONES DE AUTENTICACIÓN
    // ========================================

    /**
     * Verifica las credenciales de un usuario para login.
     *
     * @param username Nombre de usuario
     * @param rawPassword Contraseña en texto plano
     * @return Optional con el usuario si las credenciales son válidas
     */
    public Optional<Usuario> verificarCredenciales(String username, String rawPassword) {
        log.debug("Verificando credenciales para: {}", username);

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isEmpty()) {
            log.warn("Intento de login con usuario inexistente: {}", username);
            return Optional.empty();
        }

        Usuario usuario = usuarioOpt.get();

        if (!usuario.getActivo()) {
            log.warn("Intento de login con usuario desactivado: {}", username);
            return Optional.empty();
        }

        if (passwordUtil.verifyPassword(rawPassword, usuario.getPasswordHash())) {
            log.info("Login exitoso para usuario: {}", username);

            // Actualizar último login
            usuario.setUltimoLogin(LocalDateTime.now());
            usuarioRepository.save(usuario);

            return Optional.of(usuario);
        }

        log.warn("Contraseña incorrecta para usuario: {}", username);
        return Optional.empty();
    }

    public boolean verificarLogin(String username, String passwordIntroducida) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isEmpty()) return false;

        Usuario usuario = usuarioOpt.get();

        if (!usuario.getActivo()) return false;

        return passwordUtil.verifyPassword(passwordIntroducida, usuario.getPasswordHash());
    }

    // ========================================
    // EJERCICIO 1: ACTUALIZAR ÚLTIMO LOGIN
    // ========================================
    public void actualizarUltimoLogin(Usuario usuario) {
        usuario.setUltimoLogin(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    // ========================================
    // EJERCICIO 2: CAMBIO DE CONTRASEÑA
    // ========================================
    public Usuario cambiarPassword(Long id, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordUtil.verifyPassword(passwordActual, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        usuario.setPasswordHash(passwordUtil.hashPassword(passwordNueva));
        return usuarioRepository.save(usuario);
    }

    // ========================================
    // EJERCICIO 3: BÚSQUEDA PAGINADA
    // ========================================
    @Transactional(readOnly = true)
    public Page<Usuario> buscarUsuariosPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return usuarioRepository.findAll(pageable);
    }

    // ========================================
    // EJERCICIO 4: CONTADOR DE INTENTOS FALLIDOS Y BLOQUEO
    // ========================================
    public Optional<Usuario> loginConIntentos(String username, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) return Optional.empty();

        Usuario usuario = usuarioOpt.get();

        // Bloqueo por usuario desactivado o bloqueado
        if (!usuario.getActivo() || usuario.isBloqueado()) {
            log.warn("Usuario bloqueado o desactivado: {}", username);
            return Optional.empty();
        }

        // Contraseña correcta
        if (passwordUtil.verifyPassword(password, usuario.getPasswordHash())) {
            usuario.registrarLoginExitoso(); // actualiza último login y resetea intentos
            usuarioRepository.save(usuario);
            log.info("Login exitoso: {}", username);
            return Optional.of(usuario);
        }

        // Contraseña incorrecta -> incrementar intentos fallidos y bloquear si corresponde
        usuario.incrementarIntentosFallidos();
        usuarioRepository.save(usuario);

        if (usuario.isBloqueado()) {
            log.warn("Usuario {} bloqueado por 3 intentos fallidos", username);
        }

        return Optional.empty();
    }
}
