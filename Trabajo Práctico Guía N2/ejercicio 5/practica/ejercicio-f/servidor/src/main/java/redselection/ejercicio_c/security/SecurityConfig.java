package redselection.ejercicio_c.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import redselection.ejercicio_c.repositories.UsuarioRepository;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String API = "/api/v1/**";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService,
                                           UserDetailsService userDetailsService) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        http
                // CSRF se desactiva a propósito: la API es stateless, no usa cookies de sesión; el token viaja
                // en el header Authorization, que el navegador no agrega solo (por eso no hay ataque CSRF).
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        // Públicos: solo login y registro
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                        // Preflight CORS de los navegadores
                        .requestMatchers(HttpMethod.OPTIONS, API).permitAll()
                        // Autorización por rol
                        .requestMatchers(HttpMethod.GET, API).hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, API).hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, API).hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, API).hasRole("ADMIN")
                        // Todo lo demás (incluidos los endpoints que auto-expone Spring Data REST) queda bloqueado
                        .anyRequest().denyAll())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                escribirJson(res, HttpServletResponse.SC_UNAUTHORIZED,
                                        "No autenticado: falta el token o es inválido/vencido."))
                        .accessDeniedHandler((req, res, e) ->
                                escribirJson(res, HttpServletResponse.SC_FORBIDDEN,
                                        "Acceso denegado: tu rol no tiene permiso para esta operación.")))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository repo) {
        return username -> repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private static void escribirJson(HttpServletResponse res, int status, String mensaje) throws java.io.IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.getWriter().write("{\"error\":\"" + mensaje + "\"}");
    }
}
