package com.example.demo.business.domain.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/* @Entity: se utiliza para marcar una clase Java como una entidad que se mapea a una tabla en una base 
 * de datos relacional. Es decir, Spring Boot utiliza esta anotación para reconocer que una 
 * clase Java representa una tabla en la base de datos y permite realizar operaciones CRUD 
 * (Crear, Leer, Actualizar, Borrar) sobre esa tabla a través de la clase entidad.
 */

@Entity
public class Pais implements Serializable {
    
	/* @Id:Marca el campo como la clave primaria de la entidad.
     * En JPA, cada entidad debe tener una clave primaria,
     * que sirve como identificador único en la base de datos.
     */
    @Id
    private String id;
    
    private String nombre;
    private boolean eliminado;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Pais)) {
            return false;
        }
        Pais other = (Pais) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.demo.business.domain.entity.Pais[ id=" + id + " ]";
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }
    
}
