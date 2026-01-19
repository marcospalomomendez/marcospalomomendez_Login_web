package com.LoginWeb.marcos_Login_web;

import com.LoginWeb.marcos_Login_web.entity.Usuario;
import com.LoginWeb.marcos_Login_web.service.UsuarioService;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.Scanner;

public class Menu {

    private final UsuarioService usuarioService;
    private final Scanner sc;

    public Menu(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        this.sc = new Scanner(System.in);
    }

    public void iniciar() {
        boolean salir = false;

        while (!salir) {
            mostrarMenu();
            int opcion = leerOpcion();

            switch (opcion) {
                case 1 -> crearUsuario();
                case 2 -> listarUsuarios();
                case 3 -> buscarUsuario();
                case 4 -> actualizarEmail();
                case 5 -> desactivarUsuario();
                case 6 -> eliminarUsuario();
                case 7 -> login();
                case 8 -> cambiarPassword();
                case 9 -> listarUsuariosPaginados();
                case 10 -> verificarCredenciales();
                case 0 -> {
                    salir = true;
                    System.out.println("Saliendo del programa...");
                    sc.close();
                    System.exit(0);
                }
                default -> System.out.println("Opción no válida");
            }
        }

        sc.close();
    }

    private void mostrarMenu() {
        System.out.println("\n=== MENÚ DE USUARIOS ===");
        System.out.println("1. Crear usuario");
        System.out.println("2. Listar usuarios activos");
        System.out.println("3. Buscar usuario por username");
        System.out.println("4. Actualizar email de usuario");
        System.out.println("5. Desactivar usuario (borrado lógico)");
        System.out.println("6. Eliminar usuario (borrado físico)");
        System.out.println("7. Login con intentos");
        System.out.println("8. Cambiar contraseña");
        System.out.println("9. Listar usuarios paginados");
        System.out.println("10. Verificar credenciales");
        System.out.println("0. Salir");
        System.out.print("Elige una opción: ");
    }

    private int leerOpcion() {
        int op = -1;
        try {
            op = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Debes ingresar un número");
        }
        return op;
    }

    private void crearUsuario() {
        try {
            System.out.print("Username: ");
            String username = sc.nextLine();
            System.out.print("Email: ");
            String email = sc.nextLine();
            System.out.print("Password: ");
            String password = sc.nextLine();

            Usuario usuario = usuarioService.crearUsuario(username, email, password);
            System.out.println("✅ Usuario creado exitosamente:");
            System.out.println("   ID: " + usuario.getId());
            System.out.println("   Username: " + usuario.getUsername());
            System.out.println("   Email: " + usuario.getEmail());
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void listarUsuarios() {
        System.out.println("\n=== USUARIOS ACTIVOS ===");
        System.out.println("-".repeat(70));
        System.out.printf("%-5s %-15s %-30s %-15s%n", "ID", "USERNAME", "EMAIL", "ÚLTIMO LOGIN");
        System.out.println("-".repeat(70));

        usuarioService.obtenerUsuariosActivos().forEach(u ->
            System.out.printf("%-5d %-15s %-30s %-15s%n",
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getUltimoLogin() != null ? u.getUltimoLogin().toString().substring(0, 16) : "Nunca"
            )
        );
    }

    private void buscarUsuario() {
        System.out.print("Username a buscar: ");
        String username = sc.nextLine();

        Optional<Usuario> usuarioOpt = usuarioService.obtenerPorUsername(username);

        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            System.out.println("✅ Usuario encontrado:");
            System.out.println("   ID: " + u.getId());
            System.out.println("   Username: " + u.getUsername());
            System.out.println("   Email: " + u.getEmail());
            System.out.println("   Activo: " + (u.getActivo() ? "Sí" : "No"));
            System.out.println("   Bloqueado: " + (u.isBloqueado() ? "Sí" : "No"));
            System.out.println("   Intentos fallidos: " + u.getIntentosFallidos());
            System.out.println("   Último login: " + (u.getUltimoLogin() != null ? u.getUltimoLogin() : "Nunca"));
        } else {
            System.out.println("❌ Usuario no encontrado");
        }
    }

    private void actualizarEmail() {
        try {
            System.out.print("Username del usuario a actualizar: ");
            String username = sc.nextLine();

            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorUsername(username);

            if (usuarioOpt.isEmpty()) {
                System.out.println("❌ Usuario no encontrado");
                return;
            }

            Usuario usuario = usuarioOpt.get();
            System.out.print("Nuevo email: ");
            String nuevoEmail = sc.nextLine();

            usuarioService.actualizarUsuario(usuario.getId(), nuevoEmail, null);
            System.out.println("✅ Email actualizado correctamente");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void desactivarUsuario() {
        try {
            System.out.print("Username a desactivar: ");
            String username = sc.nextLine();

            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorUsername(username);

            if (usuarioOpt.isEmpty()) {
                System.out.println("❌ Usuario no encontrado");
                return;
            }

            usuarioService.desactivarUsuario(usuarioOpt.get().getId());
            System.out.println("✅ Usuario desactivado (borrado lógico)");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void eliminarUsuario() {
        try {
            System.out.print("Username a eliminar PERMANENTEMENTE: ");
            String username = sc.nextLine();

            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorUsername(username);

            if (usuarioOpt.isEmpty()) {
                System.out.println("❌ Usuario no encontrado");
                return;
            }

            System.out.print("⚠️ ¿Estás seguro? Esta acción NO se puede deshacer (S/N): ");
            String confirmar = sc.nextLine();

            if (confirmar.equalsIgnoreCase("S")) {
                usuarioService.eliminarUsuario(usuarioOpt.get().getId());
                System.out.println("✅ Usuario eliminado permanentemente");
            } else {
                System.out.println("Operación cancelada");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void login() {
        System.out.print("Username: ");
        String username = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();

        Optional<Usuario> resultado = usuarioService.loginConIntentos(username, password);

        if (resultado.isPresent()) {
            Usuario u = resultado.get();
            System.out.println("✅ Login exitoso");
            System.out.println("   Bienvenido, " + u.getUsername());
            System.out.println("   Último login anterior: " +
                (u.getUltimoLogin() != null ? u.getUltimoLogin() : "Primera vez"));
            usuarioService.actualizarUltimoLogin(u);
        } else {
            System.out.println("❌ Login fallido");

            // Mostrar información del bloqueo si existe
            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorUsername(username);
            if (usuarioOpt.isPresent()) {
                Usuario u = usuarioOpt.get();
                if (u.isBloqueado()) {
                    System.out.println("⚠️ Usuario bloqueado por múltiples intentos fallidos");
                } else if (!u.getActivo()) {
                    System.out.println("⚠️ Usuario desactivado");
                } else {
                    System.out.println("   Intentos fallidos: " + u.getIntentosFallidos() + "/3");
                }
            }
        }
    }

    private void cambiarPassword() {
        try {
            System.out.print("Username: ");
            String username = sc.nextLine();

            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorUsername(username);

            if (usuarioOpt.isEmpty()) {
                System.out.println("❌ Usuario no encontrado");
                return;
            }

            Long id = usuarioOpt.get().getId();

            System.out.print("Password actual: ");
            String actual = sc.nextLine();

            System.out.print("Password nueva: ");
            String nueva = sc.nextLine();

            usuarioService.cambiarPassword(id, actual, nueva);
            System.out.println("✅ Contraseña cambiada correctamente");

        } catch (IllegalArgumentException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void verificarCredenciales() {
        System.out.print("Username: ");
        String username = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();

        Optional<Usuario> resultado = usuarioService.verificarCredenciales(username, password);

        if (resultado.isPresent()) {
            Usuario u = resultado.get();
            System.out.println("✅ Credenciales válidas");
            System.out.println("   Usuario: " + u.getUsername());
            System.out.println("   Email: " + u.getEmail());
            System.out.println("   Último login: " + (u.getUltimoLogin() != null ? u.getUltimoLogin() : "Nunca"));
        } else {
            System.out.println("❌ Credenciales inválidas");
            System.out.println("   Las credenciales no son correctas o el usuario está desactivado");
        }
    }

    private void listarUsuariosPaginados() {
        System.out.print("Tamaño de página (ej: 5): ");
        int tamaño = leerOpcion();

        if (tamaño <= 0) {
            System.out.println("❌ Tamaño inválido");
            return;
        }

        int pagina = 0;
        Page<Usuario> paginaUsuarios;

        do {
            paginaUsuarios = usuarioService.buscarUsuariosPaginados(pagina, tamaño);

            System.out.println("\n=== PÁGINA " + (pagina + 1) + " DE " + paginaUsuarios.getTotalPages() + " ===");
            System.out.println("-".repeat(70));
            System.out.printf("%-5s %-15s %-30s %-8s%n", "ID", "USERNAME", "EMAIL", "ACTIVO");
            System.out.println("-".repeat(70));

            paginaUsuarios.getContent().forEach(u ->
                System.out.printf("%-5d %-15s %-30s %-8s%n",
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getActivo() ? "Sí" : "No"
                )
            );

            if (paginaUsuarios.hasNext()) {
                System.out.print("\n¿Ver siguiente página? (S/N): ");
                String respuesta = sc.nextLine();
                if (!respuesta.equalsIgnoreCase("S")) {
                    break;
                }
                pagina++;
            }

        } while (paginaUsuarios.hasNext());

        System.out.println("\nTotal de usuarios: " + paginaUsuarios.getTotalElements());
    }
}