package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.entities.*;


public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("javaSpring");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();


            Factura f1 = new Factura();

            f1.setNumero(10);
            f1.setFecha("18/12/2022");

            //Domicilio dom = new Domicilio("Av Siempre viva", 174);
            //Cliente c = new Cliente("Homero", "Simpson", 1234567);

            Domicilio dom = Domicilio.builder().nombreCalle("Av Siempre viva").numero(174).build();
            Cliente c = Cliente.builder().nombre("Homero").apellido("Simpson").dni(1234567).build();

            c.setDomicilio(dom);
            dom.setCliente(c);

            f1.setCliente(c);

            //Categoria perecederos = new Categoria("perecederos");
            //Categoria lacteos = new Categoria("lacteos");
            //Categoria limpieza = new Categoria("limpieza");

            Categoria perecederos = Categoria.builder().denominacion("perecederos").build();
            Categoria lacteos = Categoria.builder().denominacion("lacteos").build();
            Categoria limpieza = Categoria.builder().denominacion("limpieza").build();

            //Articulo art1 = new Articulo(200,"Yogurt Ser", 20);
            //Articulo art2 = new Articulo(300,"Detergente", 100);

            Articulo art1 = Articulo.builder().cantidad(200).denominacion("Yogurt Ser").precio(20).build();
            Articulo art2 = Articulo.builder().cantidad(300).denominacion("Detergente Magistral").precio(100).build();

            art1.getCategorias().add(perecederos);
            art1.getCategorias().add(lacteos);

            lacteos.getArticulos().add(art1);
            perecederos.getArticulos().add(art1);

            art2.getCategorias().add(limpieza);
            limpieza.getArticulos().add(art2);

            DetalleFactura detalle = DetalleFactura.builder().build();

            detalle.setArticulo(art1);
            detalle.setCantidad(2);
            detalle.setSubtotal(40);

            art1.getDetalle().add(detalle);
            f1.addDetalle(detalle);

            //DetalleFactura d2 = new DetalleFactura();
            DetalleFactura d2 = DetalleFactura.builder().build();

            d2.setArticulo(art2);
            d2.setCantidad(3);
            d2.setSubtotal(300);

            art2.getDetalle().add(d2);
            f1.addDetalle(d2);

            f1.setTotal(340);

            em.persist(f1);

            em.flush();
            em.getTransaction().commit();

            Factura facturaBuscada = em.find(Factura.class, 1L);
            facturaBuscada.setNumero(1010);

            em.merge(facturaBuscada);

        } catch (Exception e) {

            em.getTransaction().rollback();

        } finally {
            em.close();
            emf.close();

        }



    }
}