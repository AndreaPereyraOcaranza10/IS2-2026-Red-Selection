package com.concurso.abm;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concurso.abm.entity.*;
import com.concurso.abm.patterns.strategy.GoogleIdentityStrategy;
import com.concurso.abm.repository.*;
import com.fasterxml.jackson.databind.*;
import java.util.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired UsuarioRepository users;
  @Autowired PaisRepository paises;
  @Autowired ProvinciaRepository provincias;
  @Autowired DepartamentoRepository departamentos;
  @Autowired LocalidadRepository localidades;
  @Autowired DireccionRepository direcciones;
  @Autowired PersonaRepository personas;
  @Autowired GoogleIdentityStrategy google;
  @Autowired DataSource dataSource;
  String token;
  long localidadId;

  void seed() {
    new ResourceDatabasePopulator(new ClassPathResource("db/migration/V2__catalogo_geografico.sql"))
        .execute(dataSource);
  }

  @BeforeEach
  void setup() throws Exception {
    users.deleteAll();
    personas.deleteAll();
    direcciones.deleteAll();
    localidades.deleteAll();
    departamentos.deleteAll();
    provincias.deleteAll();
    paises.deleteAll();
    seed();
    localidadId = localidades.findByEliminadoFalseOrderByIdAsc().getFirst().getId();
    token = register("user@example.com");
  }

  JsonNode body(MvcResult result) throws Exception {
    return json.readTree(result.getResponse().getContentAsString());
  }

  ResultActions request(String method, String path, Object payload, String bearer)
      throws Exception {
    var req =
        switch (method) {
          case "POST" -> post(path);
          case "PUT" -> put(path);
          case "DELETE" -> delete(path);
          default -> get(path);
        };
    if (bearer != null) req.header("Authorization", "Bearer " + bearer);
    if (payload != null)
      req.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload));
    return mvc.perform(req);
  }

  String register(String email) throws Exception {
    return body(request(
                "POST",
                "/api/auth/register",
                Map.of("email", email, "password", "Password123"),
                null)
            .andExpect(status().isCreated())
            .andReturn())
        .get("token")
        .asText();
  }

  String login(String email, String password) throws Exception {
    return body(request(
                "POST", "/api/auth/login", Map.of("email", email, "password", password), null)
            .andExpect(status().isOk())
            .andReturn())
        .get("token")
        .asText();
  }

  Map<String, Object> profile(String email, String name, long locality) {
    return Map.of(
        "email",
        email,
        "nombre",
        name,
        "apellido",
        "Pérez",
        "dni",
        "12345678",
        "fechaNacimiento",
        "1995-06-15",
        "direccion",
        Map.of("calle", "Belgrano", "numeracion", "42", "localidadId", locality));
  }

  @Test
  void registrationAlwaysCreatesPersonalAccount() throws Exception {
    request("GET", "/api/usuarios/me", null, token)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rol").value("USER"))
        .andExpect(jsonPath("$.persona").isEmpty());
    request(
            "POST",
            "/api/auth/register",
            Map.of("email", "evil@example.com", "password", "Password123", "rol", "ADMIN"),
            null)
        .andExpect(status().isBadRequest());
  }

  @Test
  void catalogIsAuthenticatedReadOnlyAndContainsNoPersonalData() throws Exception {
    request("GET", "/api/catalogos/geografia", null, null).andExpect(status().isUnauthorized());
    request("GET", "/api/catalogos/geografia", null, token)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paises.length()").value(1))
        .andExpect(jsonPath("$.provincias.length()").value(4))
        .andExpect(jsonPath("$.localidades.length()").value(4))
        .andExpect(jsonPath("$.personas").doesNotExist())
        .andExpect(jsonPath("$.direcciones").doesNotExist());
    request("POST", "/api/catalogos/geografia", Map.of(), token).andExpect(status().isForbidden());
  }

  @Test
  void noManagementEndpointsEvenForLegacyAdmin() throws Exception {
    Usuario old = users.findByEmail("user@example.com").orElseThrow();
    old.setRol("ADMIN");
    users.saveAndFlush(old);
    for (String path :
        List.of(
            "paises",
            "provincias",
            "departamentos",
            "localidades",
            "direcciones",
            "personas",
            "dashboard")) {
      for (String method : List.of("GET", "POST", "PUT", "DELETE")) {
        request(
                method,
                "/api/" + path,
                method.equals("POST") || method.equals("PUT") ? Map.of() : null,
                token)
            .andExpect(status().isForbidden());
      }
    }
  }

  @Test
  void profileCanBeCreatedUpdatedAndReloaded() throws Exception {
    request("PUT", "/api/usuarios/me", profile("user@example.com", "Primero", localidadId), token)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.persona.nombre").value("Primero"));
    request(
            "PUT",
            "/api/usuarios/me",
            profile("updated@example.com", "Actualizado", localidadId),
            token)
        .andExpect(status().isOk());
    request("GET", "/api/usuarios/me", null, token)
        .andExpect(jsonPath("$.persona.nombre").value("Actualizado"))
        .andExpect(jsonPath("$.persona.calle").value("Belgrano"))
        .andExpect(jsonPath("$.persona.dni").value("12345678"))
        .andExpect(jsonPath("$.persona.fechaNacimiento").value("1995-06-15"))
        .andExpect(jsonPath("$.email").value("updated@example.com"));
    assertEquals(1, personas.count());
    assertEquals(1, direcciones.count());
  }

  @Test
  void personalDocumentAndBirthDateAreValidated() throws Exception {
    for (String dni : List.of("", "123456", "123456789", "12.345.678", "ABCDEFGH")) {
      var invalid = new HashMap<>(profile("user@example.com", "Prueba", localidadId));
      invalid.put("dni", dni);
      request("PUT", "/api/usuarios/me", invalid, token).andExpect(status().isBadRequest());
    }
    for (String date : List.of("", "2099-01-01", "2001-02-29", "15/06/1995")) {
      var invalid = new HashMap<>(profile("user@example.com", "Prueba", localidadId));
      invalid.put("fechaNacimiento", date);
      request("PUT", "/api/usuarios/me", invalid, token).andExpect(status().isBadRequest());
    }
    var missing = new HashMap<>(profile("user@example.com", "Prueba", localidadId));
    missing.remove("dni");
    missing.remove("fechaNacimiento");
    request("PUT", "/api/usuarios/me", missing, token).andExpect(status().isBadRequest());
    assertEquals(0, personas.count());
  }

  @Test
  void personalDocumentAndBirthDateCanBeUpdatedWithoutLosingLeadingZeros() throws Exception {
    request("PUT", "/api/usuarios/me", profile("user@example.com", "Prueba", localidadId), token)
        .andExpect(status().isOk());
    var updated = new HashMap<>(profile("user@example.com", "Prueba", localidadId));
    updated.put("dni", "01234567");
    updated.put("fechaNacimiento", "2000-02-29");
    request("PUT", "/api/usuarios/me", updated, token).andExpect(status().isOk());
    request("GET", "/api/usuarios/me", null, token)
        .andExpect(jsonPath("$.persona.dni").value("01234567"))
        .andExpect(jsonPath("$.persona.fechaNacimiento").value("2000-02-29"));
  }

  @Test
  void ownIdentityCannotBeOverriddenAndOtherAccountsRemainUntouched() throws Exception {
    String other = register("other@example.com");
    Map<String, Object> forged = new HashMap<>(profile("user@example.com", "Propio", localidadId));
    forged.put("id", users.findByEmail("other@example.com").orElseThrow().getId());
    request("PUT", "/api/usuarios/me", forged, token).andExpect(status().isBadRequest());
    request("PUT", "/api/usuarios/me", profile("user@example.com", "Propio", localidadId), token)
        .andExpect(status().isOk());
    request("GET", "/api/usuarios/me", null, other).andExpect(jsonPath("$.persona").isEmpty());
    request("PUT", "/api/usuarios/me", profile("other@example.com", "Propio", localidadId), token)
        .andExpect(status().isConflict());
  }

  @Test
  void profileCopiesSharedPersonalDataBeforeEditing() throws Exception {
    request("PUT", "/api/usuarios/me", profile("user@example.com", "Original", localidadId), token)
        .andExpect(status().isOk());
    String other = register("other@example.com");
    Usuario u = users.findByEmail("other@example.com").orElseThrow();
    u.setPersona(users.findByEmail("user@example.com").orElseThrow().getPersona());
    users.saveAndFlush(u);
    request(
            "PUT",
            "/api/usuarios/me",
            profile("user@example.com", "Independiente", localidadId),
            token)
        .andExpect(status().isOk());
    request("GET", "/api/usuarios/me", null, other)
        .andExpect(jsonPath("$.persona.nombre").value("Original"));
  }

  @Test
  void invalidAndInactiveLocationsCannotBeSelected() throws Exception {
    request("PUT", "/api/usuarios/me", profile("user@example.com", "Prueba", 999999), token)
        .andExpect(status().isNotFound());
    var local = localidades.findById(localidadId).orElseThrow();
    local.setEliminado(true);
    localidades.saveAndFlush(local);
    request("PUT", "/api/usuarios/me", profile("user@example.com", "Prueba", localidadId), token)
        .andExpect(status().isNotFound());
    request("GET", "/api/catalogos/geografia", null, token)
        .andExpect(jsonPath("$.localidades.length()").value(3));
  }

  @Test
  void inactiveAncestorIsHiddenAndRejected() throws Exception {
    var province = provincias.findByEliminadoFalseOrderByIdAsc().getFirst();
    province.setEliminado(true);
    provincias.saveAndFlush(province);
    request("PUT", "/api/usuarios/me", profile("user@example.com", "Prueba", localidadId), token)
        .andExpect(status().isBadRequest());
    request("GET", "/api/catalogos/geografia", null, token)
        .andExpect(jsonPath("$.provincias.length()").value(3))
        .andExpect(jsonPath("$.departamentos.length()").value(3))
        .andExpect(jsonPath("$.localidades.length()").value(3));
  }

  @Test
  void passwordChangeValidatesAndRevokesAllOldTokens() throws Exception {
    String second = login("user@example.com", "Password123");
    request(
            "PUT",
            "/api/usuarios/me/password",
            Map.of("actual", "wrong", "nueva", "Password456", "confirmacion", "Password456"),
            token)
        .andExpect(status().isBadRequest());
    request(
            "PUT",
            "/api/usuarios/me/password",
            Map.of("actual", "Password123", "nueva", "Password456", "confirmacion", "mismatch"),
            token)
        .andExpect(status().isBadRequest());
    request(
            "PUT",
            "/api/usuarios/me/password",
            Map.of("actual", "Password123", "nueva", "Password123", "confirmacion", "Password123"),
            token)
        .andExpect(status().isBadRequest());
    request(
            "PUT",
            "/api/usuarios/me/password",
            Map.of("actual", "Password123", "nueva", "Password456", "confirmacion", "Password456"),
            token)
        .andExpect(status().isNoContent());
    request("GET", "/api/usuarios/me", null, token).andExpect(status().isUnauthorized());
    request("GET", "/api/usuarios/me", null, second).andExpect(status().isUnauthorized());
    request(
            "POST",
            "/api/auth/login",
            Map.of("email", "user@example.com", "password", "Password123"),
            null)
        .andExpect(status().isUnauthorized());
    login("user@example.com", "Password456");
  }

  @Test
  void googleOnlyAccountCannotChangeLocalPassword() throws Exception {
    var user = users.findByEmail("user@example.com").orElseThrow();
    user.setPasswordHash(null);
    user.setProveedorAutenticacion("GOOGLE");
    user.setProveedorId("google-subject");
    users.saveAndFlush(user);
    request("GET", "/api/usuarios/me", null, token)
        .andExpect(jsonPath("$.tienePassword").value(false));
    request(
            "PUT",
            "/api/usuarios/me/password",
            Map.of("actual", "Password123", "nueva", "Password456", "confirmacion", "Password456"),
            token)
        .andExpect(status().isBadRequest());
    request("PUT", "/api/usuarios/me", profile("changed@example.com", "Prueba", localidadId), token)
        .andExpect(status().isBadRequest());
  }

  @Test
  void googleStrategyCreatesUserWithoutAutomaticallyLinkingLocalAccounts() {
    Usuario user = google.authenticate("google@example.com", "verified-subject");
    assertEquals("USER", user.getRol());
    assertNull(user.getPasswordHash());
    assertEquals(
        user.getId(), google.authenticate("google@example.com", "verified-subject").getId());
    assertThrows(
        com.concurso.abm.exception.DomainException.class,
        () -> google.authenticate("user@example.com", "other-subject"));
  }

  @Test
  void preloadIsIdempotentAndCreatesOnlyGeographicData() {
    seed();
    seed();
    assertEquals(1, paises.count());
    assertEquals(4, provincias.count());
    assertEquals(4, departamentos.count());
    assertEquals(4, localidades.count());
    assertEquals(0, personas.count());
    assertEquals(0, direcciones.count());
  }

  @Test
  void securityErrorsAreUtf8Json() throws Exception {
    var response =
        request("GET", "/api/usuarios/me", null, "invalid")
            .andExpect(status().isUnauthorized())
            .andReturn();
    assertTrue(
        new String(
                response.getResponse().getContentAsByteArray(),
                java.nio.charset.StandardCharsets.UTF_8)
            .contains("Sesión inválida"));
  }
}
