package com.example.mascotas.servicios;

import com.example.mascotas.entidades.Mascota;
import com.example.mascotas.entidades.Voto;
import com.example.mascotas.errores.ErrorServicio;
import com.example.mascotas.repositorios.MascotaRepositorio;
import com.example.mascotas.repositorios.VotoRespositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class VotoServicio {

    @Autowired
    private MascotaRepositorio mascotaRepositorio;

    @Autowired
    private VotoRespositorio votoRespositorio;

    @Autowired
    private NotificacionServicio notificacionServicio;

    public void votar(String idUsuario, String idMascota1, String idMascota2) throws ErrorServicio{
        Voto voto = new Voto();
        voto.setFecha(new Date());

        if (idMascota1.equals(idMascota2)) {
            throw new ErrorServicio("No puede autovotarse");
        }

        Optional<Mascota> respuesta = mascotaRepositorio.findById(idMascota1);
        if (respuesta.isPresent()) {
            Mascota mascota1 = respuesta.get();
            if (mascota1.getUsuario().getId().equals(idUsuario)) {
                voto.setMascota1(mascota1);
            } else {
                throw new ErrorServicio("No tiene permisos para realizar esta operacion");
            }
        } else {
            throw new ErrorServicio("No existe ninguna mascota con ese idenficador");
        }

        Optional<Mascota> respuesta2 = mascotaRepositorio.findById(idMascota2);
        if (respuesta.isPresent()) {
            Mascota mascota2 = respuesta.get();
            if (mascota2.getUsuario().getId().equals(idUsuario)) {
                voto.setMascota2(mascota2);

                notificacionServicio.enviar("Tu mascota ha sido votada", "Tinder de mascotas", mascota2.getUsuario().getMail());

            } else {
                throw new ErrorServicio("No tiene permisos para realizar esta operacion");
            }
        } else {
            throw new ErrorServicio("No existe ninguna mascota con ese idenficador");
        }

        votoRespositorio.save(voto);
    }

    public void responder(String idUsuario, String idVoto) throws ErrorServicio {
        Optional<Voto> respuesta = votoRespositorio.findById(idVoto);
        if(respuesta.isPresent()) {
            Voto voto = respuesta.get();
            voto.setRespuesta(new Date());

            if (voto.getMascota2().getUsuario().getId().equals(idUsuario)) {
                votoRespositorio.save(voto);
                notificacionServicio.enviar("Hiciste match", "Tinder de mascotas", voto.getMascota1().getUsuario().getMail());
            } else {
                throw new ErrorServicio("No tiene permisos para realizar la operacion");
            }
        }
    }
}
