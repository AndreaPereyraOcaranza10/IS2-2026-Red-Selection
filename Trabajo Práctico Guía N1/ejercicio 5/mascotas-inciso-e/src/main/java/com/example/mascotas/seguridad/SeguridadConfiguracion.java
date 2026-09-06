package com.example.mascotas;

import com.example.mascotas.servicios.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SeguridadConfiguracion {

    @Autowired
    public UsuarioServicio usuarioServicio;

    // En lugar de usar configureGlobal, exponemos el codificador como un @Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Configuración de Headers (para permitir iframes si usas H2 console o similar)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )

                // 2. Autorización de rutas (reemplaza a antMatchers)
                .authorizeHttpRequests(auth -> auth
                        // Asegúrate de usar /** para abarcar todos los archivos y subcarpetas
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/vendor/**").permitAll()
                        // Añade tus otras rutas públicas aquí (como /login, /registrar)
                        .anyRequest().authenticated()
                )

                // 3. Configuración del Login
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/logincheck")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/inicio")
                        .permitAll()
                )

                // 4. Configuración del Logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }

}
