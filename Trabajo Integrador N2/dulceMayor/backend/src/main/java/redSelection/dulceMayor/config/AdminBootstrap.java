package redSelection.dulceMayor.config;

import java.security.SecureRandom;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import redSelection.dulceMayor.enums.ProveedorAuth;
import redSelection.dulceMayor.enums.Rol;
import redSelection.dulceMayor.model.Usuario;
import redSelection.dulceMayor.repository.UsuarioRepository;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String username;
    private final String configuredPassword;

    public AdminBootstrap(
            UsuarioRepository usuarios,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin.enabled:true}") boolean enabled,
            @Value("${app.bootstrap.admin.username:admin@dulcemayor.local}") String username,
            @Value("${app.bootstrap.admin.password:}") String configuredPassword) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.username = username.trim();
        this.configuredPassword = configuredPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled || username.isBlank()) return;
        if (usuarios.findByNombreUsuarioIgnoreCase(username).isPresent()) return;

        boolean generated = configuredPassword == null || configuredPassword.isBlank();
        String password = generated ? generatePassword() : configuredPassword;
        Usuario admin = Usuario.builder()
                .nombreUsuario(username)
                .clave(passwordEncoder.encode(password))
                .rol(Rol.ADMINISTRATIVO)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .activo(true)
                .primerIngreso(false)
                .eliminado(false)
                .build();
        usuarios.save(admin);

        if (generated) {
            log.warn("Cuenta administrativa de desarrollo creada. Usuario: {} | Contraseña temporal: {}. "
                    + "Guardala ahora; no volverá a mostrarse. Configurá ADMIN_BOOTSTRAP_ADMIN_PASSWORD "
                    + "para usar una contraseña propia.", username, password);
        } else {
            log.info("Cuenta administrativa inicial creada para {}.", username);
        }
    }

    private String generatePassword() {
        byte[] bytes = new byte[24];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
