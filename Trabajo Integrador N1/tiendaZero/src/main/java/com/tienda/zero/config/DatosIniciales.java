package com.tienda.zero.config;

import com.tienda.zero.enums.TipoUsuario;
import com.tienda.zero.repository.UsuarioRepository;
import com.tienda.zero.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/*
 * Crea el usuario administrador inicial al arrancar la aplicación, si todavía no existe.
 * Resuelve el primer acceso al sistema: sin este usuario nadie podría entrar
 * a la administración para crear los demás.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final String usuarioAdmin;
    private final String claveAdmin;

    public DatosIniciales(UsuarioRepository usuarioRepository, UsuarioService usuarioService,
                          @Value("${app.admin.usuario}") String usuarioAdmin,
                          @Value("${app.admin.clave}") String claveAdmin) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.usuarioAdmin = usuarioAdmin;
        this.claveAdmin = claveAdmin;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByNombreUsuario(usuarioAdmin).isEmpty()) {
            usuarioService.crearUsuario(usuarioAdmin, claveAdmin, TipoUsuario.JEFE);
        }
    }
}