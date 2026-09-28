package com.tienda.zero.controller;

import com.tienda.zero.dto.ActivarCuentaDTO;
import com.tienda.zero.dto.RegistroDTO;
import com.tienda.zero.service.EmailService;
import com.tienda.zero.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UsuarioService usuarioService;
    private final EmailService emailService;

    /**
     * Muestra la vista combinada de Login y Registro de la tienda.
     */
    @GetMapping({"/login", "/register"})
    public String showLoginAndRegisterPage(@RequestParam(value = "error", required = false) String error,
                                           @RequestParam(value = "logout", required = false) String logout,
                                           @RequestParam(value = "activated", required = false) String activated,
                                           Model model) {
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistroDTO());
        }

        if (error != null) {
            model.addAttribute("loginError", "Correo o contraseña incorrectos. Verifica tus credenciales.");
        }
        if (logout != null) {
            model.addAttribute("logoutSuccess", "Has cerrado sesión correctamente.");
        }
        if (activated != null) {
            model.addAttribute("activationSuccess", "¡Tu cuenta ha sido activada con éxito! Ya puedes iniciar sesión.");
        }

        return "tienda/auth/register";
    }

    /**
     * Procesa el formulario de registro de clientes.
     * Crea el usuario inactivo, genera el código y envía el correo con el código y el link de activación.
     */
    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("registrationForm") RegistroDTO form,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        try {
            String baseUrl = getBaseUrl(request);
            usuarioService.registrarCliente(form, baseUrl);

            redirectAttributes.addFlashAttribute("mensajeExito",
                    "¡Registro iniciado con éxito! Te enviamos un código a tu correo para activar tu cuenta.");

            return "redirect:/activar?email=" + URLEncoder.encode(form.getEmail().trim(), StandardCharsets.UTF_8) + "&enviado=true";

        } catch (IllegalArgumentException e) {
            model.addAttribute("registroError", e.getMessage());
            model.addAttribute("registrationForm", form);
            return "tienda/auth/register";
        } catch (Exception e) {
            log.error("Error inesperado en el registro: ", e);
            model.addAttribute("registroError", "Ocurrió un error al procesar el registro. Intenta nuevamente.");
            model.addAttribute("registrationForm", form);
            return "tienda/auth/register";
        }
    }

    /**
     * Muestra la página de activación donde el cliente debe ingresar el código recibido por correo.
     */
    @GetMapping("/activar")
    public String showActivationPage(@RequestParam(value = "email", required = false) String email,
                                     @RequestParam(value = "codigo", required = false) String codigo,
                                     @RequestParam(value = "enviado", required = false) String enviado,
                                     @RequestParam(value = "reenviado", required = false) String reenviado,
                                     @RequestParam(value = "noActivo", required = false) String noActivo,
                                     Model model) {
        ActivarCuentaDTO dto = ActivarCuentaDTO.builder()
                .email(email != null ? email.trim() : "")
                .codigo(codigo != null ? codigo.trim() : "")
                .build();

        model.addAttribute("activarForm", dto);

        if (noActivo != null) {
            model.addAttribute("mensajeAlerta", "Tu cuenta aún no está activa. Debes ingresar el código de activación para poder ingresar.");
        }
        if (enviado != null) {
            model.addAttribute("mensajeInfo", "Hemos enviado un código de 6 dígitos a tu casilla de correo.");
        }
        if (reenviado != null) {
            model.addAttribute("mensajeExito", "Se ha generado y enviado un nuevo código a tu correo electrónico.");
        }

        // Si estamos en entorno de desarrollo, proveemos el último código simulado para agilizar pruebas
        if (email != null && !email.isBlank()) {
            String ultimoCodigo = emailService.getUltimoCodigoSimulado(email);
            if (ultimoCodigo != null) {
                model.addAttribute("codigoSugeridoDev", ultimoCodigo);
            }
        }

        return "tienda/auth/activar";
    }

    /**
     * Procesa la validación del código de activación ingresado por el usuario.
     */
    @PostMapping("/activar")
    public String processActivation(@ModelAttribute("activarForm") ActivarCuentaDTO form,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        try {
            usuarioService.activarCuenta(form.getEmail(), form.getCodigo());

            redirectAttributes.addFlashAttribute("activationSuccess",
                    "¡Excelente! Tu cuenta fue activada exitosamente. Ahora puedes ingresar con tu correo y contraseña.");

            return "redirect:/login?activated=true";

        } catch (IllegalArgumentException e) {
            model.addAttribute("mensajeError", e.getMessage());
            model.addAttribute("activarForm", form);

            // Mantenemos el código sugerido en caso de error
            String ultimoCodigo = emailService.getUltimoCodigoSimulado(form.getEmail());
            if (ultimoCodigo != null) {
                model.addAttribute("codigoSugeridoDev", ultimoCodigo);
            }
            return "tienda/auth/activar";
        }
    }

    /**
     * Permite solicitar el reenvío del código de activación si no llegó o expiró.
     */
    @PostMapping("/reenviar-codigo")
    public String resendActivationCode(@RequestParam("email") String email,
                                       HttpServletRequest request,
                                       RedirectAttributes redirectAttributes) {
        try {
            String baseUrl = getBaseUrl(request);
            usuarioService.reenviarCodigoActivacion(email, baseUrl);
            return "redirect:/activar?email=" + URLEncoder.encode(email.trim(), StandardCharsets.UTF_8) + "&reenviado=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/activar?email=" + (email != null ? URLEncoder.encode(email.trim(), StandardCharsets.UTF_8) : "");
        }
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
            return scheme + "://" + serverName;
        }
        return scheme + "://" + serverName + ":" + serverPort;
    }
}
