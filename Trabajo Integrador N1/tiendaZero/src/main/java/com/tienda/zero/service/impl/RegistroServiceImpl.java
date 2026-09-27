package com.tienda.zero.service.impl;

import com.tienda.zero.enums.*;
import com.tienda.zero.model.*;
import com.tienda.zero.service.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Date;

@Service
public class RegistroServiceImpl implements RegistroService {

    private static final String ASUNTO_POR_DEFECTO = "Activá tu cuenta en TiendaZero";
    private static final String CUERPO_POR_DEFECTO = """
            <html>
              <body style="font-family: Arial, sans-serif;">
                <h2>¡Bienvenido/a a TiendaZero!</h2>
                <p>Para activar tu cuenta ingresá el siguiente código en la página de activación:</p>
                <h1 style="letter-spacing: 4px;">{{CODIGO}}</h1>
                <p>Página de activación: <a href="{{LINK}}">{{LINK}}</a></p>
                <p>Este código vence en 30 minutos.</p>
              </body>
            </html>
            """;

    private final UsuarioService usuarioService;
    private final ClienteService clienteService;
    private final PersonaService personaService;
    private final DireccionService direccionService;
    private final ContactoTelefonicoService contactoTelefonicoService;
    private final CorreoService correoService;
    private final ConfiguracionCorreoEmpresaService configuracionCorreoEmpresaService;
    private final String linkActivacion;

    public RegistroServiceImpl(UsuarioService usuarioService, ClienteService clienteService,
                               PersonaService personaService, DireccionService direccionService,
                               ContactoTelefonicoService contactoTelefonicoService, CorreoService correoService,
                               ConfiguracionCorreoEmpresaService configuracionCorreoEmpresaService,
                               @Value("${app.activacion.url}") String linkActivacion) {
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
        this.personaService = personaService;
        this.direccionService = direccionService;
        this.contactoTelefonicoService = contactoTelefonicoService;
        this.correoService = correoService;
        this.configuracionCorreoEmpresaService = configuracionCorreoEmpresaService;
        this.linkActivacion = linkActivacion;
    }

    @Override
    @Transactional
    public Usuario registrarCliente(String correo, String clave) {
        Usuario usuario = usuarioService.crearUsuarioPendienteActivacion(correo, clave, TipoUsuario.CLIENTE);
        usuario = usuarioService.generarCodigoActivacion(usuario.getId());

        enviarMailActivacion(usuario);

        return usuario;
    }

    @Override
    public void activarCuentaCliente(String correo, String codigo) {
        usuarioService.activarCuenta(correo, codigo);
    }

    @Override
    @Transactional
    public void reenviarCodigoActivacion(String correo) {
        Usuario usuario = usuarioService.buscarUsuarioPorNombreUsuario(correo);

        if (usuario.isCuentaActivada()) {
            throw new IllegalArgumentException("La cuenta ya está activada");
        }

        usuario = usuarioService.generarCodigoActivacion(usuario.getId());
        enviarMailActivacion(usuario);
    }

    private void enviarMailActivacion(Usuario usuario) {
        String asunto = ASUNTO_POR_DEFECTO;
        String cuerpo = CUERPO_POR_DEFECTO;

        var configuracion = configuracionCorreoEmpresaService.buscarActivaPorTipo(TipoCorreo.ACTIVACION_CUENTA);
        if (configuracion.isPresent()) {
            asunto = configuracion.get().getAsunto();
            cuerpo = configuracion.get().getCuerpoHtml();
        }

        cuerpo = cuerpo.replace("{{CODIGO}}", usuario.getCodigoActivacion())
                .replace("{{LINK}}", linkActivacion);

        correoService.enviarCorreoHtml(usuario.getNombreUsuario(), asunto, cuerpo);
    }

    @Override
    @Transactional
    public Cliente completarPerfilCliente(String idUsuario,
                                          String nombre, String apellido, Sexo sexo, Date fechaNacimiento,
                                          TipoDocumento tipoDocumento, String numeroDocumento,
                                          String direccionEstadia, String idNacionalidad,
                                          String calle, String numeracion, String barrio, String manzanaPiso,
                                          String casaDepartamento, String referencia, String idLocalidad,
                                          String telefono) {

        Usuario usuario = usuarioService.buscarUsuario(idUsuario);

        if (!usuario.isCuentaActivada()) {
            throw new IllegalArgumentException("Hay que activar la cuenta antes de completar el perfil");
        }
        if (usuario.getRol() != TipoUsuario.CLIENTE) {
            throw new IllegalArgumentException("Este usuario no es un cliente");
        }

        Cliente cliente = clienteService.crearCliente(nombre, apellido, sexo, fechaNacimiento, tipoDocumento,
                numeroDocumento, direccionEstadia, idNacionalidad);

        Direccion direccion = direccionService.crearDireccion(calle, numeracion, barrio, manzanaPiso,
                casaDepartamento, referencia, idLocalidad);
        personaService.asociarDireccionPersona(cliente.getId(), direccion.getId());

        if (telefono != null && !telefono.isBlank()) {
            ContactoTelefonico contacto = contactoTelefonicoService.crearContactoTelefonico(
                    telefono, TipoTelefono.CELULAR, TipoContacto.PERSONAL, null);
            personaService.asociarContactoPersona(cliente.getId(), contacto.getId());
        }

        personaService.asociarUsuarioPersona(cliente.getId(), usuario.getId());

        return (Cliente) personaService.buscarPersona(cliente.getId());
    }
}