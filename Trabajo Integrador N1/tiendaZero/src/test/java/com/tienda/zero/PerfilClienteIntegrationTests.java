package com.tienda.zero;

import com.tienda.zero.enums.Sexo;
import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.enums.TipoUsuario;
import com.tienda.zero.model.Cliente;
import com.tienda.zero.model.ContactoTelefonico;
import com.tienda.zero.model.Direccion;
import com.tienda.zero.model.Localidad;
import com.tienda.zero.model.Nacionalidad;
import com.tienda.zero.model.Usuario;
import com.tienda.zero.repository.LocalidadRepository;
import com.tienda.zero.service.NacionalidadService;
import com.tienda.zero.service.PersonaService;
import com.tienda.zero.service.RegistroService;
import com.tienda.zero.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PerfilClienteIntegrationTests {

    @Autowired
    private RegistroService registroService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private NacionalidadService nacionalidadService;

    @Autowired
    private LocalidadRepository localidadRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void actualizaElPerfilSinModificarLasCredenciales() throws Exception {
        Nacionalidad nacionalidad = nacionalidadService.listarNacionalidadActiva().stream()
                .findFirst()
                .orElseThrow();
        Localidad localidad = localidadRepository.findAll().stream()
                .filter(item -> !item.isEliminado())
                .findFirst()
                .orElseThrow();
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        Usuario usuario = usuarioService.crearUsuario(
                "perfil-" + sufijo + "@zero.test", "clave123", TipoUsuario.CLIENTE);
        String correoOriginal = usuario.getNombreUsuario();
        String claveOriginal = usuario.getClave();

        registroService.completarPerfilCliente(usuario.getId(), "Nombre", "Original", Sexo.OTRO,
                Date.valueOf("1990-01-01"), TipoDocumento.DNI, "DOC-" + sufijo,
                "Domicilio original", nacionalidad.getId(), "Calle Uno", "100", null,
                null, null, null, localidad.getId(), "261 555 0000");

        mockMvc.perform(post("/completar-perfil")
                        .with(user(correoOriginal).roles("CLIENTE"))
                        .with(csrf())
                        .param("nombre", "Nombre")
                        .param("apellido", "Actualizado")
                        .param("sexo", "OTRO")
                        .param("fechaNacimiento", "1990-01-01")
                        .param("tipoDocumento", "DNI")
                        .param("numeroDocumento", "DOC-" + sufijo)
                        .param("direccionEstadia", "Domicilio actualizado")
                        .param("idNacionalidad", nacionalidad.getId())
                        .param("idProvincia", localidad.getDepartamento().getProvincia().getId())
                        .param("idDepartamento", localidad.getDepartamento().getId())
                        .param("idLocalidad", localidad.getId())
                        .param("calle", "Calle Dos")
                        .param("numeracion", "200")
                        .param("barrio", "Centro")
                        .param("manzanaPiso", "2")
                        .param("casaDepartamento", "B")
                        .param("referencia", "Portón negro")
                        .param("telefono", "261 555 1111"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil-completo"));

        Cliente cliente = (Cliente) personaService.buscarPersonaPorUsuario(usuario.getId()).orElseThrow();
        Direccion direccion = cliente.getDirecciones().stream()
                .filter(item -> !item.isEliminado())
                .findFirst()
                .orElseThrow();
        ContactoTelefonico telefono = cliente.getContactos().stream()
                .filter(item -> !item.isEliminado())
                .filter(ContactoTelefonico.class::isInstance)
                .map(ContactoTelefonico.class::cast)
                .findFirst()
                .orElseThrow();
        Usuario usuarioActualizado = usuarioService.buscarUsuario(usuario.getId());

        assertEquals("Actualizado", cliente.getApellido());
        assertEquals("Domicilio actualizado", cliente.getDireccionEstadia());
        assertEquals("Calle Dos", direccion.getCalle());
        assertEquals("200", direccion.getNumeracion());
        assertEquals("261 555 1111", telefono.getTelefono());
        assertEquals(correoOriginal, usuarioActualizado.getNombreUsuario());
        assertEquals(claveOriginal, usuarioActualizado.getClave());
        assertTrue(usuarioActualizado.isCuentaActivada());

        mockMvc.perform(get("/completar-perfil")
                        .with(user(correoOriginal).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("modoEdicion", true))
                .andExpect(content().string(containsString("value=\"Actualizado\"")))
                .andExpect(content().string(containsString("value=\"" + correoOriginal + "\" disabled")))
                .andExpect(content().string(containsString("id=\"guardar-perfil\" type=\"submit\" disabled")))
                .andExpect(content().string(containsString("action=\"/logout\"")))
                .andExpect(content().string(containsString("Cerrar sesión")));

        mockMvc.perform(get("/perfil-completo")
                        .with(user(correoOriginal).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tu perfil ya está completo ¡Hora de comprar!")));
    }

    @Test
    void elAdministradorPuedeSalirDelPanelYVolverAHome() throws Exception {
        mockMvc.perform(get("/")
                        .with(user("administrativo@tiendazero.com").roles("ADMINISTRATIVO")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Panel administrador")))
                .andExpect(content().string(containsString("href=\"/admin\"")));

        mockMvc.perform(get("/admin")
                        .with(user("administrativo@tiendazero.com").roles("ADMINISTRATIVO")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Salir del panel")))
                .andExpect(content().string(containsString("href=\"/admin\"")))
                .andExpect(content().string(containsString("action=\"/logout\"")));
    }
}
