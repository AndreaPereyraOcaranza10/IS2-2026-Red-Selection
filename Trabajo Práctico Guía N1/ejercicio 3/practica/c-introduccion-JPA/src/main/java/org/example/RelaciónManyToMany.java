package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.example.entidades.Cursos;
import org.example.entidades.Domicilio;
import org.example.entidades.PersonaMany;
import org.example.entidades.PersonaRelaciones;

public class RelaciónManyToMany {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("pruebaJPA");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Cursos c1 = new Cursos("Java Básico");
            Cursos c2 = new Cursos("Ingeniería de Software II");
            em.persist(c1);
            em.persist(c2);

            PersonaMany p1 = new PersonaMany("Lisa Simpson");
            p1.agregarCurso(c1);
            p1.agregarCurso(c2);
            em.persist(p1);

            tx.commit();
            System.out.println(em.find(PersonaMany.class, 1L));
            Cursos curso = em.find(Cursos.class, 1L);
            for (PersonaMany p : curso.getInscriptos()) {
                System.out.println("Persona " + p.getName());
            }

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
