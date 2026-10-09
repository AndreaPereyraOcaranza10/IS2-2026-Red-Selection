package com.example.mascotas.mapeadores;

import com.example.mascotas.dto.MascotaDTO;
import com.example.mascotas.entidades.Mascota;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MascotaMapper {
    @Mapping(target = "idFoto", source = "foto.id")
    @Mapping(target = "idUsuario", source = "usuario.id")
    @Mapping(target = "nombreUsuario", expression = "java(nombreUsuario(mascota))")
    MascotaDTO toDto(Mascota mascota);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "alta", ignore = true)
    @Mapping(target = "baja", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "foto", ignore = true)
    void updateEntity(MascotaDTO dto, @MappingTarget Mascota mascota);

    default String nombreUsuario(Mascota mascota) {
        if (mascota.getUsuario() == null) return null;
        return mascota.getUsuario().getNombre();
    }
}
