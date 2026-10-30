package com.concurso.abm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "direccion")
public class Direccion extends BaseEntity {
  @Column(length = 150, nullable = false)
  private String calle;

  @Column(length = 150, nullable = false)
  private String numeracion;

  @Column(length = 150, nullable = true)
  private String barrio;

  @Column(length = 150, nullable = true)
  private String manzanaPiso;

  @Column(length = 150, nullable = true)
  private String casaDepartamento;

  @Column(length = 500, nullable = true)
  private String referencia;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "localidad_id", nullable = false)
  private Localidad localidad;

  @OneToMany(mappedBy = "direccion")
  public java.util.List<Persona> personas = new java.util.ArrayList<>();

  public String getCalle() {
    return calle;
  }

  public String getNumeracion() {
    return numeracion;
  }

  public String getBarrio() {
    return barrio;
  }

  public String getManzanaPiso() {
    return manzanaPiso;
  }

  public String getCasaDepartamento() {
    return casaDepartamento;
  }

  public String getReferencia() {
    return referencia;
  }

  public Localidad getLocalidad() {
    return localidad;
  }

  public void setCalle(String value) {
    this.calle = value;
  }

  public void setNumeracion(String value) {
    this.numeracion = value;
  }

  public void setBarrio(String value) {
    this.barrio = value;
  }

  public void setManzanaPiso(String value) {
    this.manzanaPiso = value;
  }

  public void setCasaDepartamento(String value) {
    this.casaDepartamento = value;
  }

  public void setReferencia(String value) {
    this.referencia = value;
  }

  public void setLocalidad(Localidad value) {
    this.localidad = value;
  }
}
