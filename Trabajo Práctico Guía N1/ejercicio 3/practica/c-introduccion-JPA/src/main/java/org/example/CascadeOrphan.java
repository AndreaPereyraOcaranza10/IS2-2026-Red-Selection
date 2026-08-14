package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.example.entidades.Domicilio;
import org.example.entidades.PersonaRelaciones;

public class CascadeOrphan {
    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("pruebaJPA");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Domicilio domicilio = new Domicilio();
            domicilio.setCalle("Av. Siempre Viva 742");
            domicilio.setCiudad("Springfield");
            //em.persist(domicilio); no es necesario porque tenemos el cascadeType.ALL
            //cuando se persiste la persona se persiste el domicilio

            PersonaRelaciones p1 = new PersonaRelaciones();
            p1.setName("Homero Simpson");
            p1.setDomicilio(domicilio);
            em.persist(p1);

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

        } finally {
            em.close();
            emf.close();
        }
    }
}
