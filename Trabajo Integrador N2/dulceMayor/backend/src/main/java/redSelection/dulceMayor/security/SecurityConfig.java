package redSelection.dulceMayor.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import redSelection.dulceMayor.enums.Rol;

import java.util.Arrays;
import java.util.List;

/**
 * Seguridad stateless con JWT: sin sesión, sin CSRF (no hay cookies de sesión que proteger) y con las reglas
 * de acceso por rol. Un empleado solo puede pedir SU ruta: el id de la URL tiene que coincidir con el que
 * viaja en su token (el rol ADMINISTRATIVO puede ver cualquiera).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain cadenaDeSeguridad(HttpSecurity http, JwtService jwtService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> SecurityJson.responder(res, 401, "Unauthorized",
                                "Tu sesión venció. Volvé a ingresar.", "UNAUTHORIZED"))
                        .accessDeniedHandler((req, res, ex) -> SecurityJson.responder(res, 403, "Forbidden",
                                "No tenés permisos para esta operación.", "FORBIDDEN")))
                .authorizeHttpRequests(a -> a
                        // Público
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/actuator/health", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/catalogo/**", "/api/ubicacion/**").permitAll()
                        // Cada empleado ve solo su ruta
                        .requestMatchers(HttpMethod.GET, "/api/rutas/preventista/{id}/hoy").access(propia(Rol.PREVENTISTA))
                        .requestMatchers(HttpMethod.GET, "/api/rutas/transportista/{id}/hoy").access(propia(Rol.TRANSPORTISTA))
                        .requestMatchers(HttpMethod.POST, "/api/rutas/paradas/*/visita").hasRole("PREVENTISTA")
                        .requestMatchers(HttpMethod.POST, "/api/rutas/paradas/*/entrega").hasRole("TRANSPORTISTA")
                        .requestMatchers("/api/rutas/*/orden", "/api/rutas/*/optimizar")
                        .hasAnyRole("PREVENTISTA", "TRANSPORTISTA")
                        .requestMatchers("/api/ruteo/**").hasAnyRole("PREVENTISTA", "TRANSPORTISTA", "ADMINISTRATIVO")
                        // Puntos de venta: el preventista da de alta y ve SOLO los suyos (lo exige PuntoVentaService);
                        // dar de baja es del administrativo
                        .requestMatchers(HttpMethod.DELETE, "/api/puntos-venta/**").hasRole("ADMINISTRATIVO")
                        .requestMatchers("/api/puntos-venta/**").hasAnyRole("PREVENTISTA", "ADMINISTRATIVO")
                        // Responsables: el preventista solo busca (para elegir uno ya cargado); el resto es administración
                        .requestMatchers(HttpMethod.GET, "/api/responsables/buscar").hasAnyRole("PREVENTISTA", "ADMINISTRATIVO")
                        .requestMatchers("/api/responsables/**").hasRole("ADMINISTRATIVO")
                        // Gestión de productos: solo administración
                        .requestMatchers("/api/productos/**").hasRole("ADMINISTRATIVO")
                        // Todo lo demás exige estar logueado (incluye /api/auth/first-login/change-password)
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** El rol indicado y el id de la URL igual al empleadoId del token; ADMINISTRATIVO accede a cualquiera. */
    private AuthorizationManager<RequestAuthorizationContext> propia(Rol rol) {
        return (autenticacion, contexto) -> {
            Authentication auth = autenticacion.get();
            if (auth == null || !(auth.getPrincipal() instanceof UsuarioAutenticado usuario)) {
                return new AuthorizationDecision(false);
            }
            if (usuario.getRol() == Rol.ADMINISTRATIVO) {
                return new AuthorizationDecision(true);
            }
            String id = contexto.getVariables().get("id");
            return new AuthorizationDecision(usuario.getRol() == rol && id != null && id.equals(usuario.getEmpleadoId()));
        };
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.origenes:http://localhost:5173,http://localhost:5174,http://localhost:5175,http://localhost:5176}")
            String origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origenes.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", config);
        return fuente;
    }
}
