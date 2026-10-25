package com.example.mascotas.seguridad;

import com.example.mascotas.controladores.AuthController;
import com.example.mascotas.dto.UsuarioDTO;
import com.example.mascotas.entidades.Usuario;
import com.example.mascotas.enumeracion.Rol;
import com.example.mascotas.servicios.UsuarioServicio;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringJUnitConfig(JwtSecurityTests.Config.class)
@WebAppConfiguration
class JwtSecurityTests {
    @Configuration
    @EnableWebMvc
    @Import({SeguridadConfiguracion.class, AuthController.class, ProtectedController.class})
    static class Config {
        @Bean JwtService jwtService() { return new JwtService("", 3600); }
    }

    @RestController
    static class ProtectedController {
        @GetMapping({"/inicio", "/reportes/prueba"})
        String protegido() { return "ok"; }
    }

    @Autowired WebApplicationContext context;
    @Autowired JwtService jwtService;
    @MockitoBean UsuarioDetalleServicio usuarios;
    @MockitoBean UsuarioServicio usuarioServicio;
    @Autowired PasswordEncoder encoder;
    MockMvc mvc;
    UsuarioDetalle user;

    @BeforeEach
    void setup() {
        mvc = webAppContextSetup(context).apply(springSecurity()).build();
        reset(usuarios, usuarioServicio);
        Usuario usuario = new Usuario();
        usuario.setId("usuario-1");
        usuario.setMail("usuario@example.com");
        usuario.setClave(encoder.encode("clave1234"));
        usuario.setRoles(Set.of(Rol.USUARIO_REGISTRADO));
        user = new UsuarioDetalle(usuario);
        when(usuarios.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(usuarioServicio.convertirDTO(any())).thenReturn(new UsuarioDTO());
    }

    @Test
    void loginJsonEmiteTokenYRechazaClaveIncorrecta() throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"usuario@example.com\",\"password\":\"clave1234\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isString())
                .andReturn().getResponse().getContentAsString();
        String token = body.substring(body.indexOf(":\"") + 2, body.lastIndexOf('"'));
        assertEquals(user.getUsername(), jwtService.getUsernameFromToken(token));
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"usuario@example.com\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bearerValidoPermiteAccesoPeroRespetaRoles() throws Exception {
        String token = jwtService.getToken(user, 3600);
        mvc.perform(get("/inicio").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/reportes/prueba").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        user.getUsuario().setRoles(Set.of(Rol.ADMIN));
        mvc.perform(get("/reportes/prueba").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void rechazaTokensVencidosAlteradosYOtraFirma() throws Exception {
        String token = jwtService.getToken(user, 3600);
        String[] invalidos = {jwtService.getToken(user, -60), "no-es-jwt",
                token.substring(0, token.lastIndexOf('.') + 1) + "AAAA",
                new JwtService("", 3600).getToken(user, 3600)};
        for (String invalido : invalidos) {
            mvc.perform(get("/inicio").header("Authorization", "Bearer " + invalido))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void usuarioDeshabilitadoNoPuedeUsarToken() throws Exception {
        String token = jwtService.getToken(user, 3600);
        user.getUsuario().setBaja(new Date());
        mvc.perform(get("/inicio").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sesionPreviaNoReemplazaElToken() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuariosession", new UsuarioDTO());
        mvc.perform(get("/inicio").session(session)).andExpect(status().is3xxRedirection());
        assertNull(session.getAttribute("usuariosession"));
    }

    @Test
    void loginWebUsaCookieJwtYLogoutLaElimina() throws Exception {
        var response = mvc.perform(post("/logincheck").with(csrf())
                        .param("username", user.getUsername()).param("password", "clave1234")
                        .param("recordarme", "on"))
                .andExpect(redirectedUrl("/inicio")).andReturn().getResponse();
        Cookie cookie = response.getCookie(JwtService.COOKIE_NAME);
        assertNotNull(cookie);
        assertTrue(cookie.isHttpOnly());
        assertEquals(172800, cookie.getMaxAge());
        assertEquals(user.getUsername(), jwtService.getUsernameFromToken(cookie.getValue()));
        mvc.perform(get("/inicio").cookie(cookie)).andExpect(status().isOk());
        mvc.perform(post("/logout").cookie(cookie).with(csrf()))
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(cookie().maxAge(JwtService.COOKIE_NAME, 0));
        mvc.perform(post("/logout").cookie(cookie)).andExpect(status().isForbidden());
    }

    @Test
    void cookieVencidaPermiteVolverAlLogin() throws Exception {
        mvc.perform(get("/inicio").cookie(new Cookie(JwtService.COOKIE_NAME,
                        jwtService.getToken(user, -60))))
                .andExpect(status().is3xxRedirection())
                .andExpect(cookie().maxAge(JwtService.COOKIE_NAME, 0));
    }
}
