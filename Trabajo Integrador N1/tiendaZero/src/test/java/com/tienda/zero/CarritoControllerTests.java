package com.tienda.zero;

import com.tienda.zero.controller.TiendaController;
import com.tienda.zero.service.FlujoCompraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CarritoControllerTests {
    @Mock
    private FlujoCompraService flujoCompraService;
    @InjectMocks
    private TiendaController controller;
    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void agregarDesdeElCatalogoConfirmaSinRedirigirAlCarrito() throws Exception {
        mvc.perform(post("/cart/add").param("productId", "producto-1").param("quantity", "2")
                        .principal(new UsernamePasswordAuthenticationToken("cliente", "clave"))
                        .header("X-Requested-With", "XMLHttpRequest").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(header().doesNotExist("Location"));
        verify(flujoCompraService, times(1)).agregarAlCarrito("cliente", "producto-1", 2);
    }

    @Test
    void stockInsuficienteDevuelveErrorEnLugarDeConfirmar() throws Exception {
        when(flujoCompraService.agregarAlCarrito("cliente", "producto-1", 1))
                .thenThrow(new IllegalArgumentException("Stock insuficiente"));
        mvc.perform(post("/cart/add").param("productId", "producto-1")
                        .principal(new UsernamePasswordAuthenticationToken("cliente", "clave"))
                        .header("X-Requested-With", "XMLHttpRequest").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Stock insuficiente"));
        verify(flujoCompraService, times(1)).agregarAlCarrito("cliente", "producto-1", 1);
    }

    @Test
    void formularioNormalConservaLaRedireccion() throws Exception {
        mvc.perform(post("/cart/add").param("productId", "producto-1")
                        .principal(new UsernamePasswordAuthenticationToken("cliente", "clave")))
                .andExpect(redirectedUrl("/cart"))
                .andExpect(flash().attribute("mensajeExito", "Producto agregado al carrito."));
        verify(flujoCompraService, times(1)).agregarAlCarrito("cliente", "producto-1", 1);
    }
}
