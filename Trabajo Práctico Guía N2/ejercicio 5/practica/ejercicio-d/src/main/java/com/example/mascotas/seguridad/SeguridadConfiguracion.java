package com.example.mascotas.seguridad;

import com.example.mascotas.servicios.UsuarioServicio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SeguridadConfiguracion {

    // Ya no hay @Autowired de campo en esta clase — todo entra por parámetro en cada @Bean.

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UsuarioDetalleServicio usuarioDetalleServicio) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetalleServicio);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(UsuarioServicio usuarioServicio, JwtService jwtService) {
        return new LoginExitosoHandler(usuarioServicio, jwtService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationSuccessHandler authenticationSuccessHandler,
                                           JwtService jwtService, UsuarioDetalleServicio usuarios,
                                           UsuarioServicio usuarioServicio) throws Exception {
        http
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(cache -> cache.disable())
                // Se mantiene CSRF para los formularios y la cookie JWT del navegador.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/auth/login").ignoringRequestMatchers(
                        request -> {
                            String header = request.getHeader("Authorization");
                            return header != null && header.startsWith("Bearer ");
                        }))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, usuarios, usuarioServicio),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, ex) -> {
                    if (request.getHeader("Authorization") != null) {
                        response.sendError(401);
                    } else {
                        response.sendRedirect(request.getContextPath() + "/login");
                    }
                }))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/vendor/**", "/scss/**").permitAll()
                        .requestMatchers("/", "/index", "/login", "/registro", "/registrar", "/auth/login", "/error").permitAll()
                        .requestMatchers("/reportes/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/logincheck")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/inicio", true)
                        .successHandler(authenticationSuccessHandler)
                        .failureUrl("/login?error")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "usuarioRecordado", JwtService.COOKIE_NAME)
                        .permitAll()
                );

        return http.build();
    }
}
