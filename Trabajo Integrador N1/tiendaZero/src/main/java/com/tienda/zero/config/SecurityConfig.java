package com.tienda.zero.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;

    /**
     * Bean para encriptación de contraseñas con algoritmo BCrypt según requerimiento del sistema.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas accesibles sin autenticación
                .requestMatchers(
                    "/",
                    "/index",
                    "/index.html",
                    "/shop/**",
                    "/producto/**",
                    "/product/**",
                    "/cart/**",
                    "/assets/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/imagen/**",
                    "/auth/**",
                    "/register",
                    "/login",
                    "/admin/signin",
                    "/activar",
                    "/reenviar-codigo",
                    "/404"
                ).permitAll()
                // Panel administrativo: restringido a usuarios con rol ADMIN
                .requestMatchers("/admin/**", "/inventory/**", "/reports/**", "/docs/**", "/products/**").hasRole("ADMIN")
                // Perfil del cliente y checkout: requiere usuario autenticado
                .requestMatchers("/perfil/**", "/cliente/**", "/checkout/**").authenticated()
                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )
            // Configuración del formulario de login
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )
            // Configuración de cierre de sesión
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}

