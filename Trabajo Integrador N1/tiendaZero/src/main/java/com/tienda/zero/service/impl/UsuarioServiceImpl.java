package com.tienda.zero.service.impl;

import com.tienda.zero.enums.TipoUsuario;
import com.tienda.zero.model.Usuario;
import com.tienda.zero.repository.UsuarioRepository;
import com.tienda.zero.service.UsuarioService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final int LONGITUD_MINIMA_CLAVE = 4;

    private static final int MINUTOS_VIGENCIA_CODIGO = 30;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom random = new SecureRandom();

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder){
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Usuario crearUsuario(String nombreUsuario, String clave, TipoUsuario rol) {

        return construirYGuardarUsuario(nombreUsuario, clave, rol, true);

    }

    @Override
    @Transactional
    public Usuario crearUsuarioPendienteActivacion(String nombreUsuario, String clave, TipoUsuario rol) {
        return construirYGuardarUsuario(nombreUsuario, clave, rol, false);
    }

    private Usuario construirYGuardarUsuario(String nombreUsuario, String clave, TipoUsuario rol, boolean cuentaActivada) {
        validar(nombreUsuario, clave, rol);

        String nombreLimpio = nombreUsuario.trim().toLowerCase();

        if (usuarioRepository.findByNombreUsuario(nombreLimpio).isPresent()){
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre");
        }

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombreLimpio);
        usuario.setClave(passwordEncoder.encode(clave));
        usuario.setRol(rol);
        usuario.setEliminado(false);
        usuario.setCuentaActivada(cuentaActivada);

        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario generarCodigoActivacion(String id) {
        Usuario usuario = buscarUsuario(id);

        String codigo = String.format("%06d", random.nextInt(1_000_000));
        usuario.setCodigoActivacion(codigo);
        usuario.setFechaExpiracionCodigo(LocalDateTime.now().plusMinutes(MINUTOS_VIGENCIA_CODIGO));

        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario activarCuenta(String nombreUsuario, String codigo) {
        Usuario usuario = buscarUsuarioPorNombreUsuario(nombreUsuario);

        if (usuario.isCuentaActivada()) {
            throw new IllegalArgumentException("La cuenta ya está activada");
        }
        if (codigo == null || usuario.getCodigoActivacion() == null
                || !usuario.getCodigoActivacion().equals(codigo.trim())) {
            throw new IllegalArgumentException("El código de activación es incorrecto");
        }
        if (usuario.getFechaExpiracionCodigo() == null
                || usuario.getFechaExpiracionCodigo().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El código de activación expiró, solicitá uno nuevo");
        }

        usuario.setCuentaActivada(true);
        usuario.setCodigoActivacion(null);
        usuario.setFechaExpiracionCodigo(null);

        return usuarioRepository.save(usuario);
    }




    @Override
    public void validar(String nombreUsuario, String clave, TipoUsuario rol) {

        validarNombreYRol(nombreUsuario,rol);
        validarClave(clave);

    }

    @Override
    public Usuario buscarUsuario(String id) {

        Optional<Usuario> resultado = usuarioRepository.findById(id);
        if (resultado.isEmpty()){
            throw new IllegalArgumentException("No existe el usuario con id: " + id);

        }

        return resultado.get();

    }

    @Override
    public Usuario buscarUsuarioPorNombreUsuario(String nombreUsuario) {

        String nombreNormalizado = nombreUsuario.trim().toLowerCase();

        Optional<Usuario> resultado = usuarioRepository.findByNombreUsuario(nombreNormalizado);
        if (resultado.isEmpty()){
            throw new IllegalArgumentException("No existe el usuario: " + nombreUsuario);

        }

        return resultado.get();

    }

    @Override
    public Usuario modificarUsuario(String id, String nombreUsuario, String clave, TipoUsuario rol) {

        validarNombreYRol(nombreUsuario, rol);

        boolean cambiarClave = clave != null && !clave.isBlank();
        if (cambiarClave){
            validarClave(clave);
        }

        Usuario usuario = buscarUsuario(id);
        String nombreLimpio = nombreUsuario.trim().toLowerCase();

        Optional <Usuario> existente = usuarioRepository.findByNombreUsuario(nombreLimpio);
        if (existente.isPresent() && !existente.get().getId().equals(id)){
            throw new IllegalArgumentException("Ya existe otro usuario con ese nombre");
        }

        usuario.setNombreUsuario(nombreLimpio);
        usuario.setRol(rol);
        if(cambiarClave){
            usuario.setClave(passwordEncoder.encode(clave));
        }

        return usuarioRepository.save(usuario);

    }

    @Override
    @Transactional
    public void eliminarUsuario(String id) {
        Usuario usuario = buscarUsuario(id);
        usuario.setEliminado(true);
        usuarioRepository.save(usuario);
    }

    @Override
    public List<Usuario> listarUsuario() {
        return usuarioRepository.findAll();
    }

    @Override
    public List<Usuario> listarUsuarioActivo() {
        return usuarioRepository.findByEliminadoFalse();
    }

    private void validarNombreYRol(String nombreUsuario, TipoUsuario rol){
        if (nombreUsuario == null || nombreUsuario.isBlank()){
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        if (rol == null){
            throw new IllegalArgumentException("El rol es obligatorio");
        }
    }

    private void validarClave(String clave){
        if (clave == null || clave.isBlank()){
            throw new IllegalArgumentException("La clave es obligatoria");
        }
        if (clave.length() < LONGITUD_MINIMA_CLAVE) {
            throw new IllegalArgumentException("La clave debe tener al menos " + LONGITUD_MINIMA_CLAVE + " caracteres");
        }
    }

}
