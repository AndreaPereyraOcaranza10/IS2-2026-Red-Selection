package com.tienda.zero.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/shop/**",
                    "/producto/**",
                    "/cart/**",
                    "/checkout/**",
                    "/assets/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/imagen/**",
                    "/auth/**",
                    "/register",
                    "/login"
                ).permitAll()
                .anyRequest().permitAll() // Permitimos navegación pública durante el desarrollo de la plantilla
            );

        return http.build();
    }
}
