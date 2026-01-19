package com.LoginWeb.marcos_Login_web.repository;

import com.LoginWeb.marcos_Login_web.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de Usuario.
 *
 * JpaRepository<Usuario, Long> proporciona:
 * - save(entity): Guardar/actualizar
 * - findById(id): Buscar por ID
 * - findAll(): Obtener todos
 * - deleteById(id): Eliminar por ID
 * - count(): Contar registros
 * - existsById(id): Verificar existencia
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // ========================================
    // QUERY METHODS - Derivados del nombre
    // ========================================

    /**
     * Busca usuario por username exacto.
     * Spring genera: SELECT * FROM usuarios WHERE username = ?
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Busca usuario por email exacto.
     * Spring genera: SELECT * FROM usuarios WHERE email = ?
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Lista todos los usuarios activos.
     * Spring genera: SELECT * FROM usuarios WHERE activo = true
     */
    List<Usuario> findByActivoTrue();

    /**
     * Lista todos los usuarios inactivos.
     * Spring genera: SELECT * FROM usuarios WHERE activo = false
     */
    List<Usuario> findByActivoFalse();

    /**
     * Verifica si existe un username.
     * Spring genera: SELECT COUNT(*) > 0 FROM usuarios WHERE username = ?
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si existe un email.
     * Spring genera: SELECT COUNT(*) > 0 FROM usuarios WHERE email = ?
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuarios cuyo username contenga el texto (LIKE).
     * Spring genera: SELECT * FROM usuarios WHERE username LIKE %?%
     */
    List<Usuario> findByUsernameContainingIgnoreCase(String texto);

    // ========================================
    // QUERIES PERSONALIZADAS CON @Query
    // ========================================

    /**
     * Busca usuarios activos ordenados por fecha de creación.
     * Usa JPQL (Java Persistence Query Language).
     */
    @Query("SELECT u FROM Usuario u WHERE u.activo = true ORDER BY u.fechaCreacion DESC")
    List<Usuario> findUsuariosActivosOrdenados();

    /**
     * Busca usuarios por dominio de email.
     * Usa JPQL con parámetro nombrado.
     */
    @Query("SELECT u FROM Usuario u WHERE u.email LIKE %:dominio")
    List<Usuario> findByDominioEmail(@Param("dominio") String dominio);

    /**
     * Cuenta usuarios activos.
     */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.activo = true")
    long countUsuariosActivos();

    /**
     * Query nativa SQL (cuando JPQL no es suficiente).
     */
    @Query(value = "SELECT * FROM usuarios WHERE DATEDIFF(NOW(), fecha_creacion) <= :dias",
            nativeQuery = true)
    List<Usuario> findUsuariosRecientes(@Param("dias") int dias);
}