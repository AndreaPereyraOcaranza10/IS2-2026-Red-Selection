package com.concurso.abm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 254)
  private String email;

  private String passwordHash;

  @Column(nullable = false)
  private String rol = "USER";

  @Column(nullable = false)
  private String proveedorAutenticacion = "LOCAL";

  private String proveedorId;

  @ManyToOne(fetch = FetchType.LAZY)
  private Persona persona;

  @Column(nullable = false)
  private boolean activo = true;

  @Column(nullable = false)
  private long credentialVersion = 0;

  @Version private long version;

  public Long getId() {
    return id;
  }

  public long getVersion() {
    return version;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getRol() {
    return rol;
  }

  public String getProveedorAutenticacion() {
    return proveedorAutenticacion;
  }

  public String getProveedorId() {
    return proveedorId;
  }

  public Persona getPersona() {
    return persona;
  }

  public boolean getActivo() {
    return activo;
  }

  public long getCredentialVersion() {
    return credentialVersion;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public void setVersion(long value) {
    this.version = value;
  }

  public void setEmail(String value) {
    this.email = value;
  }

  public void setPasswordHash(String value) {
    this.passwordHash = value;
  }

  public void setRol(String value) {
    this.rol = value;
  }

  public void setProveedorAutenticacion(String value) {
    this.proveedorAutenticacion = value;
  }

  public void setProveedorId(String value) {
    this.proveedorId = value;
  }

  public void setPersona(Persona value) {
    this.persona = value;
  }

  public void setActivo(boolean value) {
    this.activo = value;
  }

  public void setCredentialVersion(long value) {
    this.credentialVersion = value;
  }
}
