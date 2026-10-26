package com.tienda.zero.controller;

import com.tienda.zero.service.FlujoCompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

@ControllerAdvice(assignableTypes = {HomeController.class, TiendaController.class})
@RequiredArgsConstructor
public class TiendaCartModelAdvice {

    private final FlujoCompraService flujoCompraService;

    @ModelAttribute
    public void agregarCarritoAlModelo(Authentication authentication, Model model) {
        List<?> items = List.of();
        int cantidadTotal = 0;

        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            Map<String, Object> carrito = flujoCompraService.verCarrito(authentication.getName());
            items = (List<?>) carrito.getOrDefault("items", List.of());
            cantidadTotal = items.stream()
                    .map(item -> (Map<?, ?>) item)
                    .mapToInt(item -> ((Number) item.get("quantity")).intValue())
                    .sum();
        }

        model.addAttribute("cartItems", items);
        model.addAttribute("cartItemCount", cantidadTotal);
    }
}
