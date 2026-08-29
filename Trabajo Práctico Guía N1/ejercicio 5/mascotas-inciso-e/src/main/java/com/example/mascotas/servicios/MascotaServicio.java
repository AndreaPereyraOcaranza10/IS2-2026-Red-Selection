package com.example.mascotas.servicios;

import com.example.mascotas.dto.MascotaDTO;
import com.example.mascotas.dto.UsuarioDTO;
import com.example.mascotas.entidades.Foto;
import com.example.mascotas.entidades.Mascota;
import com.example.mascotas.entidades.Usuario;
import com.example.mascotas.enumeracion.Sexo;
import com.example.mascotas.enumeracion.Tipo;
import com.example.mascotas.errores.ErrorServicio;
import com.example.mascotas.repositorios.MascotaRepositorio;
import com.example.mascotas.repositorios.UsuarioRespositorio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
public class MascotaServicio {

    //Se inicializa la variable automaticamente
    @Autowired
    private UsuarioRespositorio usuarioRepositorio;
    @Autowired
    private MascotaRepositorio mascotaRepositorio;

    @Autowired
    private FotoServicio fotoServicio;

    // cada vez que se ejecuta alguna de estas funciones se realiza una transaccion en la bd
    @Transactional
    public void agregarMascota(MultipartFile archivo, String idUsuario, String nombre, Sexo sexo, Tipo tipo) throws ErrorServicio {
        Usuario usuario = usuarioRepositorio.findById(idUsuario).get();

        validar(nombre, sexo);

        Mascota mascota = new Mascota();
        mascota.setNombre(nombre);
        mascota.setSexo(sexo);
        mascota.setAlta(new Date());
        mascota.setTipo(tipo);
        mascota.setUsuario(usuario);

        Foto foto = fotoServicio.guardar(archivo);
        mascota.setFoto(foto);


        mascotaRepositorio.save(mascota);

    }

    // cada vez que se ejecuta alguna de estas funciones se realiza una transaccion en la bd
    @Transactional
    public void modificar(MultipartFile archivo, String idUsuario, String idMascota, String nombre, Sexo sexo, Tipo tipo) throws ErrorServicio {
        validar(nombre, sexo);

        Optional<Mascota> respuesta = mascotaRepositorio.findById(idMascota);
        if (respuesta.isPresent()) {
            Mascota mascota = respuesta.get();
            if (mascota.getUsuario().getId().equals(idUsuario)) {
                mascota.setNombre(nombre);
                mascota.setSexo(sexo);
                mascota.setTipo(tipo);

                String idFoto = null;
                if(mascota.getFoto() != null) {
                    idFoto = mascota.getFoto().getId();
                }

                Foto foto = fotoServicio.actualizar(idFoto, archivo);
                mascota.setFoto(foto);

                mascotaRepositorio.save(mascota);

            } else {
                throw new ErrorServicio("No tiene permisos suficientes para realizar esta accion");
            }
        } else {
            throw new ErrorServicio("No existe una mascota con el identificador solicitado");
        }
    }

    // cada vez que se ejecuta alguna de estas funciones se realiza una transaccion en la bd
    @Transactional
    public void eliminar(String idUsuario, String idMascota) throws ErrorServicio {
        Optional<Mascota> respuesta = mascotaRepositorio.findById(idMascota);
        if (respuesta.isPresent()) {
            Mascota mascota = respuesta.get();
            if (mascota.getUsuario().getId().equals(idUsuario)) {
                mascota.setBaja(new Date());
                mascotaRepositorio.save(mascota);

            } else {
                throw new ErrorServicio("No tiene permisos suficientes para realizar esta accion");
            }
        } else {
            throw new ErrorServicio("No existe una mascota con el identificador solicitado");
        }
    }

    @Transactional()
    public MascotaDTO buscarMascota(String idMascota) throws ErrorServicio {
        if (idMascota == null || idMascota.trim().isEmpty()) {
            throw new ErrorServicio("Debe indicar el id de mascota");
        }
        Optional<Mascota> optional = mascotaRepositorio.findById(idMascota);
        Mascota mascota = null;
        if (optional.isPresent()) {
            mascota= optional.get();
            if (mascota.getBaja() != null){
                throw new ErrorServicio("No se encuentra la mascota indicada");
            }
            return convertirDTO(mascota);
        }

        return null;
    }

    public List<MascotaDTO> buscarMascotasPorUsuario(String id) {
        List<Mascota> mascotas = mascotaRepositorio.buscarMascotasPorUsuario(id);
        return convertirDTO(mascotas);
    }

    public void validar(String nombre, Sexo sexo) throws ErrorServicio {
        if (nombre == null || nombre.isEmpty()) {
            throw new ErrorServicio("El nombre de la mascota no puede ser nulo o vacio");
        }
        if (sexo == null) {
            throw new ErrorServicio("El sexo de la mascota no puede ser nulo");
        }
    }


    //DTO
    public MascotaDTO convertirDTO(Mascota mascota) {
        MascotaDTO dto = new MascotaDTO();
        dto.setId(mascota.getId());
        dto.setNombre(mascota.getNombre());
        dto.setSexo(mascota.getSexo());
        dto.setTipo(mascota.getTipo());
        dto.setIdFoto(mascota.getFoto().getId());
        dto.setIdUsuario(mascota.getUsuario().toString());
        dto.setIdFoto(mascota.getFoto().getId());

        return dto;
    }

    public List<MascotaDTO> convertirDTO(List<Mascota> mascotas) {
        List<MascotaDTO> resultado = new ArrayList<>();
        for (Mascota m : mascotas) {
            resultado.add(convertirDTO(m));
        }
        return resultado;
    }



}
