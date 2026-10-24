package redselection.ejercicio_c.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import redselection.ejercicio_c.entities.Rol;
import redselection.ejercicio_c.entities.Usuario;
import redselection.ejercicio_c.repositories.UsuarioRepository;

/** Crea el usuario administrador inicial al arrancar (usuario/contraseña vienen del .env). */
@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;

    public AdminInitializer(UsuarioRepository repo, PasswordEncoder encoder,
                            @Value("${security.admin.username:admin}") String username,
                            @Value("${security.admin.password:}") String password) {
        this.repo = repo;
        this.encoder = encoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (password.isBlank()) {
            log.warn("ADMIN_PASSWORD no está definida: no se crea el usuario administrador inicial.");
            return;
        }
        if (repo.existsByUsername(username)) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setUsername(username);
        admin.setPassword(encoder.encode(password));
        admin.setRol(Rol.ADMIN);
        repo.save(admin);
        log.info("Usuario administrador '{}' creado.", username);
    }
}
