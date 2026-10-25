package com.example.mascotas.dto;

public class ReporteVotosDTO {

    private String nombreUsuario;
    private String apellidoUsuario;
    private String nombreMascota;
    private Long cantidadVotos;

    public ReporteVotosDTO() {
    }

    public ReporteVotosDTO(String nombreUsuario, String apellidoUsuario, String nombreMascota, Long cantidadVotos) {
        this.nombreUsuario = nombreUsuario;
        this.apellidoUsuario = apellidoUsuario;
        this.nombreMascota = nombreMascota;
        this.cantidadVotos = cantidadVotos;
    }

    // Getters y Setters
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getApellidoUsuario() { return apellidoUsuario; }
    public void setApellidoUsuario(String apellidoUsuario) { this.apellidoUsuario = apellidoUsuario; }

    public String getNombreMascota() { return nombreMascota; }
    public void setNombreMascota(String nombreMascota) { this.nombreMascota = nombreMascota; }

    public Long getCantidadVotos() { return cantidadVotos; }
    public void setCantidadVotos(Long cantidadVotos) { this.cantidadVotos = cantidadVotos; }
}