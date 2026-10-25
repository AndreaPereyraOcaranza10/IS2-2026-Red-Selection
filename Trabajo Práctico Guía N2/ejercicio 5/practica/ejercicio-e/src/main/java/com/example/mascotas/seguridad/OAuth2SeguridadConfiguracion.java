package com.example.mascotas.seguridad;

import com.example.mascotas.servicios.UsuarioServicio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@Profile("oauth2")
public class OAuth2SeguridadConfiguracion {
    @Bean
    @Order(1)
    SecurityFilterChain oauth2Recursos(HttpSecurity http, OAuth2UsuarioServicio identidades,
                                       UsuarioServicio usuarios) throws Exception {
        http.securityMatcher(request -> request.getHeader("Authorization") != null
                        || PathPatternRequestMatcher.withDefaults().matcher("/api/**").matches(request))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(cache -> cache.disable())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/reportes/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(token -> {
                    var local = identidades.buscar(token.getSubject());
                    return new JwtAuthenticationToken(token, local.getAuthorities());
                })))
                .addFilterBefore(new OAuth2UsuarioFilter(identidades, usuarios), AuthorizationFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain oauth2Navegador(HttpSecurity http, OAuth2UsuarioServicio identidades,
                                        UsuarioServicio usuarios, ClientRegistrationRepository clientes) throws Exception {
        var resolver = new DefaultOAuth2AuthorizationRequestResolver(clientes, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index", "/login", "/registro", "/registrar", "/error",
                                "/css/**", "/js/**", "/img/**", "/vendor/**",
                                "/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/auth/**", "/logincheck").denyAll()
                        .requestMatchers("/reportes/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth.loginPage("/login")
                        .authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(resolver))
                        .userInfoEndpoint(info -> info.oidcUserService(request -> {
                            var oidc = new OidcUserService().loadUser(request);
                            var local = identidades.buscar(oidc.getSubject());
                            return new DefaultOidcUser(local.getAuthorities(), oidc.getIdToken(), oidc.getUserInfo());
                        }))
                        .defaultSuccessUrl("/inicio", true)
                        .failureUrl("/login?error"))
                .logout(logout -> logout.logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true).deleteCookies("JSESSIONID", JwtService.COOKIE_NAME))
                .addFilterBefore(new OAuth2UsuarioFilter(identidades, usuarios), AuthorizationFilter.class);
        return http.build();
    }
}
