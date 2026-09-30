package com.tienda.zero;

import com.tienda.zero.model.Empresa;
import com.tienda.zero.model.Localidad;
import com.tienda.zero.repository.EmpresaRepository;
import com.tienda.zero.repository.LocalidadRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmpresaAdminIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private LocalidadRepository localidadRepository;

    @Test
    void administraEmpresasYLasOfreceEnElAltaDeEmpleados() throws Exception {
        Localidad localidad = localidadRepository.findAll().stream()
                .filter(item -> !item.isEliminado())
                .findFirst()
                .orElseThrow();
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        String razonSocial = "Empresa " + sufijo;
        String cuit = "30" + String.format("%09d", Math.floorMod(UUID.randomUUID().hashCode(), 1_000_000_000));
        var administrativo = user("administrativo@tiendazero.com").roles("ADMINISTRATIVO");

        mockMvc.perform(get("/admin/empresas/nueva").with(administrativo))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nueva empresa")));

        mockMvc.perform(post("/admin/empresas/nueva")
                        .with(administrativo)
                        .with(csrf())
                        .param("razonSocial", razonSocial)
                        .param("cuit", cuit)
                        .param("tipoEmpresa", "SUCURSAL")
                        .param("calle", "San Martin")
                        .param("numeracion", "123")
                        .param("barrio", "Centro")
                        .param("idProvincia", localidad.getDepartamento().getProvincia().getId())
                        .param("idDepartamento", localidad.getDepartamento().getId())
                        .param("idLocalidad", localidad.getId())
                        .param("medioContacto", "CORREO")
                        .param("tipoContacto", "EMPRESA")
                        .param("email", "empresa-" + sufijo + "@zero.test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/empresas"));

        Empresa empresa = empresaRepository.findFirstByRazonSocialIgnoreCaseAndEliminadoFalse(razonSocial)
                .orElseThrow();

        mockMvc.perform(get("/admin/empresas").with(administrativo))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(razonSocial)))
                .andExpect(content().string(containsString("empresa-" + sufijo + "@zero.test")));

        mockMvc.perform(get("/admin/empresas/{id}/editar", empresa.getId()).with(administrativo))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Editar empresa")))
                .andExpect(content().string(containsString("data-selected=\"" + localidad.getId() + "\"")));

        mockMvc.perform(get("/admin/empleados/nuevo").with(administrativo))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(razonSocial)));

        mockMvc.perform(post("/admin/empresas/{id}/editar", empresa.getId())
                        .with(administrativo)
                        .with(csrf())
                        .param("razonSocial", razonSocial + " Editada")
                        .param("cuit", cuit)
                        .param("tipoEmpresa", "SEDE_CENTRAL")
                        .param("calle", "San Martin")
                        .param("numeracion", "456")
                        .param("idProvincia", localidad.getDepartamento().getProvincia().getId())
                        .param("idDepartamento", localidad.getDepartamento().getId())
                        .param("idLocalidad", localidad.getId())
                        .param("medioContacto", "TELEFONO")
                        .param("tipoContacto", "EMPRESA")
                        .param("telefono", "261 555 1234")
                        .param("tipoTelefono", "FIJO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/empresas"));

        Empresa modificada = empresaRepository.findById(empresa.getId()).orElseThrow();
        assertEquals("Empresa " + sufijo + " Editada", modificada.getRazonSocial());
        assertEquals("456", modificada.getDireccion().getNumeracion());

        mockMvc.perform(post("/admin/empresas/{id}/eliminar", empresa.getId())
                        .with(administrativo)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/empresas"));

        assertTrue(empresaRepository.findById(empresa.getId()).orElseThrow().isEliminado());
    }
}
