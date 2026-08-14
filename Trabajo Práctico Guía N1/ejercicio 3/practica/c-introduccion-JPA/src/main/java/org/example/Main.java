package org.example;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.entidades.Persona;

public class Main {
    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("pruebaJPA");
        EntityManager em = emf.createEntityManager();


        //agregar persona a la base de datos

        Persona p = new Persona();
        p.setName("Juan Perez");

        em.getTransaction().begin();
        em.persist(p);
        em.getTransaction().commit();

        System.out.println("Id de la persona: " + p.getId());

        //buscar persona por ID

        Long idABuscar = 2L;

        Persona pEncontrada = em.find(Persona.class, idABuscar);

        if (pEncontrada != null) {
            System.out.println("Nombre persona encontrada: " + pEncontrada.getName());
        } else {
            System.out.println("No se encontró una persona con ID: " + idABuscar);
        }

        em.close();
        emf.close();


    }
}