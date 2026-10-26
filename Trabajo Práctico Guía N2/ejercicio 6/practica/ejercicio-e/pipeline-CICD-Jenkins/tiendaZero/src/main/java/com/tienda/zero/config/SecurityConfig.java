package com.tienda.zero.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/mercadopago/webhook"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**", "/inventory/**", "/reports/**", "/docs/**", "/products/**")
                        .hasAnyRole("ADMINISTRATIVO", "JEFE")
                        .requestMatchers("/mercadopago/webhook").permitAll()
                        .requestMatchers("/completar-perfil", "/perfil-completo", "/cart/**", "/cart", "/checkout/**", "/orders/**").authenticated()
                        .anyRequest().permitAll()
                ).formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                )
                .logout(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
