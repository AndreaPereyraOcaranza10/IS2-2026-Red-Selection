package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.example.entidades.Domicilio;
import org.example.entidades.PersonaRelaciones;

public class RelacionOneToManyU {
    public static void main(String[] args) {

        // --------- UNIDIRECCIONAL -----------

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("pruebaJPA");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Domicilio domicilio = new Domicilio();
            domicilio.setCalle("Av. Siempre Viva 742");
            domicilio.setCiudad("Springfield");
            em.persist(domicilio);

            Domicilio domicilio2 = new Domicilio();
            domicilio2.setCalle("Av. Siempre Viva 742");
            domicilio2.setCiudad("Springfield");
            em.persist(domicilio2);

            PersonaRelaciones p1 = new PersonaRelaciones();
            p1.setName("Homero Simpson");
            //p1.addDomicilio(domicilio);
            //p1.addDomicilio(domicilio2);
            em.persist(p1);

            PersonaRelaciones personaBuscada = em.find(PersonaRelaciones.class, p1.getId());

            // ------------- CASO ONE TO MANY (1 persona con muchos domicilios) -----------
            /*if (personaBuscada != null) {
                for (Domicilio d : personaBuscada.getDomicilios()) {
                    System.out.println("Domicilios: " + d);
                }
            }*/

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
