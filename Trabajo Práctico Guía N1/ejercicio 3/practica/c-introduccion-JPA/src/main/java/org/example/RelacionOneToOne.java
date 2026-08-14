package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.example.entidades.Domicilio;
import org.example.entidades.PersonaRelaciones;

public class RelacionOneToOne {

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
            p1.setDomicilio(domicilio);
            em.persist(p1);

            PersonaRelaciones p2 = new PersonaRelaciones();
            p2.setName("Marge Simpson");
            p2.setDomicilio(domicilio2);
            em.persist(p2);

            PersonaRelaciones personaBuscada = em.find(PersonaRelaciones.class, 1L);

            tx.commit();
            System.out.println("Relaciones OneToOne guardadas con éxito.");
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

        } finally {
            em.close();
            emf.close();
        }
    }
        // --------- BIDIRECCIONAL ejemplo -----------
        /*
        try {
            tx.begin();

            Domicilio domicilio = new Domicilio();
            domicilio.setCalle("Wallaby Way 42");
            domicilio.setCiudad("Sydney);
            em.persist(domicilio);


            PersonaRelaciones p1 = new PersonaRelaciones();
            p1.setName("Dory");
            p1.setDomicilio(domicilio);
            em.persist(p1);

            tx.commit();

            PersonaRelaciones personaEncontrada = em.find(PersonaRelaciones.class, 1L);

            Domicilio domicilioEncontrado = em.find(Domicilio.class, 1L);
            if (domicilioEncontrado != null) {
                System.out.println("Persona encontrada: " + domicilioEncontrado.getPersona);
            } else {
                System.out.println("Persona no encontrada");
            }

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

        } finally {
            em.close();
            emf.close();

        }
        */



}