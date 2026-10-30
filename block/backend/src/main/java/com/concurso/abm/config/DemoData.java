package com.concurso.abm.config;

import com.concurso.abm.entity.Usuario;
import com.concurso.abm.repository.UsuarioRepository;
import com.concurso.abm.validation.PasswordPolicy;
import java.util.Locale;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DemoData {
  @Bean
  CommandLineRunner demo(
      DataSource dataSource,
      UsuarioRepository users,
      PasswordEncoder encoder,
      @Value("${spring.flyway.enabled:true}") boolean flywayEnabled,
      @Value("${DEMO_USER_EMAIL:}") String email,
      @Value("${DEMO_USER_PASSWORD:}") String password) {
    return args -> {
      if (!flywayEnabled)
        new ResourceDatabasePopulator(
                new ClassPathResource("db/migration/V2__catalogo_geografico.sql"))
            .execute(dataSource);
      String normalized = email.strip().toLowerCase(Locale.ROOT);
      if (!normalized.isBlank() && !password.isBlank() && users.findByEmail(normalized).isEmpty()) {
        PasswordPolicy.validate(password);
        Usuario user = new Usuario();
        user.setEmail(normalized);
        user.setPasswordHash(encoder.encode(password));
        user.setRol("USER");
        users.saveAndFlush(user);
      }
    };
  }
}
