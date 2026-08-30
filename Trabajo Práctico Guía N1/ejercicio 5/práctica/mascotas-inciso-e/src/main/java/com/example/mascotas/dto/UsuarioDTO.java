package com.example.mascotas.dto;

import java.io.Serializable;

// Serializable porque este objeto se va a guardar en la HttpSession
public class UsuarioDTO implements Serializable {

    private String id;
    private String nombre;
    private String apellido;
    private String mail;
    private String idZona;
    private String nombreZona;
    private String idFoto;

    public UsuarioDTO() {
    }

    public UsuarioDTO(String id, String nombre, String apellido, String mail, String idZona, String nombreZona, String idFoto) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.mail = mail;
        this.idZona = idZona;
        this.nombreZona = nombreZona;
        this.idFoto = idFoto;
    }

    // Getters y Setters
    public String getIdZona() { return idZona; }
    public void setIdZona(String idZona) { this.idZona = idZona; }

    public String getNombreZona() { return nombreZona; }
    public void setNombreZona(String nombreZona) { this.nombreZona = nombreZona; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getIdFoto() { return idFoto; }
    public void setIdFoto(String idFoto) { this.idFoto = idFoto; }
}