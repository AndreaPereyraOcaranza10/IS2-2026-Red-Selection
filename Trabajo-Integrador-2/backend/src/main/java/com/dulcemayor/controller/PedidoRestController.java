package com.dulcemayor.controller;

import com.dulcemayor.dto.OrderCreateDTO;
import com.dulcemayor.dto.OrderResponseDTO;
import com.dulcemayor.service.PedidoMayoristaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class PedidoRestController extends ApiControllerTemplate {
    private final PedidoMayoristaService pedidos;

    @GetMapping
    public List<OrderResponseDTO> list(Authentication authentication) {
        boolean admin = isStaff(authentication);
        return ejecutarEndpoint(() -> admin ? pedidos.listar() : pedidos.listarDeCliente(authentication.getName()));
    }

    @PostMapping
    public OrderResponseDTO create(@Valid @RequestBody OrderCreateDTO request, Authentication authentication) {
        String clientName = isStaff(authentication) ? request.clientName().trim() : authentication.getName();
        return ejecutarEndpoint(() -> pedidos.crear(new OrderCreateDTO(clientName, request.paymentMethod(), request.items())));
    }

    @PatchMapping("/{id}/status")
    public OrderResponseDTO updateStatus(@PathVariable String id, @RequestBody StatusUpdate request) {
        return ejecutarEndpoint(() -> pedidos.cambiarEstado(id, request.status()));
    }

    public record StatusUpdate(String status) {}

    private boolean isStaff(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_JEFE_ADMINISTRATIVO") || a.getAuthority().equals("ROLE_ALMACENERO"));
    }
}
