package com.tienda.zero.config;

import com.tienda.zero.enums.TipoUsuario;
import com.tienda.zero.repository.UsuarioRepository;
import com.tienda.zero.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/*
 * Crea usuarios de demostración para probar los roles del sistema:
 * un ADMINISTRATIVO y un JEFE. Ambos tienen acceso al panel de administración.
 * Solo se crean si todavía no existen. Las credenciales pueden cambiarse desde
 * application.properties (app.demo.*); si no se definen se usan los valores por defecto.
 * IMPORTANTE: son datos de prueba, no deben usarse en producción.
 */
@Component
@Order(1)
public class UsuariosDemo implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final String administrativoUsuario;
    private final String administrativoClave;
    private final String jefeUsuario;
    private final String jefeClave;

    public UsuariosDemo(UsuarioRepository usuarioRepository, UsuarioService usuarioService,
                        @Value("${app.demo.administrativo.usuario:administrativo@tiendazero.com}") String administrativoUsuario,
                        @Value("${app.demo.administrativo.clave:admin1234}") String administrativoClave,
                        @Value("${app.demo.jefe.usuario:jefe@tiendazero.com}") String jefeUsuario,
                        @Value("${app.demo.jefe.clave:jefe1234}") String jefeClave) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.administrativoUsuario = administrativoUsuario;
        this.administrativoClave = administrativoClave;
        this.jefeUsuario = jefeUsuario;
        this.jefeClave = jefeClave;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste(administrativoUsuario, administrativoClave, TipoUsuario.ADMINISTRATIVO);
        crearSiNoExiste(jefeUsuario, jefeClave, TipoUsuario.JEFE);
    }

    private void crearSiNoExiste(String usuario, String clave, TipoUsuario rol) {
        if (usuarioRepository.findByNombreUsuario(usuario.trim().toLowerCase()).isEmpty()) {
            usuarioService.crearUsuario(usuario, clave, rol);
        }
    }
}
