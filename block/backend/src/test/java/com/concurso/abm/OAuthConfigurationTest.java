package com.concurso.abm;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "app.google-client-id=test-client-id",
      "app.google-client-secret=test-client-secret"
    })
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OAuthConfigurationTest {
  @Autowired MockMvc mvc;

  @Test
  void googleAuthorizationRedirectHasStateAndOidcScope() throws Exception {
    mvc.perform(get("/api/auth/config"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.google").value(true));
    mvc.perform(get("/oauth2/authorization/google"))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            header()
                .string(
                    "Location",
                    allOf(
                        startsWith("https://accounts.google.com/o/oauth2/v2/auth"),
                        containsString("state="),
                        containsString("scope=openid"),
                        containsString("client_id=test-client-id"))));
  }

  @Test
  void forgedCallbackCannotAuthenticate() throws Exception {
    mvc.perform(get("/login/oauth2/code/google?code=forged&state=forged"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("http://localhost:5173/login?error=oauth"));
    mvc.perform(get("/api/usuarios/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void forwardedHostIsUsedForGoogleCallback() throws Exception {
    mvc.perform(
            get("/oauth2/authorization/google")
                .header("X-Forwarded-Proto", "https")
                .header("X-Forwarded-Host", "cuenta.example.com")
                .header("X-Forwarded-Port", "443"))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            header()
                .string(
                    "Location",
                    containsString(
                        "redirect_uri=https://cuenta.example.com/login/oauth2/code/google")));
  }
}
