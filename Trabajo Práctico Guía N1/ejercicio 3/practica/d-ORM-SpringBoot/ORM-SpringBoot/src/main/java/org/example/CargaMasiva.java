package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.entities.Articulo;
import org.example.entities.Cliente;

import java.math.BigDecimal;

public class CargaMasiva {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("javaSpring");
        EntityManager em = emf.createEntityManager();

        int batchSize = 50;
        int totalRegistros = 50000;

        try {
            em.getTransaction().begin();

            // cargar 50.000 clientes

            for (int i = 1; i <= totalRegistros; i++) {
                Cliente cliente = Cliente.builder()
                        .nombre("Nombre_" + i)
                        .apellido("Apellido_" + i)
                        .dni(20000000 + i)
                        .build();
                em.persist(cliente);

                if (i % batchSize == 0) {
                    em.flush();
                    em.clear(); // limpiar la memoria RAM tras la carga de los registros
                }
            }

            // cargar 50.000 articulos
            /*
            for (int i = 1; i <= totalRegistros; i++) {
                Articulo articulo = Articulo.builder()
                        .denominacion("Articulo_" + i)
                        .cantidad(100)
                        .precio(10*i+ 2)
                        .build();
                em.persist(articulo);

                if (i % batchSize == 0) {
                    em.flush();
                    em.clear();
                }
            }*/

            em.getTransaction().commit();
            em.clear();



            //EJEMPLO DE BUSQUEDA JPA CON INDICE EN DNI CLIENTE

            int dniABuscar = 20048500;
            long inicioConIndice = System.nanoTime();

            Cliente clienteConIndice = em.createQuery(
                            "SELECT c FROM Cliente c WHERE c.dni = :dni", Cliente.class)
                    .setParameter("dni", dniABuscar)
                    .getSingleResult();

            long finConIndice = System.nanoTime();
            double tiempoConIndiceMs = (finConIndice - inicioConIndice) / 1_000_000.0;

            System.out.println("=================================================");
            System.out.println("RESULTADOS DE RENDIMIENTO (50.000 REGISTROS)");
            System.out.println("=================================================");
            System.out.println("Tiempo con índice:      " + tiempoConIndiceMs + " ms");



        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }
}