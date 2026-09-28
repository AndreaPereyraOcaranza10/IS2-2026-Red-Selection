package com.tienda.zero.service.impl;

import com.tienda.zero.dto.RegistroDTO;
import com.tienda.zero.enums.Rol;
import com.tienda.zero.model.Usuario;
import com.tienda.zero.repository.UsuarioRepository;
import com.tienda.zero.service.EmailService;
import com.tienda.zero.service.UsuarioService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Implementación de UsuarioService encargada de la lógica de negocio para:
 * - Registro con validación de datos y contraseñas.
 * - Hashing de contraseñas con BCrypt.
 * - Generación de códigos numéricos de activación.
 * - Activación de cuentas y reenvío de códigos.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              EmailService emailService,
                              @Lazy PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Usuario registrarCliente(RegistroDTO dto, String baseUrl) {
        // Validación de correo
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        }

        String emailNormalizado = dto.getEmail().trim().toLowerCase();

        // Verificar si ya existe una cuenta con este email
        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            Usuario existente = usuarioRepository.findByEmailIgnoreCase(emailNormalizado).orElse(null);
            if (existente != null && !existente.isActivo()) {
                // Si la cuenta existía pero no fue activada, renovamos el código y reenviamos
                reenviarCodigoActivacion(emailNormalizado, baseUrl);
                return existente;
            }
            throw new IllegalArgumentException("Ya existe una cuenta registrada con el correo: " + emailNormalizado);
        }

        // Validación de contraseña
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }

        if (dto.getConfirmPassword() == null || !dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }

        // Generar código numérico de 6 dígitos (ej: 489201)
        String codigo = String.format("%06d", new Random().nextInt(1_000_000));

        // Construir el usuario con rol CLIENTE, inactivo hasta que ingrese el código
        Usuario usuario = Usuario.builder()
                .email(emailNormalizado)
                .password(passwordEncoder.encode(dto.getPassword()))
                .rol(Rol.CLIENTE)
                .activo(false)
                .codigoActivacion(codigo)
                .fechaExpiracionCodigo(LocalDateTime.now().plusHours(24))
                .eliminado(false)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);

        // Armar enlace y enviar correo
        String urlActivacion = (baseUrl != null ? baseUrl : "http://localhost:8080") + "/activar?email=" + emailNormalizado;
        emailService.enviarCodigoActivacion(emailNormalizado, codigo, urlActivacion);

        return guardado;
    }

    @Override
    @Transactional
    public boolean activarCuenta(String email, String codigo) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es requerido para activar la cuenta.");
        }
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Debes ingresar el código de activación.");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ningún usuario con el correo: " + email));

        if (usuario.isActivo()) {
            return true; // Ya se encontraba activa
        }

        if (usuario.getCodigoActivacion() == null || !usuario.getCodigoActivacion().trim().equalsIgnoreCase(codigo.trim())) {
            throw new IllegalArgumentException("El código de activación ingresado es incorrecto.");
        }

        if (usuario.getFechaExpiracionCodigo() != null && usuario.getFechaExpiracionCodigo().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El código de activación ha expirado. Por favor solicita uno nuevo.");
        }

        // Activar usuario y limpiar el código de seguridad
        usuario.setActivo(true);
        usuario.setCodigoActivacion(null);
        usuario.setFechaExpiracionCodigo(null);
        usuarioRepository.save(usuario);

        return true;
    }

    @Override
    @Transactional
    public void reenviarCodigoActivacion(String email, String baseUrl) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio.");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ninguna cuenta con el correo: " + email));

        if (usuario.isActivo()) {
            throw new IllegalArgumentException("La cuenta ya se encuentra activa. Puedes iniciar sesión directamente.");
        }

        String nuevoCodigo = String.format("%06d", new Random().nextInt(1_000_000));
        usuario.setCodigoActivacion(nuevoCodigo);
        usuario.setFechaExpiracionCodigo(LocalDateTime.now().plusHours(24));
        usuarioRepository.save(usuario);

        String urlActivacion = (baseUrl != null ? baseUrl : "http://localhost:8080") + "/activar?email=" + usuario.getEmail();
        emailService.enviarCodigoActivacion(usuario.getEmail(), nuevoCodigo, urlActivacion);
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        if (email == null) return null;
        return usuarioRepository.findByEmailIgnoreCase(email.trim().toLowerCase()).orElse(null);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}
