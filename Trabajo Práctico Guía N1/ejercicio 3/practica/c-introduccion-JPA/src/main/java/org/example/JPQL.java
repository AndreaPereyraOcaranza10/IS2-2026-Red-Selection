package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.example.entidades.Cursos;
import org.example.entidades.PersonaMany;

import java.util.List;

public class JPQL {
    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("pruebaJPA");
        EntityManager em = emf.createEntityManager();

        try {
            // 1. Personas ordenadas por nombre (atributo 'name' de la entidad 'PersonaMany')
            List<PersonaMany> personasOrdenadas = em.createQuery(
                            "SELECT p FROM PersonaMany p ORDER BY p.name ASC", PersonaMany.class)
                    .getResultList();

            personasOrdenadas.forEach(System.out::println);

            // 2. Cursos con más de N inscriptos
            int minimo = 3; 
            List<Cursos> cursos = em.createQuery(
                            "SELECT c FROM Cursos c WHERE SIZE(c.inscriptos) > :minimo", Cursos.class)
                    .setParameter("minimo", minimo)
                    .getResultList();

            cursos.forEach(c -> System.out.println(c.getName()));

        } finally {
            em.close();
            emf.close();
        }
    }
}
