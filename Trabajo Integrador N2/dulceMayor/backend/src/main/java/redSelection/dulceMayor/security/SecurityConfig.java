package redSelection.dulceMayor.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
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

    /**
     * Jerarquía de roles: el Jefe Administrativo puede todo lo del Administrativo y lo del Preventista.
     * Es static para que Spring la cree temprano (la usan las reglas por URL). Las reglas hasRole("ADMINISTRATIVO")
     * ya lo incluyen sin repetir al jefe en cada una.
     */
    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("JEFE_ADMINISTRATIVO").implies("ADMINISTRATIVO", "PREVENTISTA")
                .build();
    }

    @Bean
    SecurityFilterChain cadenaDeSeguridad(HttpSecurity http, JwtService jwtService,
                                          ControlDeSesion controlDeSesion) throws Exception {
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
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/actuator/health", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/imagenes/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/imagenes").hasRole("ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.GET, "/api/catalogo/**", "/api/ubicacion/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/chatbot/presupuestos").permitAll()
                        // Las imágenes del catálogo se muestran en el sitio público (<img> no manda token); los ids son UUID
                        .requestMatchers(HttpMethod.GET, "/api/imagenes/*").permitAll()
                        // Cada empleado ve solo su ruta (el administrativo y el jefe ven cualquiera).
                        // Nota de diseño: el administrativo NO registra visitas ni entregas, aunque pueda ver rutas;
                        // eso es a propósito: quien opera la ruta es el preventista/transportista dueño de la parada.
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/exchange-rate/**",
                                "/api/locations/**", "/api/companies/branches").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMINISTRATIVO")
                        // Plantillas de correo: las edita administración (el Jefe lo hereda)
                        .requestMatchers("/api/configuracion-correo/**").hasRole("ADMINISTRATIVO")
                        .requestMatchers("/api/newsletter/**").hasRole("JEFE_ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/*/status").hasRole("ADMINISTRATIVO")
                        // Despacho: administración asigna pedidos FACTURADOS a un transportista (el Jefe lo hereda)
                        .requestMatchers("/api/despachos/**").hasRole("ADMINISTRATIVO")
                        // Cada empleado ve solo su ruta
                        .requestMatchers(HttpMethod.GET, "/api/rutas/preventista/{id}/hoy").access(propia(Rol.PREVENTISTA))
                        .requestMatchers(HttpMethod.GET, "/api/rutas/transportista/{id}/hoy").access(propia(Rol.TRANSPORTISTA))
                        .requestMatchers(HttpMethod.POST, "/api/rutas/paradas/*/visita").hasRole("PREVENTISTA")
                        .requestMatchers(HttpMethod.POST, "/api/rutas/paradas/*/entrega").hasRole("TRANSPORTISTA")
                        .requestMatchers("/api/rutas/*/orden", "/api/rutas/*/optimizar")
                        .hasAnyRole("PREVENTISTA", "TRANSPORTISTA")
                        // Entregas: el historial es del propio transportista (la administración ve cualquiera); el detalle y el cobro
                        // los valida EntregaService contra el transportista asignado a ESA entrega.
                        .requestMatchers(HttpMethod.GET, "/api/entregas/transportista/{id}").access(propia(Rol.TRANSPORTISTA))
                        .requestMatchers(HttpMethod.GET, "/api/entregas/pedido/*").hasAnyRole("TRANSPORTISTA", "ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.POST, "/api/entregas/pedido/*/cobro").hasRole("TRANSPORTISTA")
                        .requestMatchers("/api/ruteo/**").hasAnyRole("PREVENTISTA", "TRANSPORTISTA", "ADMINISTRATIVO")
                        // Puntos de venta: el preventista da de alta y ve SOLO los suyos (lo exige PuntoVentaService);
                        // dar de baja es del administrativo
                        .requestMatchers(HttpMethod.DELETE, "/api/puntos-venta/**").hasRole("ADMINISTRATIVO")
                        .requestMatchers("/api/puntos-venta/**").hasAnyRole("PREVENTISTA", "ADMINISTRATIVO")
                        // Responsables: el preventista solo busca (para elegir uno ya cargado); el resto es administración
                        .requestMatchers(HttpMethod.GET, "/api/responsables/buscar").hasAnyRole("PREVENTISTA", "ADMINISTRATIVO")
                        .requestMatchers("/api/responsables/**").hasRole("ADMINISTRATIVO")
                        // Pedidos: el cliente ve los suyos, el preventista carga y ve los de SUS comercios
                        // (lo exige PortalOrderController) y el administrativo ve y carga los de cualquiera
                        // Un pedido no se modifica ni se borra: se anula (PATCH /status). Se cierran los PUT/DELETE heredados.
                        .requestMatchers(HttpMethod.PUT, "/api/orders/*").denyAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/*").denyAll()
                        .requestMatchers("/api/orders", "/api/orders/*")
                        .hasAnyRole("CLIENTE", "PREVENTISTA", "ADMINISTRATIVO")
                        // Clientes y preventistas validan códigos al armar pedidos; el ABM es de administración
                        .requestMatchers(HttpMethod.POST, "/api/promociones/validar").hasAnyRole("CLIENTE", "PREVENTISTA", "ADMINISTRATIVO")
                        .requestMatchers("/api/promociones/**").hasRole("ADMINISTRATIVO")
                        // Factura en PDF: administración, el preventista de ese pedido o el cliente de ese comercio
                        // (la pertenencia la exige FacturaClienteService)
                        .requestMatchers(HttpMethod.GET, "/api/facturas/*/pdf").hasAnyRole("CLIENTE", "PREVENTISTA", "ADMINISTRATIVO")
                        .requestMatchers("/api/profile").hasAnyRole("CLIENTE", "ADMINISTRATIVO")
                        // Pagos: administración inicia, confirma y anula; el cliente solo consulta los de SUS facturas
                        // (esa pertenencia la exige PagoService). El cliente no elige el medio: lo acuerda facturación.
                        .requestMatchers(HttpMethod.GET, "/api/facturas/*/pagos", "/api/pagos/*")
                        .hasAnyRole("CLIENTE", "ADMINISTRATIVO")
                        .requestMatchers("/api/facturas/*/pagos", "/api/facturas/*/pagos/confirmar", "/api/pagos/**")
                        .hasRole("ADMINISTRATIVO")
                        // Solo el Jefe Administrativo: usuarios, estadísticas y reportes globales
                        .requestMatchers("/api/usuarios/**", "/api/estadisticas/**", "/api/reportes/globales/**")
                        .hasRole("JEFE_ADMINISTRATIVO")
                        // Reportes operativos (pantalla, PDF y Excel): administración. Va DESPUÉS de la regla de arriba,
                        // así /api/reportes/globales/** sigue siendo solo del Jefe (la primera regla que coincide gana).
                        .requestMatchers(HttpMethod.GET, "/api/reportes/**").hasRole("ADMINISTRATIVO")
                        // Gestión de productos, categorías e imágenes: solo administración
                        // (las categorías se pueden LEER por empleados de campo; escribir es del administrativo)
                        .requestMatchers(HttpMethod.GET, "/api/categorias/**", "/api/subcategorias/**")
                        .hasAnyRole("PREVENTISTA", "ADMINISTRATIVO")
                        .requestMatchers("/api/categorias/**", "/api/subcategorias/**", "/api/imagenes/**", "/api/productos/**")
                        .hasRole("ADMINISTRATIVO")
                        // Cambio de clave: cualquier usuario logueado (incluso con la clave inicial pendiente)
                        .requestMatchers(HttpMethod.POST, "/api/auth/first-login/change-password", "/api/auth/change-password", "/api/auth/logout")
                        .authenticated()
                        // Deny-by-default: lo que no está declarado arriba se rechaza. Un endpoint nuevo sin regla
                        // explícita queda cerrado (403) en lugar de quedar abierto a cualquier logueado.
                        .anyRequest().denyAll())
                .addFilterBefore(new JwtAuthFilter(jwtService, controlDeSesion), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** El rol indicado y el id de la URL igual al empleadoId del token; ADMINISTRATIVO y JEFE_ADMINISTRATIVO acceden a cualquiera. */
    private AuthorizationManager<RequestAuthorizationContext> propia(Rol rol) {
        return (autenticacion, contexto) -> {
            Authentication auth = autenticacion.get();
            if (auth == null || !(auth.getPrincipal() instanceof UsuarioAutenticado usuario)) {
                return new AuthorizationDecision(false);
            }
            if (usuario.getRol().esAdministracion()) {
                return new AuthorizationDecision(true);
            }
            String id = contexto.getVariables().get("id");
            return new AuthorizationDecision(usuario.getRol() == rol && id != null && id.equals(usuario.getEmpleadoId()));
        };
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.origenes:http://localhost:5173,http://localhost:5174,http://localhost:5175,http://localhost:5176,http://127.0.0.1:5173,http://127.0.0.1:5174,http://127.0.0.1:5175,http://127.0.0.1:5176}")
            String origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origenes.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Content-Disposition"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", config);
        return fuente;
    }
}
