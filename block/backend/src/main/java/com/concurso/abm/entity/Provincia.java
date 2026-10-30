package com.concurso.abm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "provincia")
public class Provincia extends BaseEntity {
  @Column(length = 150, nullable = false)
  private String nombre;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "pais_id", nullable = false)
  private Pais pais;

  @OneToMany(mappedBy = "provincia")
  public java.util.List<Departamento> departamentos = new java.util.ArrayList<>();

  public String getNombre() {
    return nombre;
  }

  public Pais getPais() {
    return pais;
  }

  public void setNombre(String value) {
    this.nombre = value;
  }

  public void setPais(Pais value) {
    this.pais = value;
  }
}
