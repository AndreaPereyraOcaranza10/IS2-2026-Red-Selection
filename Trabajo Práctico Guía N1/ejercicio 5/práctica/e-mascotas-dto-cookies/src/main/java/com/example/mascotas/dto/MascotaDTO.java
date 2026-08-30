package com.example.mascotas.dto;

import com.example.mascotas.enumeracion.Sexo;
import com.example.mascotas.enumeracion.Tipo;

public class MascotaDTO {

    private String id;
    private String nombre;
    private Sexo sexo;
    private Tipo tipo;
    private String idFoto;
    private String idUsuario;
    private String nombreUsuario;

    public MascotaDTO() {
    }

    public MascotaDTO(String id, String nombre, Sexo sexo, Tipo tipo, String idFoto, String idUsuario, String nombreUsuario) {
        this.id = id;
        this.nombre = nombre;
        this.sexo = sexo;
        this.tipo = tipo;
        this.idFoto = idFoto;
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public String getIdFoto() { return idFoto; }
    public void setIdFoto(String idFoto) { this.idFoto = idFoto; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
}