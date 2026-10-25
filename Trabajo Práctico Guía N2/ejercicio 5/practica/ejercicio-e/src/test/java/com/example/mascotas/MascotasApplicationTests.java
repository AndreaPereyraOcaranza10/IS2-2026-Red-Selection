package com.example.mascotas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import com.example.mascotas.entidades.Usuario;
import com.example.mascotas.enumeracion.Rol;
import com.example.mascotas.repositorios.UsuarioRespositorio;
import java.util.Set;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:mascotas;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.security.oauth2.client.registration.mascotas.client-secret=test-secret"
})
@ActiveProfiles("oauth2")
class MascotasApplicationTests {

    // Las pruebas no requieren MySQL ni un proveedor de identidad en ejecución.
    @MockitoBean ClientRegistrationRepository clientes;
    @MockitoBean JwtDecoder decoder;
    @Autowired WebApplicationContext context;
    @Autowired UsuarioRespositorio usuarios;

	@Test
	void contextLoads() {
	}

    @Test
    void loginRenderizaBotonOauth2() throws Exception {
        webAppContextSetup(context).apply(springSecurity()).build().perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/oauth2/authorization/mascotas")))
                .andExpect(content().string(not(containsString("action=\"/logincheck\""))));
    }

    @Test
    void noPermiteModificarElPerfilDeOtraCuenta() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setOauth2Subject("perfil-test-subject");
        usuario.setMail("perfil@example.com");
        usuario.setRoles(Set.of(Rol.USUARIO_REGISTRADO));
        usuario = usuarios.save(usuario);
        try {
            webAppContextSetup(context).apply(springSecurity()).build()
                    .perform(post("/usuario/actualizar-perfil")
                            .with(oidcLogin().idToken(token -> token.subject("perfil-test-subject"))).with(csrf())
                            .param("id", "otra-cuenta").param("nombre", "Nombre").param("apellido", "Apellido")
                            .param("mail", "otro@example.com").param("clave1", "clave1234")
                            .param("clave2", "clave1234").param("idZona", "zona"))
                    .andExpect(status().isForbidden());
        } finally {
            usuarios.deleteById(usuario.getId());
        }
    }

}
