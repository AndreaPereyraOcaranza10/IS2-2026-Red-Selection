package com.concurso.abm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "localidad")
public class Localidad extends BaseEntity {
  @Column(length = 150, nullable = false)
  private String nombre;

  @Column(length = 150, nullable = false)
  private String codigoPostal;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "departamento_id", nullable = false)
  private Departamento departamento;

  @OneToMany(mappedBy = "localidad")
  public java.util.List<Direccion> direcciones = new java.util.ArrayList<>();

  public String getNombre() {
    return nombre;
  }

  public String getCodigoPostal() {
    return codigoPostal;
  }

  public Departamento getDepartamento() {
    return departamento;
  }

  public void setNombre(String value) {
    this.nombre = value;
  }

  public void setCodigoPostal(String value) {
    this.codigoPostal = value;
  }

  public void setDepartamento(Departamento value) {
    this.departamento = value;
  }
}
