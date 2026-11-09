package redSelection.dulceMayor.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redSelection.dulceMayor.dto.ChatbotPresupuestoRequestDTO;
import redSelection.dulceMayor.dto.ChatbotPresupuestoResponseDTO;
import redSelection.dulceMayor.service.ChatbotPresupuestoService;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotPresupuestoService presupuestos;

    public ChatbotController(ChatbotPresupuestoService presupuestos) {
        this.presupuestos = presupuestos;
    }

    /** Genera una propuesta sin crear pedido ni reservar stock; la confirmación se hace en el flujo de pedidos. */
    @PostMapping("/presupuestos")
    public ChatbotPresupuestoResponseDTO armarPresupuesto(@Valid @RequestBody ChatbotPresupuestoRequestDTO solicitud) {
        return presupuestos.armar(solicitud.mensaje());
    }
}
