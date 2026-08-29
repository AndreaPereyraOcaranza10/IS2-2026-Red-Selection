package com.example.mascotas.servicios;

import com.example.mascotas.dto.UsuarioDTO;
import com.example.mascotas.entidades.Foto;
import com.example.mascotas.entidades.Usuario;
import com.example.mascotas.entidades.Zona;
import com.example.mascotas.errores.ErrorServicio;
import com.example.mascotas.repositorios.UsuarioRespositorio;
import com.example.mascotas.repositorios.ZonaRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.Optional;

@Service
public class UsuarioServicio {

    //Se inicializa la variable automaticamente
    @Autowired
    private UsuarioRespositorio usuarioRepositorio;

    @Autowired
    private FotoServicio fotoServicio;

    @Autowired
    private NotificacionServicio notificacionServicio;

    @Autowired
    private ZonaRepositorio zonaRepositorio;

    // cada vez que se ejecuta alguna de estas funciones se realiza una transaccion en la bd
    @Transactional
    public void registrar(MultipartFile archivo, String nombre, String apellido, String mail, String clave, String clave2, String idZona) throws ErrorServicio {


        Zona zona = zonaRepositorio.getReferenceById(idZona);

        validar(nombre, apellido, mail, clave, clave2, zona);

        Usuario usuario = new Usuario();
        usuario.setZona(zona);
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setMail(mail);
        usuario.setClave(clave);
        usuario.setAlta(new Date());

        Foto foto = fotoServicio.guardar(archivo);
        usuario.setFoto(foto);

        usuarioRepositorio.save(usuario);

        //notificacionServicio.enviar("Bienvenido al tinder para mascotas", "Tinder de mascotas", usuario.getMail());
    }

    // cada vez que se ejecuta alguna de estas funciones se realiza una transaccion en la bd
    @Transactional
    public void modificar(MultipartFile archivo, String id, String nombre, String apellido, String mail, String clave, String clave2, String idZona) throws ErrorServicio {

        Zona zona = zonaRepositorio.getReferenceById(idZona);

        validar(nombre, apellido, mail, clave, clave2, zona);

        Optional<Usuario> respuesta  = usuarioRepositorio.findById(id);

        if (respuesta.isPresent()) {
            Usuario usuario = respuesta.get();
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setMail(mail);
            usuario.setZona(zona);

            String idFoto = null;
            if(usuario.getFoto() != null) {
                idFoto = usuario.getFoto().getId();
            }

            Foto foto = fotoServicio.actualizar(idFoto, archivo);
            usuario.setFoto(foto);

            usuarioRepositorio.save(usuario);
        } else {
            throw new ErrorServicio("No se encontro el usuario solicitado");
        }
    }

    // cada vez que se ejecuta alguna de estas funciones se realiza una transaccion en la bd
    @Transactional
    public void deshabilitar(String id) throws ErrorServicio {
        Optional<Usuario> respuesta  = usuarioRepositorio.findById(id);

        if (respuesta.isPresent()) {
            Usuario usuario = respuesta.get();
            usuario.setBaja(new Date());

            usuarioRepositorio.save(usuario);
        } else {
            throw new ErrorServicio("No se encontro el usuario solicitado");
        }
    }

    // cada vez que se ejecuta alguna de estas funciones se realiza una transaccion en la bd
    @Transactional
    public void habilitar(String id) throws ErrorServicio {
        Optional<Usuario> respuesta  = usuarioRepositorio.findById(id);

        if (respuesta.isPresent()) {
            Usuario usuario = respuesta.get();
            usuario.setBaja(null);

            usuarioRepositorio.save(usuario);
        } else {
            throw new ErrorServicio("No se encontro el usuario solicitado");
        }
    }

    // Valida credenciales de login contra la base de datos y devuelve el usuario si son correctas
    public UsuarioDTO login(String mail, String clave) throws ErrorServicio {

        if (mail == null || mail.isEmpty()) {
            throw new ErrorServicio("Debe ingresar un mail");
        }

        if (clave == null || clave.isEmpty()) {
            throw new ErrorServicio("Debe ingresar una clave");
        }

        Usuario usuario = usuarioRepositorio.buscarPorMail(mail);

        if (usuario == null) {
            // No se especifica si el error es del mail o de la clave por seguridad
            throw new ErrorServicio("El mail o la clave ingresados son incorrectos");
        }

        if (usuario.getBaja() != null) {
            throw new ErrorServicio("El usuario se encuentra deshabilitado");
        }

        if (!usuario.getClave().equals(clave)) {
            throw new ErrorServicio("El mail o la clave ingresados son incorrectos");
        }

        return convertirDTO(usuario);
    }


    @Transactional()
    public UsuarioDTO buscarUsuario(String idUsuario) throws ErrorServicio {
        try {
            if (idUsuario == null || idUsuario.trim().isEmpty()) {
                throw new ErrorServicio("Debe indicar el usuario");
            }
            Optional<Usuario> optional = usuarioRepositorio.findById(idUsuario);
            Usuario usuario = null;
            if (optional.isPresent()) {
                usuario= optional.get();
                if (usuario.getBaja() != null){
                    throw new ErrorServicio("No se encuentra el usuario indicado");
                }
            }
            return convertirDTO(usuario);

        } catch (ErrorServicio e) {
            throw e;
        }
    }

    public void validar(String nombre, String apellido, String mail, String clave, String clave2, Zona zona) throws ErrorServicio {
        if (nombre == null || nombre.isEmpty()) {
            throw new ErrorServicio("El nombre del usuario no puede ser nulo");
        }

        if (apellido == null || apellido.isEmpty()) {
            throw new ErrorServicio("El apellido del usuario no puede ser nulo");
        }

        if (mail == null || mail.isEmpty()) {
            throw new ErrorServicio("El mail del usuario no puede ser nulo");
        }

        if (clave == null || clave.isEmpty() || clave.length() <= 6) {
            throw new ErrorServicio("La calve del usuario no puede ser nula y tiene que tener mas de 6 digitos");
        }

        if (!clave.equals(clave2)) {
            throw new ErrorServicio("Las claves deben ser iguales");
        }

        if (zona == null) {
            throw new ErrorServicio("No se encontro la zona solicitada");
        }
    }

    //DTO
    public UsuarioDTO convertirDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setMail(usuario.getMail());
        dto.setIdZona(usuario.getZona() != null ? usuario.getZona().getId() : null);
        dto.setNombreZona(usuario.getZona() != null ? usuario.getZona().getNombre() : null);
        dto.setIdFoto(usuario.getFoto() != null ? usuario.getFoto().getId() : null);
        return dto;
    }


}
