package com.concurso.abm.entity;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private boolean eliminado = false;

  @Version private long version;

  public Long getId() {
    return id;
  }

  public boolean getEliminado() {
    return eliminado;
  }

  public long getVersion() {
    return version;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public void setEliminado(boolean value) {
    this.eliminado = value;
  }

  public void setVersion(long value) {
    this.version = value;
  }
}
