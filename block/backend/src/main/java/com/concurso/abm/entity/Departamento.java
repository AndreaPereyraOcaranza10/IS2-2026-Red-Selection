package com.concurso.abm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "departamento")
public class Departamento extends BaseEntity {
  @Column(length = 150, nullable = false)
  private String nombre;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "provincia_id", nullable = false)
  private Provincia provincia;

  @OneToMany(mappedBy = "departamento")
  public java.util.List<Localidad> localidades = new java.util.ArrayList<>();

  public String getNombre() {
    return nombre;
  }

  public Provincia getProvincia() {
    return provincia;
  }

  public void setNombre(String value) {
    this.nombre = value;
  }

  public void setProvincia(Provincia value) {
    this.provincia = value;
  }
}
