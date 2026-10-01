package com.tienda.zero.controller;

import com.tienda.zero.dto.PerfilClienteDTO;
import com.tienda.zero.enums.TipoUsuario;
import com.tienda.zero.enums.TipoEmpleado;
import com.tienda.zero.model.Cliente;
import com.tienda.zero.model.ContactoTelefonico;
import com.tienda.zero.model.Direccion;
import com.tienda.zero.model.Empleado;
import com.tienda.zero.model.Persona;
import com.tienda.zero.model.Usuario;
import com.tienda.zero.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Date;
import java.util.Optional;

@Controller
public class RegistroController {

    private final RegistroService registroService;
    private final UsuarioService usuarioService;
    private final PersonaService personaService;
    private final NacionalidadService nacionalidadService;
    private final EmpleadoService empleadoService;
    private final FlujoCompraService flujoCompraService;
    private final UbicacionService ubicacionService;

    public RegistroController(RegistroService registroService, UsuarioService usuarioService,
                              PersonaService personaService, NacionalidadService nacionalidadService,
                              EmpleadoService empleadoService, FlujoCompraService flujoCompraService,
                              UbicacionService ubicacionService) {
        this.registroService = registroService;
        this.usuarioService = usuarioService;
        this.personaService = personaService;
        this.nacionalidadService = nacionalidadService;
        this.empleadoService = empleadoService;
        this.flujoCompraService = flujoCompraService;
        this.ubicacionService = ubicacionService;
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarCliente(@RequestParam String correo, @RequestParam String clave,
                                   @RequestParam String repetirClave, Model model) {
        try {
            if (!clave.equals(repetirClave)) {
                throw new IllegalArgumentException("Las contraseñas no coinciden");
            }
            registroService.registrarCliente(correo, clave);
            model.addAttribute("mensaje", "¡Listo! Te enviamos un mail a " + correo + " con el código de activación.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "registro";
    }

    @GetMapping("/activar-cuenta")
    public String mostrarFormularioActivacion() {
        return "activar-cuenta";
    }

    @PostMapping("/activar-cuenta")
    public String activarCuenta(@RequestParam String correo, @RequestParam String codigo, Model model) {
        try {
            registroService.activarCuentaCliente(correo, codigo);
            model.addAttribute("activada", true);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("correo", correo);
        }
        return "activar-cuenta";
    }

    @GetMapping("/completar-perfil")
    public String mostrarFormularioPerfil(Authentication authentication, Model model) {
        Usuario usuario = usuarioService.buscarUsuarioPorNombreUsuario(authentication.getName());

        if (usuario.getRol() != TipoUsuario.CLIENTE) {
            return "redirect:/";
        }
        Optional<Persona> persona = personaService.buscarPersonaPorUsuario(usuario.getId());
        cargarFormularioPerfil(model, persona, crearPerfilDto(usuario, persona));
        return "completar-perfil";
    }

    @PostMapping("/completar-perfil")
    public String completarPerfil(Authentication authentication,
                                  @ModelAttribute("perfil") PerfilClienteDTO perfil,
                                  Model model) {

        Usuario usuario = usuarioService.buscarUsuarioPorNombreUsuario(authentication.getName());

        try {
            registroService.completarPerfilCliente(usuario.getId(), perfil.getNombre(), perfil.getApellido(),
                    perfil.getSexo(), Date.valueOf(perfil.getFechaNacimiento()), perfil.getTipoDocumento(),
                    perfil.getNumeroDocumento(), perfil.getDireccionEstadia(), perfil.getIdNacionalidad(),
                    perfil.getCalle(), perfil.getNumeracion(), perfil.getBarrio(), perfil.getManzanaPiso(),
                    perfil.getCasaDepartamento(), perfil.getReferencia(), perfil.getIdLocalidad(),
                    perfil.getTelefono());
            return "redirect:/perfil-completo";
        } catch (IllegalArgumentException | ClassCastException e) {
            model.addAttribute("error", e.getMessage());
            perfil.setCorreo(usuario.getNombreUsuario());
            cargarFormularioPerfil(model, personaService.buscarPersonaPorUsuario(usuario.getId()), perfil);
            return "completar-perfil";
        }
    }

    @GetMapping("/admin/perfil")
    public String mostrarPerfilEmpleado(Authentication authentication, Model model) {
        Usuario usuario = usuarioService.buscarUsuarioPorNombreUsuario(authentication.getName());
        Optional<Persona> persona = personaService.buscarPersonaPorUsuario(usuario.getId());

        if (persona.isPresent() && !(persona.get() instanceof Empleado)) {
            return "redirect:/admin";
        }

        Empleado empleado = persona.map(Empleado.class::cast).orElse(null);
        PerfilClienteDTO perfil = empleado == null
                ? PerfilClienteDTO.builder().correo(usuario.getNombreUsuario()).build()
                : crearPerfilEmpleadoDto(usuario, empleado);
        cargarFormularioPerfilEmpleado(model, perfil, empleado != null);
        return "completar-perfil";
    }

    @PostMapping("/admin/perfil")
    public String actualizarPerfilEmpleado(Authentication authentication,
                                           @ModelAttribute("perfil") PerfilClienteDTO perfil,
                                           Model model) {
        Usuario usuario = usuarioService.buscarUsuarioPorNombreUsuario(authentication.getName());
        Optional<Persona> persona = personaService.buscarPersonaPorUsuario(usuario.getId());

        if (persona.isPresent() && !(persona.get() instanceof Empleado)) {
            return "redirect:/admin";
        }

        try {
            if (persona.isPresent()) {
                Empleado empleado = (Empleado) persona.get();
                empleadoService.modificarDatosPersonales(empleado.getId(), perfil.getNombre(),
                        perfil.getApellido(), perfil.getSexo(), Date.valueOf(perfil.getFechaNacimiento()),
                        perfil.getTipoDocumento(), perfil.getNumeroDocumento());
            } else {
                empleadoService.crearPerfilEmpleado(usuario.getId(), perfil.getNombre(), perfil.getApellido(),
                        perfil.getSexo(), Date.valueOf(perfil.getFechaNacimiento()), perfil.getTipoDocumento(),
                        perfil.getNumeroDocumento(), TipoEmpleado.valueOf(usuario.getRol().name()));
            }
            return "redirect:/admin/perfil";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            perfil.setCorreo(usuario.getNombreUsuario());
            cargarFormularioPerfilEmpleado(model, perfil, persona.isPresent());
            return "completar-perfil";
        }
    }

    @GetMapping("/perfil-completo")
    public String perfilCompleto() {
        return "perfil-completo";
    }

    private void cargarFormularioPerfil(Model model, Optional<Persona> persona, PerfilClienteDTO perfil) {
        model.addAttribute("perfil", perfil);
        model.addAttribute("modoEdicion", persona.isPresent());
        model.addAttribute("nacionalidades", nacionalidadService.listarNacionalidadActiva());
        model.addAttribute("ordenesCliente", flujoCompraService.listarPedidosCliente(perfil.getCorreo()));
        model.addAttribute("ubicacion", ubicacionService.obtenerUbicaciones());
    }

    private void cargarFormularioPerfilEmpleado(Model model, PerfilClienteDTO perfil, boolean perfilExistente) {
        model.addAttribute("perfil", perfil);
        model.addAttribute("modoEdicion", perfilExistente);
        model.addAttribute("perfilEmpleado", true);
    }

    private PerfilClienteDTO crearPerfilDto(Usuario usuario, Optional<Persona> personaOptional) {
        PerfilClienteDTO.PerfilClienteDTOBuilder builder = PerfilClienteDTO.builder()
                .correo(usuario.getNombreUsuario());

        if (personaOptional.isEmpty()) {
            return builder.build();
        }

        if (!(personaOptional.get() instanceof Cliente cliente)) {
            throw new IllegalArgumentException("El perfil asociado no corresponde a un cliente");
        }

        builder.nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .sexo(cliente.getSexo())
                .fechaNacimiento(cliente.getFechaNacimiento().toString())
                .tipoDocumento(cliente.getTipoDocumento())
                .numeroDocumento(cliente.getNumeroDocumento())
                .direccionEstadia(cliente.getDireccionEstadia())
                .idNacionalidad(cliente.getNacionalidad().getId());

        cliente.getDirecciones().stream()
                .filter(direccion -> !direccion.isEliminado())
                .findFirst()
                .ifPresent(direccion -> completarDireccion(builder, direccion));

        cliente.getContactos().stream()
                .filter(contacto -> !contacto.isEliminado())
                .filter(ContactoTelefonico.class::isInstance)
                .map(ContactoTelefonico.class::cast)
                .findFirst()
                .ifPresent(contacto -> builder.telefono(contacto.getTelefono()));

        return builder.build();
    }

    private void completarDireccion(PerfilClienteDTO.PerfilClienteDTOBuilder builder, Direccion direccion) {
        builder.calle(direccion.getCalle())
                .numeracion(direccion.getNumeracion())
                .barrio(direccion.getBarrio())
                .manzanaPiso(direccion.getManzanaPiso())
                .casaDepartamento(direccion.getCasaDepartamento())
                .referencia(direccion.getReferencia())
                .idLocalidad(direccion.getLocalidad().getId())
                .idDepartamento(direccion.getLocalidad().getDepartamento().getId())
                .idProvincia(direccion.getLocalidad().getDepartamento().getProvincia().getId());
    }

    private PerfilClienteDTO crearPerfilEmpleadoDto(Usuario usuario, Empleado empleado) {
        return PerfilClienteDTO.builder()
                .correo(usuario.getNombreUsuario())
                .nombre(empleado.getNombre())
                .apellido(empleado.getApellido())
                .sexo(empleado.getSexo())
                .fechaNacimiento(empleado.getFechaNacimiento().toString())
                .tipoDocumento(empleado.getTipoDocumento())
                .numeroDocumento(empleado.getNumeroDocumento())
                .build();
    }
}
