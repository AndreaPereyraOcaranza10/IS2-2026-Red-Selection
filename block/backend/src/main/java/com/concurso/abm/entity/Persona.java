package com.concurso.abm.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "persona")
public class Persona extends BaseEntity {
  @Column(length = 150, nullable = false)
  private String nombre;

  @Column(length = 150, nullable = false)
  private String apellido;

  @Column(length = 8)
  private String dni;

  @Column(name = "fecha_nacimiento")
  private LocalDate fechaNacimiento;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "direccion_id", nullable = false)
  private Direccion direccion;

  public String getNombre() {
    return nombre;
  }

  public String getApellido() {
    return apellido;
  }

  public Direccion getDireccion() {
    return direccion;
  }

  public String getDni() {
    return dni;
  }

  public LocalDate getFechaNacimiento() {
    return fechaNacimiento;
  }

  public void setNombre(String value) {
    this.nombre = value;
  }

  public void setApellido(String value) {
    this.apellido = value;
  }

  public void setDni(String value) {
    this.dni = value;
  }

  public void setFechaNacimiento(LocalDate value) {
    this.fechaNacimiento = value;
  }

  public void setDireccion(Direccion value) {
    this.direccion = value;
  }
}
