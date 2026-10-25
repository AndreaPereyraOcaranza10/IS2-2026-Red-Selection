package com.example.mascotas.seguridad;

import com.example.mascotas.controladores.OAuth2CuentaController;
import com.example.mascotas.dto.UsuarioDTO;
import com.example.mascotas.entidades.Usuario;
import com.example.mascotas.enumeracion.Rol;
import com.example.mascotas.repositorios.UsuarioRespositorio;
import com.example.mascotas.servicios.UsuarioServicio;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.client.registration.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringJUnitConfig(OAuth2SecurityTests.Config.class)
@WebAppConfiguration
@ActiveProfiles("oauth2")
class OAuth2SecurityTests {
    static final String ISSUER = "https://issuer.example/realms/mascotas";
    static final RSAKey KEY;
    static {
        try { KEY = new RSAKeyGenerator(2048).keyID("test").generate(); }
        catch (Exception ex) { throw new ExceptionInInitializerError(ex); }
    }

    @Configuration
    @EnableWebMvc
    @Import({SeguridadConfiguracion.class, OAuth2SeguridadConfiguracion.class,
            OAuth2UsuarioServicio.class, OAuth2CuentaController.class, ProtectedController.class})
    static class Config {
        @Bean JwtService jwtService() { return new JwtService("", 3600); }

        @Bean ClientRegistrationRepository clientes() {
            return new InMemoryClientRegistrationRepository(ClientRegistration.withRegistrationId("mascotas")
                    .clientId("mascotas-web").clientSecret("secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("openid", "profile", "email")
                    .authorizationUri(ISSUER + "/authorize").tokenUri(ISSUER + "/token")
                    .jwkSetUri(ISSUER + "/certs").userInfoUri(ISSUER + "/userinfo")
                    .userNameAttributeName("sub").issuerUri(ISSUER).build());
        }

        @Bean JwtDecoder decoder() throws Exception {
            var decoder = NimbusJwtDecoder.withPublicKey(KEY.toRSAPublicKey()).build();
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                    JwtValidators.createDefaultWithIssuer(ISSUER),
                    new JwtClaimValidator<List<String>>("aud", aud -> aud != null && aud.contains("mascotas-api"))));
            return decoder;
        }
    }

    @RestController
    static class ProtectedController {
        @GetMapping({"/inicio", "/reportes/prueba"}) String protegido() { return "ok"; }
        @PostMapping("/usuario/prueba") String guardar() { return "ok"; }
    }

    @Autowired WebApplicationContext context;
    @MockitoBean UsuarioRespositorio repositorio;
    @MockitoBean UsuarioServicio usuarios;
    @MockitoBean UsuarioDetalleServicio detalles;
    MockMvc mvc;
    Usuario usuario;

    @BeforeEach
    void setup() {
        mvc = webAppContextSetup(context).apply(springSecurity()).build();
        reset(repositorio, usuarios);
        usuario = new Usuario();
        usuario.setId("local-1");
        usuario.setOauth2Subject("external-1");
        usuario.setRoles(Set.of(Rol.USUARIO_REGISTRADO));
        when(repositorio.findByOauth2Subject("external-1")).thenReturn(Optional.of(usuario));
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId("local-1");
        when(usuarios.convertirDTO(any())).thenReturn(dto);
    }

    String token(String issuer, String audience, Instant expires, RSAKey key) throws Exception {
        var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("test").build(),
                new JWTClaimsSet.Builder().issuer(issuer).subject("external-1").audience(audience)
                        .issueTime(Date.from(Instant.now().minusSeconds(600)))
                        .expirationTime(Date.from(expires)).claim("roles", List.of("ADMIN")).build());
        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }

    String valido() throws Exception { return token(ISSUER, "mascotas-api", Instant.now().plusSeconds(300), KEY); }

    @Test void bearerValidaFirmaYClaimsYEntregaUsuarioLocal() throws Exception {
        mvc.perform(get("/api/me").header("Authorization", "Bearer " + valido()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value("local-1"));
        var other = new RSAKeyGenerator(2048).generate();
        for (String invalid : List.of("invalido", token(ISSUER, "mascotas-api", Instant.now().minusSeconds(300), KEY),
                token("https://otro.example", "mascotas-api", Instant.now().plusSeconds(300), KEY),
                token(ISSUER, "otro-api", Instant.now().plusSeconds(300), KEY),
                token(ISSUER, "mascotas-api", Instant.now().plusSeconds(300), other))) {
            mvc.perform(get("/api/me").header("Authorization", "Bearer " + invalid))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test void rolesSeObtienenDeBaseLocalNoDelToken() throws Exception {
        mvc.perform(get("/reportes/prueba").header("Authorization", "Bearer " + valido()))
                .andExpect(status().isForbidden());
        usuario.setRoles(Set.of(Rol.ADMIN));
        mvc.perform(get("/reportes/prueba").header("Authorization", "Bearer " + valido()))
                .andExpect(status().isOk());
    }

    @Test void rechazaCuentaDeshabilitadaOSinVinculo() throws Exception {
        usuario.setBaja(new Date());
        mvc.perform(get("/api/me").header("Authorization", "Bearer " + valido()))
                .andExpect(status().isUnauthorized());
        when(repositorio.findByOauth2Subject("external-1")).thenReturn(Optional.empty());
        mvc.perform(get("/api/me").header("Authorization", "Bearer " + valido()))
                .andExpect(status().isUnauthorized());
    }

    @Test void navegadorRedirigeYApiDevuelve401SinToken() throws Exception {
        mvc.perform(get("/inicio")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        var session = new MockHttpSession();
        session.setAttribute("usuariosession", new UsuarioDTO());
        mvc.perform(get("/api/me").session(session)).andExpect(status().isUnauthorized());
    }

    @Test void inicioDeAuthorizationCodeUsaStateYPkce() throws Exception {
        var response = mvc.perform(get("/oauth2/authorization/mascotas"))
                .andExpect(status().is3xxRedirection()).andReturn().getResponse();
        String url = response.getRedirectedUrl();
        assertNotNull(url);
        assertTrue(url.startsWith(ISSUER + "/authorize?"));
        for (String part : List.of("response_type=code", "state=", "code_challenge=", "code_challenge_method=S256")) {
            assertTrue(url.contains(part), url);
        }
        mvc.perform(get("/login/oauth2/code/mascotas").param("code", "no-valido").param("state", "incorrecto"))
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test void oidcCargaDtoYReevaluaBajasYRoles() throws Exception {
        mvc.perform(get("/inicio").with(oidcLogin().idToken(token -> token.subject("external-1"))))
                .andExpect(status().isOk()).andExpect(request().sessionAttribute("usuariosession", org.hamcrest.Matchers.instanceOf(UsuarioDTO.class)));
        mvc.perform(get("/reportes/prueba").with(oidcLogin().idToken(token -> token.subject("external-1"))))
                .andExpect(status().isForbidden());
        usuario.setRoles(Set.of(Rol.ADMIN));
        mvc.perform(get("/reportes/prueba").with(oidcLogin().idToken(token -> token.subject("external-1"))))
                .andExpect(status().isOk());
        usuario.setBaja(new Date());
        mvc.perform(get("/inicio").with(oidcLogin().idToken(token -> token.subject("external-1"))))
                .andExpect(status().isForbidden());
    }

    @Test void csrfProtegeFormulariosYLogoutPeroBearerNoLoNecesita() throws Exception {
        mvc.perform(post("/usuario/prueba").with(oidcLogin().idToken(token -> token.subject("external-1"))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/usuario/prueba").with(oidcLogin().idToken(token -> token.subject("external-1"))).with(csrf()))
                .andExpect(status().isOk());
        mvc.perform(post("/usuario/prueba").header("Authorization", "Bearer " + valido()))
                .andExpect(status().isOk());
        mvc.perform(post("/logout").with(oidcLogin())).andExpect(status().isForbidden());
        mvc.perform(post("/logout").with(oidcLogin()).with(csrf()))
                .andExpect(redirectedUrl("/login?logout")).andExpect(cookie().maxAge("JSESSIONID", 0));
    }

    @Test void sesionOidcNoAutenticaApiYEndpointsViejosNoEmitenJwt() throws Exception {
        var session = (MockHttpSession) mvc.perform(get("/inicio")
                        .with(oidcLogin().idToken(token -> token.subject("external-1"))))
                .andReturn().getRequest().getSession(false);
        mvc.perform(get("/api/me").session(session))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/login").with(csrf())).andExpect(status().is3xxRedirection());
        mvc.perform(get("/inicio").cookie(new jakarta.servlet.http.Cookie(JwtService.COOKIE_NAME, "viejo")))
                .andExpect(status().is3xxRedirection());
    }
}
