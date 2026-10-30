package com.concurso.abm.security;

import com.concurso.abm.patterns.strategy.GoogleIdentityStrategy;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.*;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.web.cors.*;

@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  SecurityFilterChain filterChain(
      HttpSecurity http,
      JwtFilter filter,
      JwtService jwt,
      GoogleIdentityStrategy google,
      @Value("${app.frontend-url}") String frontend,
      @Value("${app.google-client-id}") String clientId,
      @Value("${app.google-client-secret}") String secret)
      throws Exception {
    http.csrf(c -> c.disable())
        .cors(c -> {})
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .securityContext(s -> s.securityContextRepository(new NullSecurityContextRepository()))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/config",
                        "/oauth2/**",
                        "/login/**",
                        "/actuator/health",
                        "/swagger-ui/**",
                        "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers(
                        "/api/usuarios/me", "/api/usuarios/me/password", "/api/auth/me")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/catalogos/geografia")
                    .authenticated()
                    .anyRequest()
                    .denyAll())
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                        (req, res, ex) -> {
                          res.setStatus(401);
                          res.setCharacterEncoding("UTF-8");
                          res.setContentType("application/json");
                          res.getWriter().write("{\"message\":\"Iniciá sesión para continuar\"}");
                        })
                    .accessDeniedHandler(
                        (req, res, ex) -> {
                          res.setStatus(403);
                          res.setCharacterEncoding("UTF-8");
                          res.setContentType("application/json");
                          res.getWriter()
                              .write(
                                  "{\"message\":\"Operación no disponible en esta aplicación\"}");
                        }))
        .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
    if (!clientId.isBlank() && !secret.isBlank()) {
      var registration =
          CommonOAuth2Provider.GOOGLE
              .getBuilder("google")
              .clientId(clientId)
              .clientSecret(secret)
              .scope("openid", "profile", "email")
              .build();
      http.oauth2Login(
          o ->
              o.clientRegistrationRepository(new InMemoryClientRegistrationRepository(registration))
                  .successHandler(
                      (req, res, auth) -> {
                        try {
                          OidcUser principal = (OidcUser) auth.getPrincipal();
                          if (!Boolean.TRUE.equals(principal.getEmailVerified()))
                            throw new IllegalArgumentException();
                          var u = google.authenticate(principal.getEmail(), principal.getSubject());
                          String token = jwt.issue(u);
                          if (req.getSession(false) != null) req.getSession(false).invalidate();
                          res.sendRedirect(frontend + "/mi-perfil#token=" + token);
                        } catch (Exception ex) {
                          if (req.getSession(false) != null) req.getSession(false).invalidate();
                          res.sendRedirect(frontend + "/login?error=oauth");
                        }
                      })
                  .failureHandler(
                      (req, res, ex) -> res.sendRedirect(frontend + "/login?error=oauth")));
    }
    return http.build();
  }

  @Bean
  CorsConfigurationSource cors(@Value("${app.frontend-url}") String frontend) {
    var c = new CorsConfiguration();
    c.setAllowedOrigins(List.of(frontend));
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    var s = new UrlBasedCorsConfigurationSource();
    s.registerCorsConfiguration("/**", c);
    return s;
  }
}
