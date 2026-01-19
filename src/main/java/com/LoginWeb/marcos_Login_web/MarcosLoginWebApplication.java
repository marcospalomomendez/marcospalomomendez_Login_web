package com.LoginWeb.marcos_Login_web;

import com.LoginWeb.marcos_Login_web.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MarcosLoginWebApplication {


    public static void main(String[] args) {
        SpringApplication.run(MarcosLoginWebApplication.class, args);
    }

    @Bean
    CommandLineRunner run(UsuarioService usuarioService) {
        return args -> {
            Menu menu = new Menu(usuarioService);
            menu.iniciar(); // Arranca el menú interactivo
        };
    }
}
