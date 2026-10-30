package com.concurso.abm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pais")
public class Pais extends BaseEntity {
  @Column(length = 150, nullable = false)
  private String nombre;

  @OneToMany(mappedBy = "pais")
  public java.util.List<Provincia> provincias = new java.util.ArrayList<>();

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String value) {
    this.nombre = value;
  }
}
